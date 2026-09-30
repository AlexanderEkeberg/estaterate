import { ChartNoAxesColumn } from "lucide-react";
import { Navbar } from "../components/layout/Navbar.js";
import { Badge } from "../components/ui/Badge.js";
import styles from "./LandingPage.module.css";

export function LandingPage() {
  return (
    <main className={styles.page}>
      <Navbar />
      <div className={styles.intro}>
        <Badge icon={<ChartNoAxesColumn size={16} />}>
          Data-driven property insights
        </Badge>
      </div>
    </main>
  );
}
