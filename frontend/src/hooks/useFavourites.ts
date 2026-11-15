import { useEffect, useState } from "react";
import {
  listFavourites,
  removeFavourite,
  type Favourite,
} from "../api/favouritesApi.js";

export type FavouritesState = {
  favourites: Favourite[] | null;
  error: string | null;
  remove: (analysisId: string) => void;
};

export function useFavourites(enabled: boolean): FavouritesState {
  const [favourites, setFavourites] = useState<Favourite[] | null>(null);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    setFavourites(null);
    setError(null);
    if (!enabled) {
      return;
    }
    let cancelled = false;
    listFavourites()
      .then((list) => {
        if (!cancelled) {
          setFavourites(list);
        }
      })
      .catch((e: unknown) => {
        if (!cancelled) {
          console.error(e);
          setError("Could not load your favourites. Try again later.");
        }
      });
    return () => {
      cancelled = true;
    };
  }, [enabled]);

  async function remove(analysisId: string) {
    setError(null);
    try {
      await removeFavourite(analysisId);
      setFavourites(
        (current) =>
          current?.filter((f) => f.analysisId !== analysisId) ?? null,
      );
    } catch (e) {
      console.error(e);
      setError("Could not remove the favourite. Try again.");
    }
  }

  return { favourites, error, remove: (id) => void remove(id) };
}
