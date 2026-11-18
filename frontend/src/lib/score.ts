import type { PropertyReport } from "../types/report.js";

export const SCORED_CATEGORY_COUNT = 2;

export type TotalScore = {
  total: number | null;
  used: number;
  pending: number;
};

export type ScoredCategory = "crime" | "schools";

export type EnabledCategories = Record<ScoredCategory, boolean>;

export function computeTotalScore(
  report: Pick<PropertyReport, "crime" | "schools">,
  enabled: EnabledCategories = { crime: true, schools: true },
): TotalScore {
  const scores: number[] = [];
  let pending = 0;

  for (const [category, result] of [
    ["crime", report.crime],
    ["schools", report.schools],
  ] as const) {
    if (!enabled[category]) continue;

    if (result.status === "AVAILABLE" && result.data) {
      scores.push(result.data.score);
    }
    if (result.status === "PENDING") pending++;
  }

  return { total: average(scores), used: scores.length, pending };
}

export function totalFromScores(scores: Record<string, number>): number | null {
  return average(
    [scores["CRIME"], scores["SCHOOL"]].filter(
      (score): score is number => score != null,
    ),
  );
}

function average(scores: number[]): number | null {
  return scores.length
    ? Math.round(scores.reduce((a, b) => a + b, 0) / scores.length)
    : null;
}
