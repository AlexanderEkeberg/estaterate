import {
  createContext,
  useContext,
  useEffect,
  useState,
  type ReactNode,
} from "react";
import { keycloak, keycloakReady } from "./keycloak.js";

type AuthStatus = "loading" | "unavailable" | "anonymous" | "authenticated";

type Auth = {
  status: AuthStatus;
  username: string | null;
  userId: string | null;
  login: () => void;
  register: () => void;
  logout: () => void;
};

const AuthContext = createContext<Auth | null>(null);

function currentStatus(): AuthStatus {
  return keycloak.authenticated ? "authenticated" : "anonymous";
}

function currentUsername(): string | null {
  const name: unknown = keycloak.tokenParsed?.["preferred_username"];
  return typeof name === "string" ? name : null;
}

export function AuthProvider({ children }: { children: ReactNode }) {
  const [status, setStatus] = useState<AuthStatus>("loading");

  useEffect(() => {
    keycloak.onAuthLogout = () => setStatus("anonymous");
    keycloak.onTokenExpired = () => {
      keycloak.updateToken(30).catch(() => keycloak.clearToken());
    };
    keycloakReady.then((ready) =>
      setStatus(ready ? currentStatus() : "unavailable"),
    );
  }, []);

  const auth: Auth = {
    status,
    username: status === "authenticated" ? currentUsername() : null,
    userId:
      status === "authenticated" ? (keycloak.tokenParsed?.sub ?? null) : null,
    login: () => void keycloak.login(),
    register: () => void keycloak.register(),
    logout: () => void keycloak.logout({ redirectUri: window.location.href }),
  };

  return <AuthContext.Provider value={auth}>{children}</AuthContext.Provider>;
}

export function useAuth(): Auth {
  const auth = useContext(AuthContext);
  if (!auth) {
    throw new Error("useAuth must be used inside AuthProvider");
  }
  return auth;
}
