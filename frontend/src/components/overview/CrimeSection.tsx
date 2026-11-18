import { Info, ShieldAlert } from "lucide-react";
import type { CategoryResult, CrimeData } from "../../types/report.js";
import { cx } from "../../lib/cx.js";
import { SectionPending, SectionUnavailable } from "./CategoryState.js";
import { CrimeChart } from "./CrimeChart.js";
import ui from "../../style/ui.module.css";
import styles from "./CrimeSection.module.css";

export function CrimeSection({
  result,
}: {
  result: CategoryResult<CrimeData>;
}) {
  const data = result.status === "AVAILABLE" ? result.data : undefined;
  const area = data?.bydel ?? "this district";

  return (
    <section
      aria-labelledby="crime-title"
      className={cx(ui.card, styles.crime)}
    >
      <div className={styles.sectionHead}>
        <div className={styles.sectionTitle}>
          <span className={cx(ui.iconTile, ui.crimeTile)}>
            <ShieldAlert size={22} aria-hidden />
          </span>
          <div>
            <h2 id="crime-title" className={ui.h2}>
              Safety &amp; crime
            </h2>
            <p className={styles.sectionSub}>
              Reported crime in {area} compared with Oslo
              {data?.range ? `, ${data.range}` : ""}
            </p>
          </div>
        </div>
        {data?.dataSource === "MOCK" && (
          <span className={ui.sampleTag}>Sample data</span>
        )}
      </div>

      {data && <CrimeContent data={data} />}

      {result.status === "UNAVAILABLE" && (
        <SectionUnavailable message="We couldn't load crime data for this location right now." />
      )}
      {result.status === "PENDING" && <SectionPending lines={4} />}
    </section>
  );
}

function CrimeContent({ data }: { data: CrimeData }) {
  return (
    <>
      {data.history.length > 0 ? (
        <CrimeChart
          history={data.history}
          average={data.average}
          range={data.range}
          bydel={data.bydel}
        />
      ) : (
        <p className={styles.sectionSub}>No crime history available.</p>
      )}

      <div className={styles.sources}>
        <Info size={16} aria-hidden />
        <span>
          Source: Oslo kommune statistikkbank (KRI002, BEF005). Reported crimes
          by place of crime.
        </span>
      </div>
    </>
  );
}
