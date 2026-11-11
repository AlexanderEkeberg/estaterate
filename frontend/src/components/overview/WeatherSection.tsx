import { Droplet, Info, Sun } from "lucide-react";
import type { CategoryResult, WeatherData } from "../../types/report.js";
import {
  formatNumber,
  formatShortDate,
  formatTemp,
  formatWeekday,
  isToday,
} from "../../lib/format.js";
import { weatherIcon } from "../../lib/weatherIcon.js";
import { cx } from "../../lib/cx.js";
import { SectionPending, SectionUnavailable } from "./CategoryState.js";
import { MonthlyClimateChart } from "./MonthlyClimateChart.js";
import ui from "../../style/ui.module.css";
import styles from "./WeatherSection.module.css";

const TONE_CLASS = {
  sun: styles.toneSun,
  cloud: styles.toneCloud,
  rain: styles.toneRain,
  snow: styles.toneSnow,
};

export function WeatherSection({
  result,
}: {
  result: CategoryResult<WeatherData>;
}) {
  const data = result.status === "AVAILABLE" ? result.data : undefined;

  return (
    <section
      aria-labelledby="weather-title"
      className={cx(ui.card, styles.weather)}
    >
      <div className={styles.sectionHead}>
        <div className={styles.sectionTitle}>
          <span className={cx(ui.iconTile, ui.weatherTile)}>
            <Sun size={22} aria-hidden />
          </span>
          <div>
            <h2 id="weather-title" className={ui.h2}>
              Weather &amp; sunlight
            </h2>
            <p className={styles.sectionSub}>
              10-day forecast and five years of local climate history
            </p>
          </div>
        </div>
        {data?.dataSource === "MOCK" && (
          <span className={ui.sampleTag}>Sample data</span>
        )}
      </div>

      {data && <WeatherContent data={data} />}

      {result.status === "UNAVAILABLE" && (
        <SectionUnavailable message="We couldn't load weather data for this location right now." />
      )}
      {result.status === "PENDING" && <SectionPending lines={4} />}
    </section>
  );
}

function WeatherContent({ data }: { data: WeatherData }) {
  return (
    <>
      {data.forecast.length > 0 && (
        <div className={styles.block}>
          <div className={styles.blockHead}>
            <h3 className={ui.h3}>Next {data.forecast.length} days</h3>
            <span className={styles.blockHint}>
              Max / min °C · precipitation
            </span>
          </div>
          <ol className={styles.forecast}>
            {data.forecast.map((day) => {
              const { Icon, label, tone } = weatherIcon(day.symbol);
              return (
                <li
                  key={day.date}
                  className={cx(styles.day, isToday(day.date) && styles.today)}
                >
                  <div>
                    <div className={styles.dayName}>
                      {formatWeekday(day.date)}
                    </div>
                    <div className={styles.dayDate}>
                      {formatShortDate(day.date)}
                    </div>
                  </div>
                  <Icon
                    size={32}
                    strokeWidth={1.8}
                    className={TONE_CLASS[tone]}
                    role="img"
                    aria-label={label}
                  />
                  <div className={styles.temps}>
                    <span className={styles.tMax}>
                      {formatTemp(day.maxTempC)}
                    </span>{" "}
                    <span className={styles.tMin}>
                      {formatTemp(day.minTempC)}
                    </span>
                  </div>
                  <div
                    className={cx(
                      styles.precip,
                      day.precipitationMm > 0 && styles.precipWet,
                    )}
                  >
                    <Droplet size={12} strokeWidth={2.2} aria-hidden />
                    {formatNumber(day.precipitationMm, 1)} mm
                  </div>
                </li>
              );
            })}
          </ol>
        </div>
      )}

      {data.fiveYearAverage.length > 0 && (
        <div className={styles.climate}>
          <MonthlyClimateChart
            fiveYearAverage={data.fiveYearAverage}
            byYear={data.byYear}
            fiveYearRange={data.fiveYearRange}
          />
        </div>
      )}

      <div className={styles.sources}>
        <Info size={16} aria-hidden />
        <span>Historical data: Open-Meteo · Forecast: Yr (MET Norway)</span>
      </div>
    </>
  );
}
