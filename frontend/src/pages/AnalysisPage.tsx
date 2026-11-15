import { useEffect, useState } from "react";
import { Navbar } from "../components/layout/Navbar.js";
import { DataNotice } from "../components/analysis/DataNotice.js";
import { AnalysisProgress } from "../components/analysis/AnalysisProgress.js";
import type { CategoryProgress } from "../components/analysis/AnalysisProgress.js";
import {
  DEFAULT_CATEGORIES,
  progressCategories,
} from "../components/analysis/categories.js";
import { useAnalysis } from "../hooks/useAnalysis.js";
import { loadScrapeResult } from "../storage/scrapeResults.js";
import { toPropertyReport } from "../api/reportMapper.js";
import { OverviewPage } from "./OverviewPage.js";
import styles from "./AnalysisPage.module.css";

type AnalysisPageProps = {
  analysisId: string;
};

const SPINNER_DELAY_MS = 300;

function DelayedSpinner() {
  const [visible, setVisible] = useState(false);

  useEffect(() => {
    const timer = window.setTimeout(() => setVisible(true), SPINNER_DELAY_MS);
    return () => window.clearTimeout(timer);
  }, []);

  return visible ? (
    <div className={styles.spinner} role="status" aria-label="Loading report" />
  ) : null;
}

export function AnalysisPage({ analysisId }: AnalysisPageProps) {
  const scrapeResult = loadScrapeResult(analysisId);
  const notice =
    scrapeResult?.coordinatesSource === "MOCK" ? scrapeResult.message : null;

  const saved = new URLSearchParams(window.location.search).has("saved");
  const { analysis, timedOut, finished } = useAnalysis(analysisId, saved);
  const categories: CategoryProgress[] = (
    analysis
      ? progressCategories(Object.keys(analysis.categories))
      : DEFAULT_CATEGORIES
  ).map((category) => {
    const status = analysis?.categories[category]?.status ?? "PENDING";
    return {
      category,
      state: status === "PENDING" && timedOut ? "TIMED_OUT" : status,
    };
  });

  if (finished && analysis) {
    return (
      <OverviewPage
        report={toPropertyReport(analysis, scrapeResult, timedOut)}
      />
    );
  }

  if (saved) {
    return (
      <main className={styles.page}>
        <Navbar />
        {finished ? (
          <section className={styles.content}>
            <h1 className={`${styles.title} bold-font`}>Report unavailable</h1>
            <p className={styles.subtitle}>
              This report is unavailable right now. Try again later.
            </p>
          </section>
        ) : (
          <DelayedSpinner />
        )}
      </main>
    );
  }

  return (
    <main className={styles.page}>
      <Navbar />
      <section className={styles.content}>
        <p className={styles.eyebrow}>Analyzing property</p>
        <h1 className={`${styles.title} bold-font`}>
          Analyzing the property ...
        </h1>
        <p className={styles.subtitle}>
          We're collecting and analyzing relevant information from multiple
          sources to give you a complete overview.
        </p>
        {notice && <DataNotice message={notice} />}
        <AnalysisProgress
          categories={categories}
          finished={finished}
          failedToLoad={timedOut && !analysis}
        />
      </section>
    </main>
  );
}
