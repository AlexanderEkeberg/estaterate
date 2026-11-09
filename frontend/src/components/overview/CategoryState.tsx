import { CircleAlert } from "lucide-react";
import { cx } from "../../lib/cx.js";
import ui from "../../style/ui.module.css";
import styles from "./CategoryState.module.css";

export function InlineUnavailable() {
  return (
    <div className={styles.inlineFail}>
      <CircleAlert size={16} strokeWidth={2.2} aria-hidden />
      <span>Unavailable – not included in the report</span>
    </div>
  );
}

export function InlinePending() {
  return (
    <>
      <div className={styles.inlinePending}>
        <span className={ui.pulseDot} aria-hidden />
        <span>Still analysing…</span>
      </div>
      <div className={cx(ui.skeleton, styles.meterSkeleton)} aria-hidden />
    </>
  );
}
