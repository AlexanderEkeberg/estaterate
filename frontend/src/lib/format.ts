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
