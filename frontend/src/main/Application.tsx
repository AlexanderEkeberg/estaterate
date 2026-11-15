import { LandingPage } from "../pages/LandingPage.js";
import { AnalysisPage } from "../pages/AnalysisPage.js";
import { FavouritesPage } from "../pages/FavouritesPage.js";
import { usePath } from "./router.js";

const ANALYSIS_PATH = /^\/analysis\/([^/]+)\/?$/;

export function Application() {
  const path = usePath();
  const analysisId = ANALYSIS_PATH.exec(path)?.[1];

  if (path === "/favourites" || path === "/favourites/") {
    return <FavouritesPage />;
  }
  if (analysisId) {
    return <AnalysisPage analysisId={decodeURIComponent(analysisId)} />;
  }
  return <LandingPage />;
}
