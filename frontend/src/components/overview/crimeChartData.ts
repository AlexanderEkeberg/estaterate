import type { CrimeAverage, CrimeYear } from "../../types/report.js";
import { formatNumber } from "../../lib/format.js";

export const AVERAGE = "average";
export const WIDTH = 1200;
export const HEIGHT = 240;

type Point = { index: number; value: number };

type Tick = { value: number; pct: number; label: string };

export type CrimeLabel = {
  index: number;
  text: string;
  bottom: number;
  placement: "above" | "below";
};

export const xPct = (index: number, count: number) =>
  ((index + 0.5) / count) * 100;

const xUnits = (index: number, count: number) =>
  (WIDTH / count) * (index + 0.5);

function valueRange(values: number[]) {
  const min = Math.min(...values);
  const max = Math.max(...values);
  const span = Math.max(max - min, 10);
  const step = span > 40 ? 20 : 10;
  const low = Math.max(0, Math.floor((min - span * 0.4) / step) * step);
  const high = low + Math.ceil((max + span * 0.3 - low) / step) * step;
  return { low, high, step };
}

export function buildCrimeView(
  history: CrimeYear[],
  average: CrimeAverage | undefined,
  showAverage: boolean,
) {
  const count = history.length;
  const own: Point[] = history.map((y, index) => ({
    index,
    value: y.ratePer1000,
  }));
  const oslo: Point[] = history.map((y, index) => ({
    index,
    value: y.osloRatePer1000,
  }));

  const values = [...own, ...oslo].map((p) => p.value);
  const averageValue = showAverage ? average?.ratePer1000 : undefined;
  if (averageValue !== undefined) values.push(averageValue);

  const { low, high, step } = valueRange(values.length ? values : [0]);
  const pct = (value: number) => ((value - low) / (high - low)) * 100;
  const y = (value: number) =>
    (HEIGHT - (pct(value) / 100) * HEIGHT).toFixed(1);
  const coord = (p: Point) => `${xUnits(p.index, count)},${y(p.value)}`;

  const ticks: Tick[] = [];
  for (let t = low; t <= high; t += step) {
    ticks.push({ value: t, pct: pct(t), label: formatNumber(t) });
  }

  const labels: CrimeLabel[] = [];
  const first = own[0];
  if (first) {
    const toLabel = (
      p: Point,
      placement: CrimeLabel["placement"],
    ): CrimeLabel => ({
      index: p.index,
      bottom: pct(p.value),
      placement,
      text: formatNumber(p.value, 1),
    });
    const highest = own.reduce((a, p) => (p.value > a.value ? p : a), first);
    const lowest = own.reduce((a, p) => (p.value < a.value ? p : a), first);
    labels.push(toLabel(highest, "above"));
    if (lowest.index !== highest.index) labels.push(toLabel(lowest, "below"));
  }

  return {
    pct,
    ticks,
    labels,
    line: own.map(coord).join(" "),
    referenceLine: oslo.map(coord).join(" "),
    averageY: averageValue === undefined ? null : y(averageValue),
  };
}

export function labelTransform(label: CrimeLabel, count: number) {
  const x =
    label.index === 0
      ? "-12px"
      : label.index === count - 1
        ? "calc(-100% + 12px)"
        : "-50%";
  const y = label.placement === "above" ? "-10px" : "calc(100% + 10px)";
  return `translate(${x}, ${y})`;
}
