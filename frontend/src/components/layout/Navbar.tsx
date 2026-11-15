import { Heart, LogOut, User } from "lucide-react";
import { useAuth } from "../../auth/AuthProvider.js";
import { AppLink } from "../ui/AppLink.js";
import { Logo } from "../ui/Logo.js";
import { Button } from "../ui/Button.js";
import styles from "./Navbar.module.css";

export function Navbar() {
  const { status, username, login, logout } = useAuth();

  return (
    <header className={styles.navbar}>
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
            {username && (
              <span className={styles.user}>
                <User size={16} aria-hidden />
                {username}
              </span>
            )}
            <Button variant="ghost" onClick={logout}>
              <LogOut size={16} />
              Log out
            </Button>
          </>
        )}
      </div>
    </header>
  );
}
