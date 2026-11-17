import { Fragment, useEffect, useState } from "react";
import { ArrowLeft, Columns2, Plus, X } from "lucide-react";
import { useAuth } from "../auth/AuthProvider.js";
import { getComparedAnalysis } from "../api/compareApi.js";
import { toPropertyReport } from "../api/reportMapper.js";
import { Navbar } from "../components/layout/Navbar.js";
import { AppLink } from "../components/ui/AppLink.js";
import { Message } from "../components/ui/Message.js";
import {
  MAX_COMPARE,
  isCompareId,
  useCompare,
} from "../components/compare/CompareContext.js";
import { navigate } from "../main/router.js";
import { climateYear, type ClimateYear } from "../lib/compare.js";
import { formatNok, formatNumber, splitAddress } from "../lib/format.js";
import { computeTotalScore } from "../lib/score.js";
import { cx } from "../lib/cx.js";
import type { PropertyReport } from "../types/report.js";
import ui from "../style/ui.module.css";
import styles from "./ComparePage.module.css";

type Row = {
  label: string;
  value: (report: PropertyReport) => number | null;
  format: (value: number) => string;
  sub?: (report: PropertyReport) => string | null;
};

const outOf100 = (value: number) => `${value} / 100`;

const climate = (report: PropertyReport): ClimateYear | null =>
  report.weather.data ? climateYear(report.weather.data.fiveYearAverage) : null;

const climateRow = (
  label: string,
  pick: (year: ClimateYear) => number,
  format: (value: number) => string,
): Row => ({
  label,
  format,
  value: (report) => {
    const year = climate(report);
    return year ? pick(year) : null;
  },
});

const GROUPS: { title: string; rows: Row[] }[] = [
  {
    title: "Scores",
    rows: [
      {
        label: "Total score",
        value: (report) => computeTotalScore(report).total,
        format: outOf100,
      },
      {
        label: "Safety & crime",
        value: (report) => report.crime.data?.score ?? null,
        format: outOf100,
        sub: (report) => {
          const rate = report.crime.data?.ratePer1000;
          return rate === undefined
            ? null
            : `${formatNumber(rate, 1)} crimes per 1 000`;
        },
      },
      {
        label: "Schools",
        value: (report) => report.schools.data?.score ?? null,
        format: outOf100,
        sub: (report) => {
          const school = report.schools.data;
          return school?.nearestSchool && school.distanceKm !== undefined
            ? `${school.nearestSchool} · ${formatNumber(school.distanceKm, 1)} km`
            : null;
        },
      },
    ],
  },
  {
    title: "Weather · 5-year average",
    rows: [
      climateRow(
        "Sunshine hours / year (est.)",
        (year) => year.sunshineHours,
        (value) => `${formatNumber(value)} h`,
      ),
      climateRow(
        "Rainy days / year",
        (year) => year.rainyDays,
        (value) => `${formatNumber(value)} days`,
      ),
      climateRow(
        "Frost days / year",
        (year) => year.frostDays,
        (value) => `${formatNumber(value)} days`,
      ),
      climateRow(
        "Avg July temperature",
        (year) => year.julyTempC,
        (value) => `${formatNumber(value, 1)} °C`,
      ),
    ],
  },
];

function readIds(): string[] {
  const raw = new URLSearchParams(window.location.search).get("ids") ?? "";
  return [...new Set(raw.split(",").filter(isCompareId))].slice(0, MAX_COMPARE);
}

const icon = <Columns2 size={28} aria-hidden />;

type Loaded = Record<string, PropertyReport | "error">;

type HeaderProps = {
  id: string;
  report: PropertyReport | null;
  onRemove: () => void;
};

function PropertyHeader({ id, report, onRemove }: HeaderProps) {
  const [street, postalArea] = splitAddress(report?.address);
  const name = street ?? "Address not available";
  const total = report ? computeTotalScore(report).total : null;
  const facts = [
    report?.askingPriceNok !== undefined && formatNok(report.askingPriceNok),
    report?.sizeM2 !== undefined && `${formatNumber(report.sizeM2)} m²`,
  ]
    .filter(Boolean)
    .join(" · ");

  return (
    <div className={styles.colCard}>
      <div>
        <div className={styles.address}>
          {report ? name : "Report unavailable"}
        </div>
        {postalArea && <div className={styles.small}>{postalArea}</div>}
      </div>
      {facts && <div className={styles.facts}>{facts}</div>}
      {report && (
        <div className={styles.total}>
          <span className={styles.totalValue}>{total ?? "–"}</span>
          <span className={styles.small}>/ 100</span>
        </div>
      )}
      <div className={styles.colActions}>
        {report && (
          <AppLink
            to={`/analysis/${encodeURIComponent(id)}?saved`}
            className={cx(ui.btn, ui.outlineBlue, ui.sm)}
          >
            Open report
          </AppLink>
        )}
        <button
          type="button"
          className={cx(ui.btn, ui.ghost, ui.sm)}
          onClick={onRemove}
          aria-label={`Remove ${report ? name : "property"} from comparison`}
        >
          <X size={16} aria-hidden />
          Remove
        </button>
      </div>
    </div>
  );
}

function Content() {
  const { status, login } = useAuth();
  const compare = useCompare();
  const [ids, setIds] = useState(readIds);
  const [loaded, setLoaded] = useState<Loaded>({});
  const idsKey = ids.join(",");

  useEffect(() => {
    if (status !== "authenticated") return;
    let cancelled = false;
    for (const id of idsKey.split(",").filter(Boolean)) {
      getComparedAnalysis(id)
        .then((analysis) => toPropertyReport(analysis, null, true))
        .then(
          (report) => {
            if (!cancelled)
              setLoaded((current) => ({ ...current, [id]: report }));
          },
          (e: unknown) => {
            if (cancelled) return;
            console.error(e);
            setLoaded((current) => ({ ...current, [id]: "error" }));
          },
        );
    }
    return () => {
      cancelled = true;
    };
  }, [status, idsKey]);

  function remove(id: string) {
    const next = ids.filter((other) => other !== id);
    setIds(next);
    compare.remove(id);
    window.history.replaceState(
      null,
      "",
      next.length ? `/compare?ids=${next.join(",")}` : "/compare",
    );
  }

  if (status === "loading") {
    return null;
  }
  if (status === "unavailable") {
    return (
      <Message
        icon={icon}
        title="Login is unavailable"
        text="We could not reach the login service. Try again later."
      />
    );
  }
  if (status === "anonymous") {
    return (
      <Message
        icon={icon}
        title="Log in to compare properties"
        text="Comparing is available for logged-in users."
        action={{ label: "Log in", onClick: login }}
      />
    );
  }
  if (ids.length === 0) {
    return (
      <Message
        icon={icon}
        title="Nothing to compare yet"
        text={`Tick Compare on ${MAX_COMPARE} of your favourites to see them side by side.`}
        action={{
          label: "Go to favourites",
          onClick: () => navigate("/favourites"),
        }}
      />
    );
  }
  if (ids.some((id) => !(id in loaded))) {
    return (
      <p role="status" className={styles.small}>
        Loading comparison…
      </p>
    );
  }

  const slots = Array.from({ length: MAX_COMPARE }, (_, i) => ids[i] ?? null);
  const reportFor = (id: string | null) => {
    const entry = id ? loaded[id] : undefined;
    return entry && entry !== "error" ? entry : null;
  };
  const reports = slots.map(reportFor);

  return (
    <>
      <div className={cx(ui.card, styles.tableCard)}>
        <div role="table" aria-label="Property comparison">
          <div role="row" className={styles.row}>
            <div
              role="columnheader"
              className={cx(styles.labelCell, styles.small)}
            >
              Property
            </div>
            {slots.map((id, i) => (
              <div
                role="columnheader"
                key={id ?? `empty-${i}`}
                className={styles.headCell}
              >
                {id ? (
                  <PropertyHeader
                    id={id}
                    report={reports[i] ?? null}
                    onRemove={() => remove(id)}
                  />
                ) : (
                  <AppLink to="/favourites" className={styles.emptyCol}>
                    <Plus size={22} aria-hidden />
                    Add from favourites
                  </AppLink>
                )}
              </div>
            ))}
          </div>

          {GROUPS.map((group) => (
            <Fragment key={group.title}>
              <div role="row" className={styles.group}>
                <span role="rowheader">{group.title}</span>
              </div>
              {group.rows.map((row) => {
                return (
                  <div
                    role="row"
                    key={row.label}
                    className={cx(styles.row, styles.dataRow)}
                  >
                    <div role="rowheader" className={styles.labelCell}>
                      {row.label}
                    </div>
                    {reports.map((report, i) => {
                      const value = report ? row.value(report) : null;
                      const sub = report && row.sub?.(report);
                      return (
                        <div
                          role="cell"
                          key={slots[i] ?? `empty-${i}`}
                          className={styles.cell}
                        >
                          {report && (
                            <>
                              <div className={styles.value}>
                                {value === null ? "–" : row.format(value)}
                              </div>
                              {sub && (
                                <span className={styles.small}>{sub}</span>
                              )}
                            </>
                          )}
                        </div>
                      );
                    })}
                  </div>
                );
              })}
            </Fragment>
          ))}
        </div>
      </div>
    </>
  );
}

export function ComparePage() {
  return (
    <div className={styles.page}>
      <Navbar />
      <main className={ui.page}>
        <AppLink to="/favourites" className={styles.back}>
          <ArrowLeft size={16} aria-hidden />
          Back to favourites
        </AppLink>
        <h1 className={ui.h1}>Compare properties</h1>
        <Content />
      </main>
    </div>
  );
}
