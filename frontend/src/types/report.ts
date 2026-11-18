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

export type CrimeYear = {
  year: number;
  crimeCount: number;
  population: number;
  ratePer1000: number;
  osloRatePer1000: number;
  rank: number;
  districtCount: number;
};

export type CrimeAverage = {
  ratePer1000: number;
  osloRatePer1000: number;
};

export type CrimeData = {
  dataSource: "LIVE" | "MOCK";
  score: number;
  ratePer1000?: number;
  bydel?: string;
  history: CrimeYear[];
  average?: CrimeAverage;
  range?: string;
};

export type SchoolData = {
  score: number;
  nearestSchool?: string;
  distanceKm?: number;
  dataSource?: "LIVE" | "MOCK";
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
