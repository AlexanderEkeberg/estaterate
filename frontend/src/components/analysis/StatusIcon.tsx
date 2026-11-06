import { Check, X } from "lucide-react";
import styles from "./StatusIcon.module.css";

export type Status = "done" | "active" | "upcoming" | "failed";

const LABELS: Record<Status, string> = {
  done: "Done",
  active: "In progress",
  upcoming: "Not started",
  failed: "Unavailable",
};

type StatusIconProps = {
  status: Status;
};

export function StatusIcon({ status }: StatusIconProps) {
  return (
    <span
      className={`${styles.icon} ${styles[status]}`}
      aria-label={LABELS[status]}
    >
      {status === "done" && <Check size={16} strokeWidth={3} />}
      {status === "failed" && <X size={16} strokeWidth={3} />}
    </span>
  );
}
