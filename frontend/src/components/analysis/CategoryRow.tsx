import type { CategoryStatus } from "../../api/analysisApi.js";
import { categoryInfo } from "./categories.js";
import { StatusIcon } from "./StatusIcon.js";
import type { Status } from "./StatusIcon.js";
import styles from "./CategoryRow.module.css";

export type RowState = CategoryStatus | "TIMED_OUT";

const ICON_STATUS: Record<RowState, Status> = {
  PENDING: "active",
  SUCCESS: "done",
  FAILED: "failed",
  TIMED_OUT: "failed",
};

const HINTS: Partial<Record<RowState, string>> = {
  FAILED: "Unavailable, not included in the report",
  TIMED_OUT: "No response, not included in the report",
};

type CategoryRowProps = {
  category: string;
  state: RowState;
};

export function CategoryRow({ category, state }: CategoryRowProps) {
  const { label, icon: Icon, tone } = categoryInfo(category);
  const hint = HINTS[state];

  return (
    <li className={styles.row}>
      <span className={`${styles.badge} ${styles[tone]}`}>
        <Icon size={22} />
      </span>
      <div className={styles.body}>
        <div className={styles.text}>
          <span className={styles.label}>{label}</span>
          {hint && <span className={styles.hint}>{hint}</span>}
        </div>
        <StatusIcon status={ICON_STATUS[state]} />
      </div>
    </li>
  );
}
