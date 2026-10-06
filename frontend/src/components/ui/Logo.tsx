import { House } from "lucide-react";
import styles from "./Logo.module.css";

export function Logo() {
  return (
    <div className={styles.logo}>
      <span className={styles.icon}>
        <House size={22} />
      </span>
      <span className={`${styles.name} bold-font`}>Estaterate</span>
    </div>
  );
}
