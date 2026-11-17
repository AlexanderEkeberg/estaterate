import { ArrowRight, Columns2, X } from "lucide-react";
import { useAuth } from "../../auth/AuthProvider.js";
import { MAX_COMPARE, useCompare } from "./CompareContext.js";
import { AppLink } from "../ui/AppLink.js";
import { cx } from "../../lib/cx.js";
import ui from "../../style/ui.module.css";
import styles from "./CompareTray.module.css";

export function CompareTray() {
  const { status } = useAuth();
  const { items, remove, clear } = useCompare();

  if (status !== "authenticated" || items.length === 0) {
    return null;
  }

  const count = items.length;
  const ids = items.map((item) => encodeURIComponent(item.id)).join(",");

  return (
    <>
      <div className={styles.spacer} />
      <div className={styles.wrap}>
        <div role="region" aria-label="Compare tray" className={styles.tray}>
          <div className={styles.label}>
            <Columns2 size={18} color="var(--er-blue)" aria-hidden />
            <span>Compare</span>
            <span className={styles.count}>
              {count} of {MAX_COMPARE}
            </span>
          </div>

          <ul className={styles.chips}>
            {items.map((item) => {
              const name = item.address ?? "Unknown address";
              return (
                <li key={item.id} className={styles.chip}>
                  <span className={styles.address}>{name}</span>
                  <span className={styles.score}>{item.totalScore ?? "–"}</span>
                  <button
                    type="button"
                    className={styles.remove}
                    onClick={() => remove(item.id)}
                    aria-label={`Remove ${name} from compare`}
                  >
                    <X size={16} aria-hidden />
                  </button>
                </li>
              );
            })}
            {count < MAX_COMPARE && (
              <li className={styles.slot} aria-hidden>
                Free slot
              </li>
            )}
          </ul>

          <div className={styles.actions}>
            <button type="button" className={styles.clear} onClick={clear}>
              Clear
            </button>
            {count === MAX_COMPARE ? (
              <AppLink
                to={`/compare?ids=${ids}`}
                className={cx(ui.btn, ui.primary)}
              >
                Compare ({count})
                <ArrowRight size={16} aria-hidden />
              </AppLink>
            ) : (
              <button type="button" disabled className={cx(ui.btn, ui.outline)}>
                Select {MAX_COMPARE - count} more to compare
              </button>
            )}
          </div>
        </div>
      </div>
    </>
  );
}
