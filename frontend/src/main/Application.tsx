import { LandingPage } from "../pages/LandingPage.js";
import { AnalysisPage } from "../pages/AnalysisPage.js";
import { usePath } from "./router.js";

const ANALYSIS_PATH = /^\/analysis\/([^/]+)\/?$/;

export function Application() {
  const path = usePath();
  const analysisId = ANALYSIS_PATH.exec(path)?.[1];

  if (analysisId) {
    return <AnalysisPage analysisId={decodeURIComponent(analysisId)} />;
  }
  return <LandingPage />;
}
