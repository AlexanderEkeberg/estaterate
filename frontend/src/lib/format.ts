const NBSP = " ";
const MINUS = "−";

const group = (digits: string) => digits.replace(/\B(?=(\d{3})+(?!\d))/g, NBSP);

export function formatNumber(value: number, decimals = 0): string {
  const fixed = Math.abs(value).toFixed(decimals);
  const [int = "", frac] = fixed.split(".");
  const negative = value < 0 && Number(fixed) !== 0;
  return (negative ? MINUS : "") + group(int) + (frac ? "." + frac : "");
}

export const formatNok = (value: number) => `${formatNumber(value)}${NBSP}kr`;

export function splitAddress(
  address: string | null | undefined,
): [string | null, string | null] {
  if (!address) return [null, null];
  const comma = address.lastIndexOf(",");
  return comma === -1
    ? [address, null]
    : [address.slice(0, comma).trim(), address.slice(comma + 1).trim()];
}

export const formatTemp = (value: number) => `${formatNumber(value)}°`;

const parseDate = (iso: string) => new Date(`${iso}T12:00:00`);

export const isToday = (iso: string) =>
  parseDate(iso).toDateString() === new Date().toDateString();

export const formatShortDate = (iso: string) =>
  parseDate(iso).toLocaleDateString("en-GB", {
    day: "numeric",
    month: "short",
  });

export const formatSavedDate = (iso: string) =>
  new Date(iso).toLocaleDateString("en-GB", {
    day: "numeric",
    month: "short",
    year: "numeric",
  });

export const formatWeekday = (iso: string) =>
  isToday(iso)
    ? "Today"
    : parseDate(iso).toLocaleDateString("en-GB", { weekday: "short" });

export const MONTH_LABELS = [
  "Jan",
  "Feb",
  "Mar",
  "Apr",
  "May",
  "Jun",
  "Jul",
  "Aug",
  "Sep",
  "Oct",
  "Nov",
  "Dec",
];

export const MONTH_NAMES = [
  "January",
  "February",
  "March",
  "April",
  "May",
  "June",
  "July",
  "August",
  "September",
  "October",
  "November",
  "December",
];
