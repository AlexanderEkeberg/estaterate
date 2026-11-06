import type { ScrapeResult } from "../api/propertyApi.js";

// The scrape response is kept per analysis so the analysis page can show
// whether the data is mock, also after a page refresh in the same tab.

function key(analysisId: string): string {
  return `scrape-result:${analysisId}`;
}

export function saveScrapeResult(result: ScrapeResult): void {
  try {
    sessionStorage.setItem(key(result.analysisId), JSON.stringify(result));
  } catch {
    // storage unavailable: the page still works, only the notice is missing
  }
}

export function loadScrapeResult(analysisId: string): ScrapeResult | null {
  try {
    const raw = sessionStorage.getItem(key(analysisId));
    return raw ? (JSON.parse(raw) as ScrapeResult) : null;
  } catch {
    return null;
  }
}
