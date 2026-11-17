import { Heart } from "lucide-react";
import { useAuth } from "../../auth/AuthProvider.js";
import { useFavourite } from "../../hooks/useFavourite.js";
import { cx } from "../../lib/cx.js";
import ui from "../../style/ui.module.css";
import styles from "./SaveButton.module.css";

type SaveButtonProps = {
  analysisId: string;
  onLoginRequired: () => void;
};

export function SaveButton({ analysisId, onLoginRequired }: SaveButtonProps) {
  const { status } = useAuth();
  const { saved, busy, error, toggle } = useFavourite(
    analysisId,
    status === "authenticated",
  );

  if (status === "anonymous") {
    return (
      <button
        type="button"
        className={cx(ui.btn, ui.outline)}
        onClick={onLoginRequired}
      >
        <Heart size={18} aria-hidden />
        Save
      </button>
    );
  }
  if (status !== "authenticated") {
    return null;
  }

  return (
    <div className={styles.save}>
      <button
        type="button"
        aria-pressed={saved}
        disabled={busy}
        onClick={toggle}
        className={cx(ui.btn, saved ? ui.selected : ui.outline)}
      >
        <Heart size={18} fill={saved ? "currentColor" : "none"} aria-hidden />
        {saved ? "Saved" : "Save"}
      </button>
      {error && (
        <p role="alert" className={styles.error}>
          {error}
        </p>
      )}
    </div>
  );
}
