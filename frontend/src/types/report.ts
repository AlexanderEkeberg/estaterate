export type CategoryStatus = "AVAILABLE" | "UNAVAILABLE" | "PENDING";

export type CategoryResult<T> = {
  status: CategoryStatus;
  data?: T;
};

export type ForecastDay = {
  date: string;
  symbol: string;
  maxTempC: number;
  minTempC: number;
  precipitationMm: number;
};

export type MonthlyClimate = {
  month: number;
  avgTempC: number;
  avgMaxTempC: number;
  avgMinTempC: number;
  precipitationMm: number;
  rainyDays: number;
  sunshineHours: number;
  snowfallCm: number;
  frostDays: number;
  avgMaxWindKmh: number;
};

export type WeatherData = {
  dataSource: "LIVE" | "MOCK";
  forecast: ForecastDay[];
  fiveYearAverage: MonthlyClimate[];
  byYear: Record<string, MonthlyClimate[]>;
  fiveYearRange?: string;
};

// TODO: add the crime fields when crimerate sends its ScoreEvent data
export type CrimeData = {
  score: number;
};

// TODO: add the school fields when school sends its ScoreEvent data
export type SchoolData = {
  score: number;
};

export type PropertyReport = {
  id: string;
  address?: string;
  askingPriceNok?: number;
  sizeM2?: number;
  sizeType?: string;
  usedSavedData: boolean;
  notice?: string;
  weather: CategoryResult<WeatherData>;
  crime: CategoryResult<CrimeData>;
  schools: CategoryResult<SchoolData>;
};
