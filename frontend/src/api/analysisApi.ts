import { API_URL } from "./apiBase.js";
import { ApiError } from "./propertyApi.js";

export type CategoryStatus = "PENDING" | "SUCCESS" | "FAILED";

export type CategoryResult = {
  status: CategoryStatus;
  score: number | null;
  data: unknown;
  error: string | null;
};

export type Analysis = {
  analysisId: string;
  status: "RUNNING" | "DONE";
  categories: Record<string, CategoryResult>;
};

// Returns null while the aggregator has not created the analysis yet.
export async function getAnalysis(
  analysisId: string,
): Promise<Analysis | null> {
  const response = await fetch(
    `${API_URL}/api/analysis/${encodeURIComponent(analysisId)}`,
  );
  if (response.status === 404) {
    return null;
  }
  if (!response.ok) {
    throw new ApiError(
      `Fetching analysis failed: ${response.status}`,
      response.status,
    );
  }
  return (await response.json()) as Analysis;
}
