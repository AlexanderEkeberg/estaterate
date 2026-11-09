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

export function SectionUnavailable({ message }: { message: string }) {
  return (
    <div role="status" className={styles.failBox}>
      <CircleAlert size={22} aria-hidden />
      <div className={styles.failText}>
        <div className={styles.failTitle}>
          Unavailable – not included in the report
        </div>
        <div className={styles.failBody}>{message}</div>
      </div>
    </div>
  );
}

const SKELETON_WIDTHS = ["55%", "80%", "65%", "70%"];

export function SectionPending({ lines = 3 }: { lines?: number }) {
  return (
    <>
      <div role="status" className={styles.inlinePending}>
        <span className={ui.pulseDot} aria-hidden />
        Still analysing…
      </div>
      <div className={styles.skeletonStack} aria-hidden>
        {SKELETON_WIDTHS.slice(0, lines).map((width, index) => (
          <div
            key={width}
            className={ui.skeleton}
            style={{ height: index === 0 ? 28 : 14, width }}
          />
        ))}
      </div>
    </>
  );
}
