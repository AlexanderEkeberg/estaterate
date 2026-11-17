import { Lock, X } from "lucide-react";
import { useAuth } from "../../auth/AuthProvider.js";
import { cx } from "../../lib/cx.js";
import ui from "../../style/ui.module.css";
import styles from "./LoginPopover.module.css";

export function LoginPopover({ onClose }: { onClose: () => void }) {
  const { login } = useAuth();

  return (
    <div
      role="dialog"
      aria-labelledby="login-popover-title"
      className={styles.popover}
    >
      <div className={styles.top}>
        <span className={styles.icon}>
          <Lock size={20} aria-hidden />
        </span>
        <button
          type="button"
          className={styles.close}
          aria-label="Close"
          onClick={onClose}
        >
          <X size={18} aria-hidden />
        </button>
      </div>
      <div id="login-popover-title" className={styles.title}>
        Log in to save properties and compare them
      </div>
      <p className={styles.text}>
        Favourites and comparisons are kept on your account. The analysis itself
        stays free to use.
      </p>
      <button
        type="button"
        className={cx(ui.btn, ui.primary, styles.login)}
        onClick={login}
      >
        Log in
      </button>
    </div>
  );
}
