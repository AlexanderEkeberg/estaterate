import { TriangleAlert } from "lucide-react";
import styles from "./DataNotice.module.css";

type DataNoticeProps = {
  message: string;
};

export function DataNotice({ message }: DataNoticeProps) {
  return (
    <div className={styles.notice} role="status">
      <TriangleAlert size={18} className={styles.icon} />
      <p className={styles.message}>{message}</p>
    </div>
  );
}
