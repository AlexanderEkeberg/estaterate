import { ArrowLeft } from "lucide-react";
import type { PropertyReport } from "../types/report.js";
import { Navbar } from "../components/layout/Navbar.js";
import { OverviewHeader } from "../components/overview/OverviewHeader.js";
import { ScoreSummary } from "../components/overview/ScoreSummary.js";
import { WeatherSection } from "../components/overview/WeatherSection.js";
import { navigate } from "../main/router.js";
import { cx } from "../lib/cx.js";
import ui from "../style/ui.module.css";
import styles from "./OverviewPage.module.css";

export function OverviewPage({ report }: { report: PropertyReport }) {
  return (
    <div className={styles.page}>
      <Navbar />
      <main className={ui.page}>
        <OverviewHeader report={report} />
        <ScoreSummary report={report} />
        <WeatherSection result={report.weather} />

        <div className={styles.footer}>
          <button
            type="button"
            className={cx(ui.btn, ui.primary, ui.lg)}
            onClick={() => navigate("/")}
          >
            <ArrowLeft size={18} aria-hidden />
            Analyze another property
          </button>
        </div>
      </main>
    </div>
  );
}
