import type { ReactNode } from "react";
import { GraduationCap, ShieldCheck, Sun } from "lucide-react";
import type { CategoryResult, PropertyReport } from "../../types/report.js";
import { computeTotalScore, SCORED_CATEGORY_COUNT } from "../../lib/score.js";
import { cx } from "../../lib/cx.js";
import { InlinePending, InlineUnavailable } from "./CategoryState.js";
import ui from "../../style/ui.module.css";
import styles from "./ScoreSummary.module.css";

type MiniCardProps = {
  title: string;
  icon: ReactNode;
  tileClass: string | undefined;
  result: CategoryResult<{ score: number }>;
};

function MiniCard({ title, icon, tileClass, result }: MiniCardProps) {
  return (
    <div className={styles.mini}>
      <div className={styles.miniHead}>
        <span className={cx(ui.iconTile, ui.iconTileSm, tileClass)}>
          {icon}
        </span>
        {title}
      </div>
      {result.status === "AVAILABLE" && result.data && (
        <>
          <div>
            <span className={styles.miniScore}>{result.data.score}</span>
            <span className={styles.outOf}> / 100</span>
          </div>
          <div className={styles.meter} role="presentation">
            <div
              className={styles.meterFill}
              style={{ width: `${result.data.score}%` }}
            />
          </div>
        </>
      )}
      {result.status === "UNAVAILABLE" && <InlineUnavailable />}
      {result.status === "PENDING" && <InlinePending />}
    </div>
  );
}

export function ScoreSummary({ report }: { report: PropertyReport }) {
  const { total, used, pending } = computeTotalScore(report);
  const degrees = total != null ? Math.round(total * 3.6) : 0;

  let basedOn = `Based on ${used} of ${SCORED_CATEGORY_COUNT} categories`;
  if (pending) basedOn += ` · ${pending} still analysing`;

  return (
    <section
      aria-labelledby="score-title"
      className={cx(ui.card, styles.summary)}
    >
      <div className={styles.totalBlock}>
        <div
          role="img"
          aria-label={
            total != null
              ? `Total score ${total} out of 100`
              : "No total score yet"
          }
          className={styles.ring}
          style={{
            background: `conic-gradient(var(--er-blue) ${degrees}deg, var(--er-border) 0deg)`,
          }}
        >
          <div className={styles.ringInner}>
            <span className={styles.ringValue}>{total ?? "–"}</span>
            <span className={styles.ringLabel}>of 100</span>
          </div>
        </div>
        <div className={styles.totalText}>
          <h2 id="score-title" className={ui.h2}>
            Total score
          </h2>
          <p className={styles.basedOn}>{basedOn}</p>
          <p className={styles.note}>
            Weather is shown for context and is not part of the score.
          </p>
        </div>
      </div>

      <div className={styles.miniCards}>
        <MiniCard
          title="Safety & crime"
          icon={<ShieldCheck size={18} aria-hidden />}
          tileClass={ui.crimeTile}
          result={report.crime}
        />
        <MiniCard
          title="Schools"
          icon={<GraduationCap size={18} aria-hidden />}
          tileClass={ui.schoolsTile}
          result={report.schools}
        />
        <div className={styles.mini}>
          <div className={styles.miniHead}>
            <span className={cx(ui.iconTile, ui.iconTileSm, ui.weatherTile)}>
              <Sun size={18} aria-hidden />
            </span>
            Weather
          </div>
          <div className={styles.notScored}>Not scored</div>
        </div>
      </div>
    </section>
  );
}
