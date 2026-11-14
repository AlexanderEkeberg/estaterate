import { useEffect, useState } from "react";
import {
  listFavourites,
  removeFavourite,
  saveFavourite,
} from "../api/favouritesApi.js";
import { ApiError } from "../api/propertyApi.js";

export type FavouriteState = {
  saved: boolean;
  busy: boolean;
  error: string | null;
  toggle: () => void;
};

function errorMessage(error: unknown): string {
  const status = error instanceof ApiError ? error.status : null;
  if (status === 401) {
    return "Your session has expired. Log in again to save.";
  }
  if (status === 503) {
    return "Could not save right now. Try again later.";
  }
  return "Something went wrong. Try again.";
}

export function useFavourite(
  analysisId: string,
  enabled: boolean,
): FavouriteState {
  const [saved, setSaved] = useState(false);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    setSaved(false);
    setError(null);
    if (!enabled) {
      return;
    }
    let cancelled = false;
    listFavourites()
      .then((favourites) => {
        if (!cancelled) {
          setSaved(favourites.some((f) => f.analysisId === analysisId));
        }
      })
      .catch((e: unknown) => console.error(e));
    return () => {
      cancelled = true;
    };
  }, [analysisId, enabled]);

  async function toggle() {
    setBusy(true);
    setError(null);
    try {
      if (saved) {
        await removeFavourite(analysisId);
      } else {
        await saveFavourite(analysisId);
      }
      setSaved(!saved);
    } catch (e) {
      setError(errorMessage(e));
    } finally {
      setBusy(false);
    }
  }

  return { saved, busy, error, toggle: () => void toggle() };
}
