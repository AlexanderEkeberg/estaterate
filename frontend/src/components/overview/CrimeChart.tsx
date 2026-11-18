import { useState } from "react";
import type { CrimeAverage, CrimeYear } from "../../types/report.js";
import { formatNumber } from "../../lib/format.js";
import { cx } from "../../lib/cx.js";
import ui from "../../style/ui.module.css";
import {
  AVERAGE,
  buildCrimeView,
  HEIGHT,
  labelTransform,
  WIDTH,
  xPct,
} from "./crimeChartData.js";
import styles from "./MonthlyClimateChart.module.css";

type ChartProps = {
  history: CrimeYear[];
  average: CrimeAverage | undefined;
  range: string | undefined;
  bydel: string | undefined;
};

const ordinal = (n: number) => {
  const tens = n % 100;
  if (tens >= 11 && tens <= 13) return `${n}th`;
  if (n % 10 === 1) return `${n}st`;
  if (n % 10 === 2) return `${n}nd`;
  if (n % 10 === 3) return `${n}rd`;
  return `${n}th`;
};

export function CrimeChart({ history, average, range, bydel }: ChartProps) {
  const lastYear = history[history.length - 1];
  const [selected, setSelected] = useState(
    lastYear ? String(lastYear.year) : AVERAGE,
  );
  const [hover, setHover] = useState<number | null>(null);

  if (!lastYear) return null;

  const count = history.length;
  const isAverage = selected === AVERAGE;
  const selectedIndex = history.findIndex((y) => String(y.year) === selected);
  const activeIndex = hover ?? (selectedIndex >= 0 ? selectedIndex : null);
  const active = activeIndex == null ? null : (history[activeIndex] ?? null);
  const view = buildCrimeView(history, average, isAverage);
  const name = bydel ?? "This district";
  const averageLabel = `${range ?? "All years"} average`;
  const averageReports =
    history.reduce((sum, y) => sum + y.crimeCount, 0) / count;
  const averageResidents =
    history.reduce((sum, y) => sum + y.population, 0) / count;

  return (
    <div className={styles.chart}>
      <div className={styles.top}>
        <div>
          <h3 className={ui.h3}>Crime over time</h3>
          <p className={styles.subtitle}>
            Reported crimes per 1,000 residents in {name} compared with the Oslo
            average
          </p>
        </div>
      </div>

      <div role="group" aria-label="Year" className={styles.years}>
        {[AVERAGE, ...history.map((y) => String(y.year))].map((key) => (
          <button
            key={key}
            type="button"
            className={styles.year}
            aria-pressed={selected === key}
            onClick={() => setSelected(key)}
          >
            {key === AVERAGE ? averageLabel : key}
          </button>
        ))}
      </div>

      <ul className={styles.legend}>
        <li>
          <span
            className={styles.swLine}
            style={{ background: "var(--er-crime)" }}
          />
          {name}
        </li>
        <li>
          <span className={styles.swRef} />
          Oslo average
        </li>
        {isAverage && average && (
          <li>
            <span
              className={styles.swLine}
              style={{ background: "var(--er-navy)" }}
            />
            {name} {averageLabel.toLowerCase()} ·{" "}
            {formatNumber(average.ratePer1000, 1)}
          </li>
        )}
      </ul>

      <div className={styles.details} aria-live="polite">
        {active ? (
          <>
            <span className={styles.detailsTitle}>
              {name} · {active.year}
            </span>
            <Detail name="Reports" value={formatNumber(active.crimeCount)} />
            <Detail name="Residents" value={formatNumber(active.population)} />
            <Detail
              name="Rank"
              value={`${ordinal(active.rank)} safest of ${active.districtCount}`}
            />
          </>
        ) : average ? (
          <>
            <span className={styles.detailsTitle}>
              {name} · {averageLabel}
            </span>
            <Detail
              name="Reports per year"
              value={formatNumber(averageReports)}
            />
            <Detail name="Residents" value={formatNumber(averageResidents)} />
          </>
        ) : (
          <span className={styles.muted}>
            Hover or tap a year to see details
          </span>
        )}
      </div>

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
                className={styles.gridline}
                style={{ bottom: `${tick.pct}%` }}
              />
            ))}

            {activeIndex != null && (
              <div
                className={styles.highlight}
                style={{
                  left: `${(activeIndex / count) * 100 + 0.6}%`,
                  width: `${100 / count - 1.2}%`,
                }}
              />
            )}

            <svg
              className={styles.svg}
              viewBox={`0 0 ${WIDTH} ${HEIGHT}`}
              preserveAspectRatio="none"
              aria-hidden
            >
              {view.averageY != null && (
                <line
                  x1={0}
                  x2={WIDTH}
                  y1={view.averageY}
                  y2={view.averageY}
                  stroke="var(--er-navy)"
                  strokeWidth={2}
                  vectorEffect="non-scaling-stroke"
                />
              )}
              <polyline
                points={view.referenceLine}
                fill="none"
                stroke="#9aa1b8"
                strokeWidth={2}
                strokeDasharray="6 6"
                strokeLinejoin="round"
                vectorEffect="non-scaling-stroke"
              />
              <polyline
                points={view.line}
                fill="none"
                stroke="var(--er-crime)"
                strokeWidth={3}
                strokeLinejoin="round"
                strokeLinecap="round"
                vectorEffect="non-scaling-stroke"
              />
            </svg>

            {view.labels
              .filter((label) => label.index !== activeIndex)
              .map((label) => (
                <span
                  key={`${label.index}-${label.placement}`}
                  className={styles.label}
                  style={{
                    left: `${xPct(label.index, count)}%`,
                    bottom: `${label.bottom}%`,
                    transform: labelTransform(label, count),
                  }}
                >
                  {label.text}
                </span>
              ))}

            {active && activeIndex != null && (
              <div
                className={styles.dot}
                style={{
                  left: `${xPct(activeIndex, count)}%`,
                  bottom: `${view.pct(active.ratePer1000)}%`,
                  background: "var(--er-crime)",
                  boxShadow: "0 0 0 1.5px var(--er-crime)",
                }}
              />
            )}

            <div className={styles.layer}>
              {history.map((y, index) => (
                <button
                  key={y.year}
                  type="button"
                  className={styles.hit}
                  onMouseEnter={() => setHover(index)}
                  onFocus={() => setHover(index)}
                  onClick={() => setSelected(String(y.year))}
                  aria-label={`${y.year}: ${formatNumber(y.ratePer1000, 1)} reported crimes per 1,000 residents, Oslo ${formatNumber(y.osloRatePer1000, 1)}`}
                />
              ))}
            </div>
          </div>

          <div className={styles.months} aria-hidden>
            {history.map((y, index) => (
              <span
                key={y.year}
                className={cx(
                  styles.month,
                  activeIndex === index && styles.monthActive,
                )}
              >
                <span className={styles.monthLong}>{y.year}</span>
                <span className={styles.monthShort}>
                  ’{String(y.year).slice(2)}
                </span>
              </span>
            ))}
          </div>
        </div>
      </div>
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
