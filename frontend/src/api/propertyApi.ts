

export async function scrapeProperty(url: string): Promise<void> {
    const response = await fetch("/api/properties/scrape", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({url})
    });
    if (!response.ok) {
        throw new Error(`Scraping feilet: ${response.status}`);
    }
}