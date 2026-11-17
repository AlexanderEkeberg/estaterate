import { useState } from "react";
import { MapPin, TriangleAlert } from "lucide-react";
import type { PropertyReport } from "../../types/report.js";
import { formatNok, formatNumber } from "../../lib/format.js";
import { SaveButton } from "./SaveButton.js";
import { CompareButton } from "./CompareButton.js";
import { LoginPopover } from "./LoginPopover.js";
import { useDismiss } from "../../hooks/useDismiss.js";
import ui from "../../style/ui.module.css";
import styles from "./OverviewHeader.module.css";

export function OverviewHeader({ report }: { report: PropertyReport }) {
  const hasFacts = report.askingPriceNok != null || report.sizeM2 != null;
  const [loginOpen, setLoginOpen] = useState(false);
  const actionsRef = useDismiss<HTMLDivElement>(loginOpen, () =>
    setLoginOpen(false),
  );
  const askLogin = () => setLoginOpen(true);

  return (
    <section aria-labelledby="overview-title" className={styles.header}>
      <div className={styles.row}>
        <div className={styles.titleBlock}>
          <div className={ui.eyebrow}>Property overview</div>
          <h1 id="overview-title" className={ui.h1}>
            Your property overview
          </h1>

          {report.address && (
            <div className={styles.address}>
              <MapPin size={18} color="var(--er-muted)" aria-hidden />
              <span>{report.address}</span>
            </div>
          )}

          {hasFacts && (
            <div className={styles.facts}>
              {report.askingPriceNok != null && (
                <span className={styles.fact}>
                  Asking price{" "}
                  <strong>{formatNok(report.askingPriceNok)}</strong>
                </span>
              )}
              {report.sizeM2 != null && (
                <span className={styles.fact}>
                  Size{" "}
                  <strong>
                    {formatNumber(report.sizeM2)} m²
                    {report.sizeType ? ` (${report.sizeType})` : ""}
                  </strong>
                </span>
              )}
            </div>
          )}
        </div>

        <div className={styles.actions} ref={actionsRef}>
          <SaveButton analysisId={report.id} onLoginRequired={askLogin} />
          <CompareButton report={report} onLoginRequired={askLogin} />
          {loginOpen && <LoginPopover onClose={() => setLoginOpen(false)} />}
        </div>
      </div>

      {report.usedSavedData && (
        <div role="status" className={styles.notice}>
          <TriangleAlert size={20} aria-hidden />
          <div>
            <strong>Showing saved data.</strong>{" "}
            {report.notice ??
              "FINN blocks automated access. The report uses saved data for this listing."}
          </div>
        </div>
      )}
    </section>
  );
}
