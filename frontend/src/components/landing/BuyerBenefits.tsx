import { ChartNoAxesColumn, Check, Heart, Target } from "lucide-react";
import styles from "./BuyerBenefits.module.css";

const BENEFITS = [
  {
    icon: Check,
    color: "#12a150",
    title: "Objective information",
    text: "Based on public data",
  },
  {
    icon: Target,
    color: "#0a5cff",
    title: "Everything in one place",
    text: "Save time and get the full picture",
  },
  {
    icon: ChartNoAxesColumn,
    color: "#0a5cff",
    title: "Better decisions",
    text: "See advantages and drawbacks",
  },
  {
    icon: Heart,
    color: "#e5484d",
    title: "Suitable for everyone",
    text: "Whether you are buying or selling",
  },
];

export function BuyerBenefits() {
  return (
    <section className={styles.section}>
      <div className={styles.panel}>
        <h2 className={`${styles.title} bold-font`}>
          For home buyers who want to make better decisions
        </h2>
        <ul className={styles.benefits}>
          {BENEFITS.map(({ icon: Icon, color, title, text }) => (
            <li key={title} className={styles.benefit}>
              <span
                className={styles.icon}
                style={{ color, background: `${color}1a` }}
              >
                <Icon size={20} aria-hidden />
              </span>
              <div>
                <h3 className={`${styles.benefitTitle} bold-font`}>{title}</h3>
                <p className={styles.benefitText}>{text}</p>
              </div>
            </li>
          ))}
        </ul>
      </div>
    </section>
  );
}
