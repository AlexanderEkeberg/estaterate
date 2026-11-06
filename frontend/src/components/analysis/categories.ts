import { CircleDot, School, ShieldAlert, Sun } from "lucide-react";
import type { LucideIcon } from "lucide-react";

export type CategoryTone = "weather" | "crime" | "school" | "other";

export type CategoryInfo = {
  label: string;
  icon: LucideIcon;
  tone: CategoryTone;
};

const CATEGORIES: Record<string, CategoryInfo> = {
  WEATHER: { label: "Weather & sunlight", icon: Sun, tone: "weather" },
  CRIME: { label: "Safety & crime", icon: ShieldAlert, tone: "crime" },
  SCHOOL: { label: "Schools & services", icon: School, tone: "school" },
};

export const DEFAULT_CATEGORIES = Object.keys(CATEGORIES);

export function categoryInfo(category: string): CategoryInfo {
  return (
    CATEGORIES[category] ?? {
      label: category.charAt(0) + category.slice(1).toLowerCase(),
      icon: CircleDot,
      tone: "other",
    }
  );
}

export function sortCategories(categories: string[]): string[] {
  const rank = (category: string) => {
    const index = DEFAULT_CATEGORIES.indexOf(category);
    return index === -1 ? DEFAULT_CATEGORIES.length : index;
  };
  return [...categories].sort(
    (a, b) => rank(a) - rank(b) || a.localeCompare(b),
  );
}
