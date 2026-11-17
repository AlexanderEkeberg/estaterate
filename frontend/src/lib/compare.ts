import type { MonthlyClimate } from "../types/report.js";

export type ClimateYear = {
  sunshineHours: number;
  rainyDays: number;
  frostDays: number;
  julyTempC: number;
};

export function climateYear(months: MonthlyClimate[]): ClimateYear | null {
  const july = months.find((month) => month.month === 7);
  if (months.length !== 12 || !july) return null;
  const sum = (pick: (month: MonthlyClimate) => number) =>
    Math.round(months.reduce((total, month) => total + pick(month), 0));
  return {
    sunshineHours: sum((month) => month.sunshineHours),
    rainyDays: sum((month) => month.rainyDays),
    frostDays: sum((month) => month.frostDays),
    julyTempC: Math.round(july.avgTempC * 10) / 10,
  };
}
