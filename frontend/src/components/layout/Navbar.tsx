import { ArrowRight, User } from "lucide-react";
import { Logo } from "../ui/Logo.js";
import { Button } from "../ui/Button.js";
import styles from "./Navbar.module.css";

export function Navbar() {
  return (
    <header className={styles.navbar}>
      <Logo />
      <div className={styles.actions}>
        <Button variant="ghost">
          <User size={16} />
          Log in
        </Button>
        <Button>
          Get started
          <ArrowRight size={16} />
        </Button>
      </div>
    </header>
  );
}
