import React from "react";
import { createRoot } from "react-dom/client";
import { AuthProvider } from "../auth/AuthProvider.js";
import { Application } from "./Application.js";

createRoot(document.getElementById("root")!).render(
  <AuthProvider>
    <Application />
  </AuthProvider>,
);
