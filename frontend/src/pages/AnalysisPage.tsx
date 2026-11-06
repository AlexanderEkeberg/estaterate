import { Navbar } from "../components/layout/Navbar.js";
import { DataNotice } from "../components/analysis/DataNotice.js";
import { loadScrapeResult } from "../storage/scrapeResults.js";
import styles from "./AnalysisPage.module.css";

type AnalysisPageProps = {
  analysisId: string;
};

export function AnalysisPage({ analysisId }: AnalysisPageProps) {
  const scrapeResult = loadScrapeResult(analysisId);
  const notice =
    scrapeResult?.coordinatesSource === "MOCK" ? scrapeResult.message : null;

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
      </section>
    </main>
  );
}
