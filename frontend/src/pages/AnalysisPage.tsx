import { Navbar } from "../components/layout/Navbar.js";
import { DataNotice } from "../components/analysis/DataNotice.js";
import { AnalysisProgress } from "../components/analysis/AnalysisProgress.js";
import type { CategoryProgress } from "../components/analysis/AnalysisProgress.js";
import {
  DEFAULT_CATEGORIES,
  sortCategories,
} from "../components/analysis/categories.js";
import { useAnalysis } from "../hooks/useAnalysis.js";
import { loadScrapeResult } from "../storage/scrapeResults.js";
import styles from "./AnalysisPage.module.css";

type AnalysisPageProps = {
  analysisId: string;
};

export function AnalysisPage({ analysisId }: AnalysisPageProps) {
  const scrapeResult = loadScrapeResult(analysisId);
  const notice =
    scrapeResult?.coordinatesSource === "MOCK" ? scrapeResult.message : null;

  const { analysis, timedOut, finished } = useAnalysis(analysisId);
  const categories: CategoryProgress[] = (
    analysis
      ? sortCategories(Object.keys(analysis.categories))
      : DEFAULT_CATEGORIES
  ).map((category) => {
    const status = analysis?.categories[category]?.status ?? "PENDING";
    return {
      category,
      state: status === "PENDING" && timedOut ? "TIMED_OUT" : status,
    };
  });

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
