import type { ReactNode } from "react";
import { cx } from "../../lib/cx.js";
import ui from "../../style/ui.module.css";
import styles from "./Message.module.css";

type MessageProps = {
  icon: ReactNode;
  title: string;
  text: string;
  action?: { label: string; onClick: () => void };
};

export function Message({ icon, title, text, action }: MessageProps) {
  return (
    <div className={cx(ui.card, styles.message)}>
      <span className={styles.icon}>{icon}</span>
      <h2 className={styles.title}>{title}</h2>
      <p className={styles.text}>{text}</p>
      {action && (
        <button
          type="button"
          className={cx(ui.btn, ui.primary, ui.lg)}
          onClick={action.onClick}
        >
          {action.label}
        </button>
      )}
    </div>
  );
}
