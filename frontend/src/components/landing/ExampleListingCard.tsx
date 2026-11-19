import { Activity, ExternalLink } from "lucide-react";
import houseImage from "../../assets/images/main_img.jpg";
import styles from "./ExampleListingCard.module.css";

export function ExampleListingCard() {
  return (
    <div className={styles.card}>
      <img className={styles.image} src={houseImage} alt="Example property" />
      <div className={styles.info}>
        <div className={styles.addressRow}>
          <h3 className={`${styles.address} bold-font`}>
            Krokveien 12, 1367 Snarøya
          </h3>
          <ExternalLink size={16} className={styles.link} aria-hidden />
        </div>
        <p className={styles.details}>
          Detached house · 143 m² · 5 rooms · NOK 5,900,000
        </p>
      </div>
      <div className={styles.button}>
        <Activity size={16} aria-hidden />
        Analyze this property ...
      </div>
    </div>
  );
}
