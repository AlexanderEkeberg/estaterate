import {
  ArrowRight,
  GraduationCap,
  Heart,
  Info,
  ShieldCheck,
} from "lucide-react";
import type { Favourite } from "../../api/favouritesApi.js";
import {
  formatNok,
  formatNumber,
  formatSavedDate,
  splitAddress,
} from "../../lib/format.js";
import { totalFromScores } from "../../lib/score.js";
import { cx } from "../../lib/cx.js";
import { AppLink } from "../ui/AppLink.js";
import { MAX_COMPARE, useCompare } from "../compare/CompareContext.js";
import ui from "../../style/ui.module.css";
import styles from "./FavouriteCard.module.css";

type FavouriteCardProps = {
  favourite: Favourite;
  onRemove: () => void;
};

export function FavouriteCard({ favourite, onRemove }: FavouriteCardProps) {
  const { scores, askingPrice, usableArea, analysisId } = favourite;
  const compare = useCompare();
  const inCompare = compare.isSelected(analysisId);
  const compareDisabled = compare.isFull && !inCompare;
  const total = totalFromScores(scores);
  const [street, postalArea] = splitAddress(favourite.address);
  const title = street ?? "Address not available";
  const meta = [
    postalArea,
    askingPrice != null && formatNok(askingPrice),
    usableArea != null && `${formatNumber(usableArea)} m²`,
  ]
    .filter(Boolean)
    .join(" · ");
  const titleId = `favourite-${favourite.analysisId}`;

  return (
    <article
      className={cx(ui.card, styles.card, inCompare && styles.inCompare)}
      aria-labelledby={titleId}
    >
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
          onClick={() => {
            compare.remove(analysisId);
            onRemove();
          }}
          aria-label={`Remove ${favourite.address ?? title} from favourites`}
        >
          <Heart size={20} fill="currentColor" aria-hidden />
        </button>
      </div>

      <div>
        <h2 id={titleId} className={styles.address}>
          {title}
        </h2>
        {meta && <div className={styles.meta}>{meta}</div>}
      </div>

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
        <label
          className={cx(styles.check, compareDisabled && styles.checkDisabled)}
          title={
            compareDisabled
              ? `You can compare up to ${MAX_COMPARE} properties`
              : undefined
          }
        >
          <input
            type="checkbox"
            checked={inCompare}
            disabled={compareDisabled}
            onChange={() =>
              compare.toggle({
                id: analysisId,
                address: favourite.address,
                totalScore: total,
              })
            }
          />
          Compare
        </label>
        <AppLink
          to={`/analysis/${encodeURIComponent(favourite.analysisId)}?saved`}
          className={cx(ui.btn, ui.primary)}
        >
          Open report
          <ArrowRight size={16} aria-hidden />
        </AppLink>
      </div>
    </article>
  );
}
