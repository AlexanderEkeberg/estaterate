import type { Analysis, CategoryResult as ApiCategory } from "./analysisApi.js";
import type { ScrapeResult } from "./propertyApi.js";
import type {
  CategoryResult,
  CrimeData,
  ForecastDay,
  MonthlyClimate,
  PropertyReport,
  SchoolData,
  WeatherData,
} from "../types/report.js";

type Json = Record<string, unknown>;

const isObject = (value: unknown): value is Json =>
  typeof value === "object" && value !== null && !Array.isArray(value);

const isNumber = (value: unknown): value is number =>
  typeof value === "number" && Number.isFinite(value);

const isText = (value: unknown): value is string =>
  typeof value === "string" && value.trim() !== "";

const toScore = (value: number) =>
  Math.round(Math.min(100, Math.max(0, value)));

function toCategory<T>(
  api: ApiCategory | undefined,
  timedOut: boolean,
  parse: (data: unknown, score: number | null) => T | null,
): CategoryResult<T> {
  if (!api || api.status === "PENDING") {
    return { status: timedOut ? "UNAVAILABLE" : "PENDING" };
  }
  if (api.status !== "SUCCESS") {
    return { status: "UNAVAILABLE" };
  }
  const data = parse(api.data, api.score);
  return data ? { status: "AVAILABLE", data } : { status: "UNAVAILABLE" };
}

const CLIMATE_FIELDS = [
  "avgTempC",
  "avgMaxTempC",
  "avgMinTempC",
  "precipitationMm",
  "rainyDays",
  "sunshineHours",
  "snowfallCm",
  "frostDays",
  "avgMaxWindKmh",
] as const;

function parseClimate(value: unknown): MonthlyClimate | null {
  if (!isObject(value) || !isNumber(value.month)) return null;
  if (value.month < 1 || value.month > 12) return null;
  if (!CLIMATE_FIELDS.every((field) => isNumber(value[field]))) return null;
  const v = value as Record<(typeof CLIMATE_FIELDS)[number], number>;
  return {
    month: value.month,
    avgTempC: v.avgTempC,
    avgMaxTempC: v.avgMaxTempC,
    avgMinTempC: v.avgMinTempC,
    precipitationMm: v.precipitationMm,
    rainyDays: v.rainyDays,
    sunshineHours: v.sunshineHours,
    snowfallCm: v.snowfallCm,
    frostDays: v.frostDays,
    avgMaxWindKmh: v.avgMaxWindKmh,
  };
}

function parseForecastDay(value: unknown): ForecastDay | null {
  if (!isObject(value)) return null;
  const { date, symbol, maxTempC, minTempC, precipitationMm } = value;
  if (!isText(date) || !isText(symbol)) return null;
  if (
    !isNumber(maxTempC) ||
    !isNumber(minTempC) ||
    !isNumber(precipitationMm)
  ) {
    return null;
  }
  return { date, symbol, maxTempC, minTempC, precipitationMm };
}

const asList = (value: unknown): unknown[] =>
  Array.isArray(value) ? value : [];

function notNull<T>(value: T | null): value is T {
  return value !== null;
}

function parseWeather(data: unknown): WeatherData | null {
  if (!isObject(data)) return null;

  const forecast = asList(data.forecast).map(parseForecastDay).filter(notNull);
  const fiveYearAverage = asList(data.monthlyAverages)
    .map(parseClimate)
    .filter(notNull)
    .sort((a, b) => a.month - b.month);

  const byYear: Record<string, MonthlyClimate[]> = {};
  for (const row of asList(data.monthly)) {
    if (!isObject(row) || row.complete !== true || !isNumber(row.year))
      continue;
    const month = parseClimate(row);
    if (!month) continue;
    (byYear[String(row.year)] ??= []).push(month);
  }
  for (const months of Object.values(byYear)) {
    months.sort((a, b) => a.month - b.month);
  }

  if (forecast.length === 0 && fiveYearAverage.length === 0) return null;

  const sources = isObject(data.sources) ? data.sources : {};
  const usesMock = sources.history === "MOCK" || sources.forecast === "MOCK";

  const fullYears = Object.keys(byYear)
    .filter((year) => byYear[year]?.length === 12)
    .sort()
    .slice(-5);
  const first = fullYears[0];
  const last = fullYears[fullYears.length - 1];

  const weather: WeatherData = {
    dataSource: usesMock ? "MOCK" : "LIVE",
    forecast,
    fiveYearAverage,
    byYear,
  };
  if (first && last) {
    weather.fiveYearRange = first === last ? first : `${first}–${last}`;
  }
  return weather;
}

function parseScore(data: unknown, score: number | null) {
  const value = score ?? (isObject(data) ? data.score : null);
  return isNumber(value) ? { score: toScore(value) } : null;
}

export function toPropertyReport(
  analysis: Analysis,
  scrapeResult: ScrapeResult | null,
  timedOut: boolean,
): PropertyReport {
  const categories = analysis.categories;
  const report: PropertyReport = {
    id: analysis.analysisId,
    usedSavedData: scrapeResult?.coordinatesSource === "MOCK",
    weather: toCategory(categories.WEATHER, timedOut, parseWeather),
    crime: toCategory<CrimeData>(categories.CRIME, timedOut, parseScore),
    schools: toCategory<SchoolData>(categories.SCHOOL, timedOut, parseScore),
  };
  if (report.usedSavedData && isText(scrapeResult?.message)) {
    report.notice = scrapeResult.message;
  }
  return report;
}
