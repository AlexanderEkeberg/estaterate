import { ExampleListingCard } from "./ExampleListingCard.js";
import styles from "./HowItWorks.module.css";

const STEPS = [
  {
    title: "Paste FINN URL",
    text: "We automatically extract information from the listing.",
  },
  {
    title: "We analyze",
    text: "We collect data from public sources and analyze the area.",
  },
  {
    title: "Get the full overview",
    text: "See the results in a clear and easy-to-read report.",
  },
];

export function HowItWorks() {
  return (
    <section id="how-it-works" className={styles.section}>
      <div className={styles.content}>
        <h2 className={`${styles.title} bold-font`}>How it works</h2>
        <p className={styles.subtitle}>
          Three simple steps – get a complete analysis.
        </p>
        <ol className={styles.steps}>
          {STEPS.map((step, i) => (
            <li key={step.title} className={styles.step}>
              <span className={`${styles.number} bold-font`}>{i + 1}</span>
              <h3 className={`${styles.stepTitle} bold-font`}>{step.title}</h3>
              <p className={styles.stepText}>{step.text}</p>
            </li>
          ))}
        </ol>
      </div>
      <ExampleListingCard />
    </section>
  );
}
