import { Navbar } from "../components/layout/Navbar.js";
import { Hero } from "../components/landing/Hero.js";
import styles from "./LandingPage.module.css";

export function LandingPage() {
  return (
    <main className={styles.page}>
      <Navbar />
      <Hero />
    </main>
  );
}
