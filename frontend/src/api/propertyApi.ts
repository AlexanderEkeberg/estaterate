import { API_URL } from "./apiBase.js";

export class ApiError extends Error {
    readonly status: number | null;

    constructor(message: string, status: number | null = null) {
        super(message);
        this.name = "ApiError";
        this.status = status;
    }
}

export type CoordinatesSource = "SCRAPED" | "MOCK";

export type ScrapeResult = {
    analysisId: string;
    coordinatesSource: CoordinatesSource;
    message: string | null;
};

export async function scrapeProperty(url: string): Promise<ScrapeResult> {
    let response: Response;
    try {
        response = await fetch(`${API_URL}/api/properties/scrape`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ url }),
        });
    } catch {
        throw new ApiError("Network error");
    }
    if (!response.ok) {
        throw new ApiError(`Scraping failed: ${response.status}`, response.status);
    }
    return (await response.json()) as ScrapeResult;
}
