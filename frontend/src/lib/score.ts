import type { PropertyReport } from "../types/report.js";

export const SCORED_CATEGORY_COUNT = 2;

export type TotalScore = {
  total: number | null;
  used: number;
  pending: number;
};

export function computeTotalScore(
  report: Pick<PropertyReport, "crime" | "schools">,
): TotalScore {
  const scores: number[] = [];
  let pending = 0;

  for (const result of [report.crime, report.schools]) {
    if (result.status === "AVAILABLE" && result.data) {
      scores.push(result.data.score);
    }
    if (result.status === "PENDING") {
      pending++;
    }
  }

  const total = scores.length
    ? Math.round(scores.reduce((a, b) => a + b, 0) / scores.length)
    : null;
  return { total, used: scores.length, pending };
}
