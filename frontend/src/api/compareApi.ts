import { API_URL } from "./apiBase.js";
import { authHeaders } from "./authHeaders.js";
import { ApiError } from "./propertyApi.js";
import type { Analysis } from "./analysisApi.js";

function isAnalysis(value: unknown): value is Analysis {
  if (typeof value !== "object" || value === null) return false;
  const { analysisId, categories } = value as Record<string, unknown>;
  return (
    typeof analysisId === "string" &&
    typeof categories === "object" &&
    categories !== null
  );
}

export async function getComparedAnalysis(
  analysisId: string,
): Promise<Analysis> {
  const headers = await authHeaders();
  let response: Response;
  try {
    response = await fetch(
      `${API_URL}/api/compare/${encodeURIComponent(analysisId)}`,
      { headers },
    );
  } catch {
    throw new ApiError("Network error");
  }
  if (!response.ok) {
    throw new ApiError(
      `Fetching comparison failed: ${response.status}`,
      response.status,
    );
  }
  const data: unknown = await response.json();
  if (!isAnalysis(data)) {
    throw new ApiError("Invalid comparison response");
  }
  return data;
}
