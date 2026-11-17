import { Heart, User } from "lucide-react";
import { useAuth } from "../../auth/AuthProvider.js";
import { AppLink } from "../ui/AppLink.js";
import { Logo } from "../ui/Logo.js";
import { Button } from "../ui/Button.js";
import { AccountMenu } from "./AccountMenu.js";
import { cx } from "../../lib/cx.js";
import styles from "./Navbar.module.css";

export function Navbar({ onHero = false }: { onHero?: boolean }) {
  const { status, username, login, logout } = useAuth();

  return (
    <header className={cx(styles.navbar, !onHero && styles.solid)}>
      <AppLink to="/" className={styles.home} aria-label="Estaterate home">
        <Logo />
      </AppLink>
      <div className={styles.actions}>
        {status === "anonymous" && (
          <Button variant="ghost" onClick={login}>
            <User size={16} />
            Log in
          </Button>
        )}
        {status === "authenticated" && (
          <>
            <AppLink to="/favourites" className={styles.link}>
              <Heart size={16} aria-hidden />
              Favourites
            </AppLink>
            <AccountMenu username={username ?? "Account"} onLogout={logout} />
          </>
        )}
      </div>
    </header>
  );
}
