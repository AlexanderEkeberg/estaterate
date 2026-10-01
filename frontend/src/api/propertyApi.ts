export class ApiError extends Error {
    readonly status: number | null;

    constructor(message: string, status: number | null = null) {
        super(message);
        this.name = "ApiError";
        this.status = status;
    }
}

export async function scrapeProperty(url: string): Promise<void> {
    let response: Response;
    try {
        response = await fetch("/api/properties/scrape", {
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
}
