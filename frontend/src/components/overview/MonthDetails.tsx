import type { MonthlyClimate } from "../../types/report.js";
import { formatNumber } from "../../lib/format.js";
import { METRICS, monthName, type Metric } from "./climateChartData.js";
import styles from "./MonthlyClimateChart.module.css";

type MonthDetailsProps = {
  hover: number | null;
  month: MonthlyClimate | null;
  reference: MonthlyClimate | null;
  metric: Metric;
  yearLabel: string;
};

export function MonthDetails({
  hover,
  month,
  reference,
  metric,
  yearLabel,
}: MonthDetailsProps) {
  const info = METRICS[metric];

  if (hover == null) {
    return (
      <div className={styles.details} aria-live="polite">
        <span className={styles.muted}>
          Hover or tap a month to see details
        </span>
      </div>
    );
  }

  return (
    <div className={styles.details} aria-live="polite">
      <span className={styles.detailsTitle}>
        {monthName(hover)} · {yearLabel}
      </span>
      {month ? (
        <>
          <Detail
            name="Avg"
            value={`${formatNumber(month.avgTempC, 1)} °C`}
            strong={metric === "temp"}
          />
          <Detail
            name="Min/max"
            value={`${formatNumber(month.avgMinTempC, 1)}° / ${formatNumber(month.avgMaxTempC, 1)}°`}
          />
          <Detail
            name="Precipitation"
            value={`${formatNumber(month.precipitationMm)} mm`}
            strong={metric === "precip"}
          />
          <Detail
            name="Sun (est.)"
            value={`${formatNumber(month.sunshineHours)} h`}
            strong={metric === "sun"}
          />
          <Detail
            name="Snow"
            value={`${formatNumber(month.snowfallCm)} cm`}
            strong={metric === "snow"}
          />
          {reference && (
            <Detail
              name="5-yr avg"
              value={`${formatNumber(reference[info.key], info.decimals)} ${info.unit}`}
            />
          )}
        </>
      ) : (
        <Detail name="Status" value="No data yet" strong />
      )}
    </div>
  );
}

type DetailProps = { name: string; value: string; strong?: boolean };

function Detail({ name, value, strong = false }: DetailProps) {
  return (
    <span className={styles.detail}>
      <span className={styles.muted}>{name}</span>{" "}
      <span className={strong ? styles.strong : undefined}>{value}</span>
    </span>
  );
}
