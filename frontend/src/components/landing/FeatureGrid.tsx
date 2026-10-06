import styles from "./FeatureGrid.module.css";

const CARD_COUNT = 6;

export function FeatureGrid() {
  return (
    <div className={styles.grid}>
      {Array.from({ length: CARD_COUNT }, (_, i) => (
        <div key={i} className={styles.card} />
      ))}
    </div>
  );
}
