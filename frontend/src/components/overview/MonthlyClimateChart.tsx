import { useState } from "react";
import { Info } from "lucide-react";
import type { MonthlyClimate } from "../../types/report.js";
import { formatNumber, MONTH_LABELS } from "../../lib/format.js";
import { cx } from "../../lib/cx.js";
import ui from "../../style/ui.module.css";
import {
  AVERAGE,
  buildView,
  HEIGHT,
  labelTransform,
  METRIC_IDS,
  METRICS,
  monthName,
  toSlots,
  WIDTH,
  xPct,
  type Metric,
} from "./climateChartData.js";
import { MonthDetails } from "./MonthDetails.js";
import styles from "./MonthlyClimateChart.module.css";

type ChartProps = {
  fiveYearAverage: MonthlyClimate[];
  byYear: Record<string, MonthlyClimate[]>;
  fiveYearRange: string | undefined;
};

export function MonthlyClimateChart({
  fiveYearAverage,
  byYear,
  fiveYearRange,
}: ChartProps) {
  const [year, setYear] = useState(AVERAGE);
  const [metric, setMetric] = useState<Metric>("temp");
  const [hover, setHover] = useState<number | null>(null);

  const isAverage = year === AVERAGE;
  const average = toSlots(fiveYearAverage);
  const slots = isAverage ? average : toSlots(byYear[year] ?? []);
  const info = METRICS[metric];
  const view = buildView(metric, slots, isAverage ? [] : average);

  const missing = MONTH_LABELS.filter((_, index) => !slots[index]);
  const isPartial = !isAverage && missing.length > 0;
  const yearLabel = isAverage ? "5-year average" : year;
  const hovered = hover == null ? null : (slots[hover] ?? null);

  const subtitle = isAverage
    ? `Average of ${fiveYearRange ?? "the last 5 years"}`
    : `${year} compared with the 5-year average`;
  const showTotal = metric === "precip" || metric === "snow";
  const legend = !showTotal
    ? `${info.legend} · ${yearLabel}`
    : `${info.legend} · ${yearLabel} · ${isPartial ? "so far" : "total"} ${formatNumber(view.total)} ${info.unit}`;

  return (
    <div className={styles.chart}>
      <div className={styles.top}>
        <div>
          <h3 className={ui.h3}>Monthly climate</h3>
          <p className={styles.subtitle}>{subtitle}</p>
        </div>
        <div role="group" aria-label="Metric" className={styles.segmented}>
          {METRIC_IDS.map((id) => (
            <button
              key={id}
              type="button"
              className={styles.segment}
              aria-pressed={metric === id}
              onClick={() => setMetric(id)}
            >
              {METRICS[id].label}
            </button>
          ))}
        </div>
      </div>

      <div role="group" aria-label="Year" className={styles.years}>
        {[AVERAGE, ...Object.keys(byYear).sort()].map((key) => (
          <button
            key={key}
            type="button"
            className={styles.year}
            aria-pressed={year === key}
            onClick={() => setYear(key)}
          >
            {key === AVERAGE ? "5-year average" : key}
          </button>
        ))}
      </div>

      <ul className={styles.legend}>
        <li>
          <span className={styles.swLine} style={{ background: info.color }} />
          {legend}
        </li>
        {!isAverage && (
          <li>
            <span className={styles.swRef} />
            5-year average
          </li>
        )}
        {isPartial && (
          <li className={styles.partialNote}>
            <Info size={14} aria-hidden />
            Partial year – {missing.join(", ")} not available yet
          </li>
        )}
      </ul>

      <MonthDetails
        hover={hover}
        month={hovered}
        reference={hover == null || isAverage ? null : (average[hover] ?? null)}
        metric={metric}
        yearLabel={yearLabel}
      />

      <div className={styles.grid}>
        <div className={styles.axis} aria-hidden>
          {view.ticks.map((tick) => (
            <span
              key={tick.value}
              className={styles.tick}
              style={{ bottom: `${tick.pct}%` }}
            >
              {tick.label}
            </span>
          ))}
        </div>

        <div className={styles.plotCol}>
          <div className={styles.plot} onMouseLeave={() => setHover(null)}>
            {view.ticks.map((tick) => (
              <div
                key={tick.value}
                className={cx(
                  styles.gridline,
                  metric === "temp" && tick.value === 0 && styles.zeroLine,
                )}
                style={{ bottom: `${tick.pct}%` }}
              />
            ))}

            {hover != null && (
              <div
                className={styles.highlight}
                style={{
                  left: `${(hover / 12) * 100 + 0.6}%`,
                  width: `${100 / 12 - 1.2}%`,
                }}
              />
            )}

            <div className={styles.layer}>
              {slots.map((month, index) => (
                <div key={index} className={styles.barCell}>
                  {!month && <div className={styles.missing} />}
                  {month && metric !== "temp" && (
                    <div
                      className={styles.bar}
                      style={{
                        height: `${view.pct(month[info.key])}%`,
                        background:
                          hover != null && hover !== index
                            ? info.soft
                            : info.color,
                      }}
                    />
                  )}
                </div>
              ))}
            </div>

            <svg
              className={styles.svg}
              viewBox={`0 0 ${WIDTH} ${HEIGHT}`}
              preserveAspectRatio="none"
              aria-hidden
            >
              {view.referenceLine && (
                <polyline
                  points={view.referenceLine}
                  fill="none"
                  stroke="#9aa1b8"
                  strokeWidth={2}
                  strokeDasharray="6 6"
                  strokeLinejoin="round"
                  vectorEffect="non-scaling-stroke"
                />
              )}
              {view.line && (
                <polyline
                  points={view.line}
                  fill="none"
                  stroke={info.color}
                  strokeWidth={3}
                  strokeLinejoin="round"
                  strokeLinecap="round"
                  vectorEffect="non-scaling-stroke"
                />
              )}
            </svg>

            {view.labels
              .filter((label) => label.index !== hover)
              .map((label) => (
                <span
                  key={`${label.index}-${label.placement}`}
                  className={styles.label}
                  style={{
                    left: `${xPct(label.index)}%`,
                    bottom: `${label.bottom}%`,
                    transform: labelTransform(label),
                  }}
                >
                  {label.text}
                </span>
              ))}

            {metric === "temp" && hover != null && hovered && (
              <div
                className={styles.dot}
                style={{
                  left: `${xPct(hover)}%`,
                  bottom: `${view.pct(hovered.avgTempC)}%`,
                }}
              />
            )}

            <div className={styles.layer}>
              {slots.map((month, index) => (
                <button
                  key={index}
                  type="button"
                  className={styles.hit}
                  onMouseEnter={() => setHover(index)}
                  onFocus={() => setHover(index)}
                  onClick={() => setHover(index)}
                  aria-label={
                    month
                      ? `${monthName(index)}: average ${formatNumber(month.avgTempC, 1)} °C, ${formatNumber(month.precipitationMm)} mm precipitation`
                      : `${monthName(index)}: no data yet`
                  }
                />
              ))}
            </div>
          </div>

          <div className={styles.months} aria-hidden>
            {MONTH_LABELS.map((label, index) => (
              <span
                key={label}
                className={cx(
                  styles.month,
                  hover === index && styles.monthActive,
                )}
              >
                <span className={styles.monthLong}>{label}</span>
                <span className={styles.monthShort}>{label.charAt(0)}</span>
              </span>
            ))}
          </div>
        </div>
      </div>

      {metric === "sun" && (
        <p className={styles.note}>
          Sunshine is estimated from modelled solar radiation. Use it to compare
          months and years, not as exact hours.
        </p>
      )}
    </div>
  );
}
