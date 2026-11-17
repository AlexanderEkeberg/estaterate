import { useState } from "react";
import { ChevronDown, Heart, LogOut } from "lucide-react";
import { useDismiss } from "../../hooks/useDismiss.js";
import { AppLink } from "../ui/AppLink.js";
import styles from "./AccountMenu.module.css";

type AccountMenuProps = {
  username: string;
  onLogout: () => void;
};

export function AccountMenu({ username, onLogout }: AccountMenuProps) {
  const [open, setOpen] = useState(false);
  const ref = useDismiss<HTMLDivElement>(open, () => setOpen(false));

  return (
    <div className={styles.wrap} ref={ref}>
      <button
        type="button"
        className={styles.trigger}
        aria-haspopup="menu"
        aria-expanded={open}
        onClick={() => setOpen((current) => !current)}
      >
        <span className={styles.avatar} aria-hidden>
          {username.charAt(0)}
        </span>
        {username}
        <ChevronDown size={16} color="var(--er-muted)" aria-hidden />
      </button>
      {open && (
        <div role="menu" aria-label="Account menu" className={styles.menu}>
          <div className={styles.head}>
            <div className={styles.name}>{username}</div>
            <div className={styles.sub}>Signed in</div>
          </div>
          <AppLink role="menuitem" to="/favourites" className={styles.item}>
            <Heart size={18} aria-hidden />
            My favourites
          </AppLink>
          <div className={styles.divider} />
          <button
            role="menuitem"
            type="button"
            className={styles.item}
            onClick={onLogout}
          >
            <LogOut size={18} aria-hidden />
            Log out
          </button>
        </div>
      )}
    </div>
  );
}
