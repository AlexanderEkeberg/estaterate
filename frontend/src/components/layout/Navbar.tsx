import { LogOut, User } from "lucide-react";
import { useAuth } from "../../auth/AuthProvider.js";
import { Logo } from "../ui/Logo.js";
import { Button } from "../ui/Button.js";
import styles from "./Navbar.module.css";

export function Navbar() {
  const { status, username, login, logout } = useAuth();

  return (
    <header className={styles.navbar}>
      <Logo />
      <div className={styles.actions}>
        {status === "anonymous" && (
          <Button variant="ghost" onClick={login}>
            <User size={16} />
            Log in
          </Button>
        )}
        {status === "authenticated" && (
          <>
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
