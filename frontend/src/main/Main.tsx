import React from "react";
import { createRoot } from "react-dom/client";
import { AuthProvider } from "../auth/AuthProvider.js";
import { CompareProvider } from "../components/compare/CompareContext.js";
import { Application } from "./Application.js";

createRoot(document.getElementById("root")!).render(
  <AuthProvider>
    <CompareProvider>
      <Application />
    </CompareProvider>
  </AuthProvider>,
);
