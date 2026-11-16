import Keycloak from "keycloak-js";

export const keycloak = new Keycloak({
  url: import.meta.env.VITE_KEYCLOAK_URL ?? "http://localhost:8180",
  realm: "estaterate",
  clientId: "estaterate-client",
});

export const keycloakReady: Promise<boolean> = keycloak
  .init({
    onLoad: "check-sso",
    pkceMethod: "S256",
    silentCheckSsoRedirectUri: `${window.location.origin}/silent-check-sso.html`,
    checkLoginIframe: false,
  })
  .then(() => true)
  .catch(() => false);
