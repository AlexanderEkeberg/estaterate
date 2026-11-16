import { keycloak, keycloakReady } from "../auth/keycloak.js";

export async function authHeaders(): Promise<Record<string, string>> {
  await keycloakReady;
  if (!keycloak.authenticated) {
    return {};
  }
  try {
    await keycloak.updateToken(30);
  } catch {
    keycloak.clearToken();
    return {};
  }
  return keycloak.token ? { Authorization: `Bearer ${keycloak.token}` } : {};
}
