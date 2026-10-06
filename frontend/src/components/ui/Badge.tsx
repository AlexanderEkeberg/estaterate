import type { ReactNode } from "react";
import styles from "./Badge.module.css";

type BadgeProps = {
  icon?: ReactNode;
  children: ReactNode;
};

export function Badge({ icon, children }: BadgeProps) {
  return (
    <span className={styles.badge}>
      {icon && <span className={styles.icon}>{icon}</span>}
      {children}
    </span>
  );
}
