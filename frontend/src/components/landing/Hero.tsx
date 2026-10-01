import { ChartNoAxesColumn } from "lucide-react";
import { Badge } from "../ui/Badge.js";
import { UrlSearch } from "./UrlSearch.js";
import styles from "./Hero.module.css";

export function Hero() {
  return (
    <section className={styles.hero}>
      <Badge icon={<ChartNoAxesColumn size={16} />}>
        Data-driven property insights
      </Badge>
      <h1 className={`${styles.title} bold-font`}>
        Get a complete overview before you decide
      </h1>
      <p className={styles.subtitle}>
        Paste a FINN listing and get all relevant information about the property
        and surrounding area — collected in one place.
      </p>
      <UrlSearch />
    </section>
  );
}
