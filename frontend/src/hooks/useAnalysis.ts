import { useEffect, useState } from "react";
import { getAnalysis } from "../api/analysisApi.js";
import type { Analysis } from "../api/analysisApi.js";

const POLL_INTERVAL_MS = 1500;
// Categories that have not answered by then are shown as unavailable.
const TIMEOUT_MS = 60_000;

export type AnalysisState = {
  analysis: Analysis | null;
  timedOut: boolean;
  finished: boolean;
};

export function useAnalysis(analysisId: string): AnalysisState {
  const [analysis, setAnalysis] = useState<Analysis | null>(null);
  const [timedOut, setTimedOut] = useState(false);

  useEffect(() => {
    let cancelled = false;
    let timer: number | undefined;
    const startedAt = Date.now();
    setAnalysis(null);
    setTimedOut(false);

    async function poll() {
      try {
        const result = await getAnalysis(analysisId);
        if (cancelled) return;
        if (result) {
          setAnalysis(result);
          if (result.status === "DONE") return;
        }
      } catch (error) {
        if (cancelled) return;
        console.error(error);
      }
      if (Date.now() - startedAt >= TIMEOUT_MS) {
        setTimedOut(true);
        return;
      }
      timer = window.setTimeout(poll, POLL_INTERVAL_MS);
    }

    void poll();
    return () => {
      cancelled = true;
      window.clearTimeout(timer);
    };
  }, [analysisId]);

  return {
    analysis,
    timedOut,
    finished: analysis?.status === "DONE" || timedOut,
  };
}
