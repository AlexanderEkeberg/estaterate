import {
  createContext,
  useContext,
  useEffect,
  useState,
  type ReactNode,
} from "react";
import { useAuth } from "../../auth/AuthProvider.js";

export const MAX_COMPARE = 2;

export type CompareItem = {
  id: string;
  address: string | null;
  totalScore: number | null;
};

type CompareState = {
  items: CompareItem[];
  isFull: boolean;
  isSelected: (id: string) => boolean;
  toggle: (item: CompareItem) => void;
  remove: (id: string) => void;
  clear: () => void;
};

const STORAGE_KEY = "estaterate.compare";
const UUID = /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i;

export const isCompareId = (id: string) => UUID.test(id);

function toItem(value: unknown): CompareItem | null {
  if (typeof value !== "object" || value === null) return null;
  const { id, address, totalScore } = value as Record<string, unknown>;
  if (typeof id !== "string" || !UUID.test(id)) return null;
  return {
    id,
    address: typeof address === "string" ? address : null,
    totalScore:
      typeof totalScore === "number" && Number.isFinite(totalScore)
        ? totalScore
        : null,
  };
}

type Stored = { owner: string | null; items: CompareItem[] };

function readStored(): Stored {
  try {
    const parsed: unknown = JSON.parse(
      sessionStorage.getItem(STORAGE_KEY) ?? "{}",
    );
    const { owner, items } = (parsed ?? {}) as Record<string, unknown>;
    if (typeof owner !== "string" || !Array.isArray(items)) {
      return { owner: null, items: [] };
    }
    return {
      owner,
      items: items
        .map(toItem)
        .filter((item): item is CompareItem => item !== null)
        .slice(0, MAX_COMPARE),
    };
  } catch {
    return { owner: null, items: [] };
  }
}

const CompareContext = createContext<CompareState | null>(null);

export function CompareProvider({ children }: { children: ReactNode }) {
  const { userId } = useAuth();
  const [stored, setStored] = useState<Stored>(readStored);
  const items = stored.owner === userId ? stored.items : [];

  useEffect(() => {
    try {
      sessionStorage.setItem(STORAGE_KEY, JSON.stringify(stored));
    } catch {
      return;
    }
  }, [stored]);

  const update = (change: (current: CompareItem[]) => CompareItem[]) =>
    setStored((current) => ({
      owner: userId,
      items: change(current.owner === userId ? current.items : []),
    }));

  const remove = (id: string) =>
    update((current) => current.filter((item) => item.id !== id));

  const value: CompareState = {
    items,
    isFull: items.length >= MAX_COMPARE,
    isSelected: (id) => items.some((item) => item.id === id),
    toggle: (item) =>
      update((current) => {
        if (current.some((i) => i.id === item.id)) {
          return current.filter((i) => i.id !== item.id);
        }
        return current.length < MAX_COMPARE ? [...current, item] : current;
      }),
    remove,
    clear: () => update(() => []),
  };

  return (
    <CompareContext.Provider value={value}>{children}</CompareContext.Provider>
  );
}

export function useCompare(): CompareState {
  const context = useContext(CompareContext);
  if (!context) {
    throw new Error("useCompare must be used inside CompareProvider");
  }
  return context;
}
