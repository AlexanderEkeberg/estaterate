import styles from "./BuyerBenefits.module.css";

const BENEFITS = [
  {
    title: "Objective information",
    text: "Based on public data",
  },
  {
    title: "Everything in one place",
    text: "Save time and get the full picture",
  },
  {
    title: "Better decisions",
    text: "See advantages and drawbacks",
  },
  {
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
          {BENEFITS.map((benefit) => (
            <li key={benefit.title} className={styles.benefit}>
              <h3 className={`${styles.benefitTitle} bold-font`}>
                {benefit.title}
              </h3>
              <p className={styles.benefitText}>{benefit.text}</p>
            </li>
          ))}
        </ul>
      </div>
    </section>
  );
}
