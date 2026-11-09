import {
  Cloud,
  CloudDrizzle,
  CloudFog,
  CloudLightning,
  CloudMoon,
  CloudRain,
  CloudSnow,
  CloudSun,
  Moon,
  Sun,
} from "lucide-react";
import type { LucideIcon } from "lucide-react";

export type WeatherTone = "sun" | "cloud" | "rain" | "snow";

export type WeatherIconInfo = {
  Icon: LucideIcon;
  label: string;
  tone: WeatherTone;
};

export function weatherIcon(symbol: string): WeatherIconInfo {
  const s = symbol.toLowerCase();
  const night = s.endsWith("_night");

  if (s.includes("thunder")) {
    return { Icon: CloudLightning, label: "Thunder", tone: "rain" };
  }
  if (s.includes("sleet")) {
    return { Icon: CloudSnow, label: "Sleet", tone: "snow" };
  }
  if (s.includes("snow")) {
    return { Icon: CloudSnow, label: "Snow", tone: "snow" };
  }
  if (s.includes("lightrain") || s.includes("drizzle")) {
    return { Icon: CloudDrizzle, label: "Light rain", tone: "rain" };
  }
  if (s.includes("rain")) {
    return { Icon: CloudRain, label: "Rain", tone: "rain" };
  }
  if (s.startsWith("fog")) {
    return { Icon: CloudFog, label: "Fog", tone: "cloud" };
  }
  if (s.startsWith("partlycloudy") || s.startsWith("fair")) {
    return {
      Icon: night ? CloudMoon : CloudSun,
      label: "Partly cloudy",
      tone: "sun",
    };
  }
  if (s.startsWith("clearsky")) {
    return { Icon: night ? Moon : Sun, label: "Clear sky", tone: "sun" };
  }
  return { Icon: Cloud, label: "Cloudy", tone: "cloud" };
}
