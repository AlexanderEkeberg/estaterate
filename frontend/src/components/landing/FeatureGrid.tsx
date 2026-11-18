import { GraduationCap, House, ShieldAlert, Sun } from "lucide-react";
import styles from "./FeatureGrid.module.css";

const FEATURES = [
  {
    icon: House,
    color: "#12a150",
    title: "Property details",
    text: "Price, size and location",
  },
  {
    icon: GraduationCap,
    color: "#0a5cff",
    title: "Schools",
    text: "Nearby schools and distance",
  },
  {
    icon: Sun,
    color: "#f59e0b",
    title: "Weather & climate",
    text: "Temperature, rain, snow and sunshine",
  },
  {
    icon: ShieldAlert,
    color: "#e5484d",
    title: "Safety & crime",
    text: "Reported crime per 1,000 residents",
  },
];

export function FeatureGrid() {
  return (
    <ul className={styles.grid}>
      {FEATURES.map(({ icon: Icon, color, title, text }) => (
        <li key={title} className={styles.card}>
          <span
            className={styles.icon}
            style={{ color, background: `${color}1a` }}
          >
            <Icon size={22} aria-hidden />
          </span>
          <h3 className={`${styles.title} bold-font`}>{title}</h3>
          <p className={styles.text}>{text}</p>
        </li>
      ))}
    </ul>
  );
}
