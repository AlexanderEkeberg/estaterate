import { Fragment } from "react";
import { CategoryRow } from "./CategoryRow.js";
import type { RowState } from "./CategoryRow.js";
import { StatusIcon } from "./StatusIcon.js";
import type { Status } from "./StatusIcon.js";
import styles from "./AnalysisProgress.module.css";

export type CategoryProgress = {
  category: string;
  state: RowState;
};

type AnalysisProgressProps = {
  categories: CategoryProgress[];
  finished: boolean;
  failedToLoad: boolean;
};

export function AnalysisProgress({
  categories,
  finished,
  failedToLoad,
}: AnalysisProgressProps) {
  const steps: { label: string; status: Status }[] = [
    { label: "Property found", status: "done" },
    { label: "Analyzing data", status: finished ? "done" : "active" },
    { label: "Building overview", status: finished ? "done" : "upcoming" },
  ];

  return (
    <div className={styles.card}>
      <ol className={styles.steps}>
        {steps.map((step, index) => (
          <Fragment key={step.label}>
            {index > 0 && <li className={styles.line} aria-hidden="true" />}
            <li className={styles.step}>
              <StatusIcon status={step.status} />
              <span className={styles.stepLabel}>{step.label}</span>
            </li>
          </Fragment>
        ))}
      </ol>

      <ul className={styles.rows}>
        {categories.map(({ category, state }) => (
          <CategoryRow key={category} category={category} state={state} />
        ))}
      </ul>

      {(finished || failedToLoad) && (
        <p className={styles.footer}>
          {failedToLoad
            ? "We could not load the analysis. Please try again."
            : "Analysis complete."}
        </p>
      )}
    </div>
  );
}
