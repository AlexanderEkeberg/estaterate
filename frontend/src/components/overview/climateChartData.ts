import type { MonthlyClimate } from "../../types/report.js";
import { formatNumber, MONTH_LABELS, MONTH_NAMES } from "../../lib/format.js";

export type Metric = "temp" | "precip" | "sun" | "snow";
type ValueKey = "avgTempC" | "precipitationMm" | "sunshineHours" | "snowfallCm";
export type Slot = MonthlyClimate | null;

type MetricInfo = {
  label: string;
  legend: string;
  key: ValueKey;
  unit: string;
  decimals: number;
  step: number;
  color: string;
  soft: string;
};

type Tick = { value: number; pct: number; label: string };

type Label = {
  index: number;
  text: string;
  bottom: number;
  placement: "above" | "below";
};

export const AVERAGE = "average";
export const WIDTH = 1200;
export const HEIGHT = 240;

export const METRICS: Record<Metric, MetricInfo> = {
  temp: {
    label: "Temperature",
    legend: "Average temperature",
    key: "avgTempC",
    unit: "°C",
    decimals: 1,
    step: 5,
    color: "#0a5cff",
    soft: "#0a5cff",
  },
  precip: {
    label: "Precipitation",
    legend: "Precipitation",
    key: "precipitationMm",
    unit: "mm",
    decimals: 0,
    step: 50,
    color: "#0a5cff",
    soft: "#b9d0ff",
  },
  sun: {
    label: "Sunshine (est.)",
    legend: "Estimated sunshine",
    key: "sunshineHours",
    unit: "h",
    decimals: 0,
    step: 100,
    color: "#f5a623",
    soft: "#fbd9a0",
  },
  snow: {
    label: "Snow",
    legend: "Snowfall",
    key: "snowfallCm",
    unit: "cm",
    decimals: 0,
    step: 10,
    color: "#6c7bb0",
    soft: "#c3cbe4",
  },
};

export const METRIC_IDS = Object.keys(METRICS) as Metric[];

export const toSlots = (months: MonthlyClimate[]): Slot[] =>
  MONTH_LABELS.map(
    (_, index) => months.find((m) => m.month === index + 1) ?? null,
  );

const niceCeil = (value: number, step: number) =>
  Math.max(step, Math.ceil(value / step) * step);

const xUnits = (index: number) => 50 + 100 * index;
export const xPct = (index: number) => ((index + 0.5) / 12) * 100;
const monthLabel = (index: number) => MONTH_LABELS[index] ?? "";
export const monthName = (index: number) => MONTH_NAMES[index] ?? "";

function valueRange(metric: Metric, values: number[]) {
  const info = METRICS[metric];
  if (metric !== "temp") {
    return { low: 0, high: niceCeil(Math.max(...values, 1) * 1.15, info.step) };
  }
  const min = Math.min(...values);
  const max = Math.max(...values);
  const span = Math.max(max - min, 1);
  return {
    low: Math.floor((min - span * 0.18) / 5) * 5,
    high: Math.ceil((max + span * 0.15) / 5) * 5,
  };
}

export function buildView(metric: Metric, slots: Slot[], reference: Slot[]) {
  const info = METRICS[metric];
  const points = slots.flatMap((month, index) =>
    month ? [{ index, value: month[info.key] }] : [],
  );
  const referencePoints = reference.flatMap((month, index) =>
    month ? [{ index, value: month[info.key] }] : [],
  );

  const { low, high } = valueRange(metric, [
    ...points.map((p) => p.value),
    ...referencePoints.map((p) => p.value),
  ]);
  const pct = (value: number) => ((value - low) / (high - low)) * 100;
  const coord = (p: { index: number; value: number }) =>
    `${xUnits(p.index)},${(HEIGHT - (pct(p.value) / 100) * HEIGHT).toFixed(1)}`;

  const ticks: Tick[] = [];
  if (metric === "temp") {
    const step = high - low > 20 ? 10 : 5;
    for (let t = low; t <= high; t += step) {
      ticks.push({ value: t, pct: pct(t), label: `${formatNumber(t)}°` });
    }
  } else {
    for (const t of [0, high / 2, high]) {
      ticks.push({ value: t, pct: pct(t), label: formatNumber(t) });
    }
  }

  const labels: Label[] = [];
  const first = points[0];
  if (first) {
    const unit = metric === "temp" ? "°" : ` ${info.unit}`;
    const toLabel = (
      p: { index: number; value: number },
      placement: Label["placement"],
    ): Label => ({
      index: p.index,
      bottom: pct(p.value),
      placement,
      text: `${formatNumber(p.value, info.decimals)}${unit} ${monthLabel(p.index)}`,
    });
    const highest = points.reduce((a, p) => (p.value > a.value ? p : a), first);
    labels.push(toLabel(highest, "above"));
    if (metric === "temp") {
      const lowest = points.reduce(
        (a, p) => (p.value < a.value ? p : a),
        first,
      );
      labels.push(toLabel(lowest, "below"));
    }
  }

  return {
    pct,
    ticks,
    labels,
    total: points.reduce((sum, p) => sum + p.value, 0),
    line: metric === "temp" ? points.map(coord).join(" ") : "",
    referenceLine: referencePoints.map(coord).join(" "),
  };
}

export function labelTransform(label: Label) {
  const x =
    label.index <= 1
      ? "-12px"
      : label.index >= 10
        ? "calc(-100% + 12px)"
        : "-50%";
  const y = label.placement === "above" ? "-10px" : "calc(100% + 10px)";
  return `translate(${x}, ${y})`;
}
