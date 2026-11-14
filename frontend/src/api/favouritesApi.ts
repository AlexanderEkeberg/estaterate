import { API_URL } from "./apiBase.js";
import { authHeaders } from "./authHeaders.js";
import { ApiError } from "./propertyApi.js";

export type Favourite = {
  analysisId: string;
  savedAt: string;
  available: boolean;
  status: string | null;
  scores: Record<string, number>;
  address: string | null;
};

async function send(
  method: string,
  path: string,
  body?: unknown,
): Promise<Response> {
  const headers = await authHeaders();
  if (body !== undefined) {
    headers["Content-Type"] = "application/json";
  }
  let response: Response;
  try {
    response = await fetch(`${API_URL}/api/favourites${path}`, {
      method,
      headers,
      body: body === undefined ? null : JSON.stringify(body),
    });
  } catch {
    throw new ApiError("Network error");
  }
  if (!response.ok) {
    throw new ApiError(
      `Favourites request failed: ${response.status}`,
      response.status,
    );
  }
  return response;
}

function toFavourite(value: unknown): Favourite | null {
  if (typeof value !== "object" || value === null) {
    return null;
  }
  const item = value as Record<string, unknown>;
  if (
    typeof item.analysisId !== "string" ||
    typeof item.savedAt !== "string" ||
    typeof item.available !== "boolean"
  ) {
    return null;
  }
  const scores: Record<string, number> = {};
  if (typeof item.scores === "object" && item.scores !== null) {
    for (const [category, score] of Object.entries(item.scores)) {
      if (typeof score === "number") {
        scores[category] = score;
      }
    }
  }
  return {
    analysisId: item.analysisId,
    savedAt: item.savedAt,
    available: item.available,
    status: typeof item.status === "string" ? item.status : null,
    scores,
    address: typeof item.address === "string" ? item.address : null,
  };
}

export async function listFavourites(): Promise<Favourite[]> {
  const data: unknown = await (await send("GET", "")).json();
  if (!Array.isArray(data)) {
    throw new ApiError("Invalid favourites response");
  }
  return data
    .map(toFavourite)
    .filter((item): item is Favourite => item !== null);
}

export async function saveFavourite(analysisId: string): Promise<void> {
  await send("POST", "", { analysisId });
}

export async function removeFavourite(analysisId: string): Promise<void> {
  await send("DELETE", `/${encodeURIComponent(analysisId)}`);
}
