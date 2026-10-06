import { Navbar } from "../components/layout/Navbar.js";
import { Hero } from "../components/landing/Hero.js";
import { HowItWorks } from "../components/landing/HowItWorks.js";
import { BuyerBenefits } from "../components/landing/BuyerBenefits.js";
import styles from "./LandingPage.module.css";

export function LandingPage() {
  return (
    <main className={styles.page}>
      <div className={styles.heroBackground}>
        <Navbar />
        <Hero />
      </div>
      <HowItWorks />
      <BuyerBenefits />
    </main>
  );
}
