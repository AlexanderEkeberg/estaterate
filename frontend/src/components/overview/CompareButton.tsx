import { Check, Columns2 } from "lucide-react";
import { useAuth } from "../../auth/AuthProvider.js";
import { MAX_COMPARE, useCompare } from "../compare/CompareContext.js";
import type { PropertyReport } from "../../types/report.js";
import { computeTotalScore } from "../../lib/score.js";
import { cx } from "../../lib/cx.js";
import ui from "../../style/ui.module.css";

export function CompareButton({ report }: { report: PropertyReport }) {
  const { status, login } = useAuth();
  const { isSelected, isFull, toggle } = useCompare();

  if (status === "anonymous") {
    return (
      <button type="button" className={cx(ui.btn, ui.outline)} onClick={login}>
        <Columns2 size={18} aria-hidden />
        Log in to compare
      </button>
    );
  }
  if (status !== "authenticated") {
    return null;
  }

  const selected = isSelected(report.id);
  const disabled = isFull && !selected;

  return (
    <button
      type="button"
      aria-pressed={selected}
      disabled={disabled}
      title={
        disabled ? `You can compare up to ${MAX_COMPARE} properties` : undefined
      }
      onClick={() =>
        toggle({
          id: report.id,
          address: report.address ?? null,
          totalScore: computeTotalScore(report).total,
        })
      }
      className={cx(ui.btn, selected ? ui.selected : ui.outline)}
    >
      {selected ? (
        <Check size={18} strokeWidth={2.4} aria-hidden />
      ) : (
        <Columns2 size={18} aria-hidden />
      )}
      {selected ? "Added to compare" : "Compare"}
    </button>
  );
}
