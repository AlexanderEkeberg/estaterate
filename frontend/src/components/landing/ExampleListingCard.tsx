import styles from "./ExampleListingCard.module.css";

export function ExampleListingCard() {
  return (
    <div className={styles.card}>
      <div className={styles.images}>
        <div className={styles.mainImage} />
        <div className={styles.smallImage} />
        <div className={styles.smallImage} />
      </div>
      <div className={styles.info}>
        <div className={styles.address} />
        <div className={styles.details} />
      </div>
      <div className={styles.button} />
    </div>
  );
}
