import {
  ArrowRight,
  GraduationCap,
  Heart,
  Info,
  ShieldCheck,
} from "lucide-react";
import type { Favourite } from "../../api/favouritesApi.js";
import { formatSavedDate } from "../../lib/format.js";
import { totalFromScores } from "../../lib/score.js";
import { cx } from "../../lib/cx.js";
import { AppLink } from "../ui/AppLink.js";
import ui from "../../style/ui.module.css";
import styles from "./FavouriteCard.module.css";

type FavouriteCardProps = {
  favourite: Favourite;
  onRemove: () => void;
};

export function FavouriteCard({ favourite, onRemove }: FavouriteCardProps) {
  const { scores } = favourite;
  const total = totalFromScores(scores);
  const title = favourite.address ?? "Address not available";
  const titleId = `favourite-${favourite.analysisId}`;

  return (
    <article className={cx(ui.card, styles.card)} aria-labelledby={titleId}>
      <div className={styles.top}>
        <div>
          <div className={styles.label}>Total score</div>
          <div className={styles.score}>
            <span className={styles.scoreValue}>{total ?? "–"}</span>
            <span className={styles.outOf}>/ 100</span>
          </div>
        </div>
        <button
          type="button"
          className={styles.heart}
          onClick={onRemove}
          aria-label={`Remove ${title} from favourites`}
        >
          <Heart size={20} fill="currentColor" aria-hidden />
        </button>
      </div>

      <h2 id={titleId} className={styles.address}>
        {title}
      </h2>

      {favourite.available ? (
        <div className={styles.chips}>
          <span className={cx(styles.chip, styles.crime)}>
            <ShieldCheck size={14} aria-hidden />
            Crime {scores["CRIME"] ?? "–"}
          </span>
          <span className={cx(styles.chip, styles.schools)}>
            <GraduationCap size={14} aria-hidden />
            Schools {scores["SCHOOL"] ?? "–"}
          </span>
        </div>
      ) : (
        <p className={styles.unavailable}>
          <Info size={16} aria-hidden />
          Details unavailable right now
        </p>
      )}

      <div className={styles.saved}>
        Saved {formatSavedDate(favourite.savedAt)}
      </div>

      <div className={styles.foot}>
        <AppLink
          to={`/analysis/${encodeURIComponent(favourite.analysisId)}`}
          className={cx(ui.btn, ui.primary)}
        >
          Open report
          <ArrowRight size={16} aria-hidden />
        </AppLink>
      </div>
    </article>
  );
}
