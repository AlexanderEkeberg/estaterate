import {
  createContext,
  useContext,
  useEffect,
  useState,
  type ReactNode,
} from "react";

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

function readStored(): CompareItem[] {
  try {
    const parsed: unknown = JSON.parse(
      sessionStorage.getItem(STORAGE_KEY) ?? "[]",
    );
    if (!Array.isArray(parsed)) return [];
    return parsed
      .map(toItem)
      .filter((item): item is CompareItem => item !== null)
      .slice(0, MAX_COMPARE);
  } catch {
    return [];
  }
}

const CompareContext = createContext<CompareState | null>(null);

export function CompareProvider({ children }: { children: ReactNode }) {
  const [items, setItems] = useState<CompareItem[]>(readStored);

  useEffect(() => {
    try {
      sessionStorage.setItem(STORAGE_KEY, JSON.stringify(items));
    } catch {
      return;
    }
  }, [items]);

  const remove = (id: string) =>
    setItems((current) => current.filter((item) => item.id !== id));

  const value: CompareState = {
    items,
    isFull: items.length >= MAX_COMPARE,
    isSelected: (id) => items.some((item) => item.id === id),
    toggle: (item) =>
      setItems((current) => {
        if (current.some((i) => i.id === item.id)) {
          return current.filter((i) => i.id !== item.id);
        }
        return current.length < MAX_COMPARE ? [...current, item] : current;
      }),
    remove,
    clear: () => setItems([]),
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
