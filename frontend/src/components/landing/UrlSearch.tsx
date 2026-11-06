import { useState } from "react";
import type { FormEvent } from "react";
import { ArrowRight } from "lucide-react";
import { ApiError, scrapeProperty } from "../../api/propertyApi.js";
import { navigate } from "../../main/router.js";
import { saveScrapeResult } from "../../storage/scrapeResults.js";
import { Button } from "../ui/Button.js";
import styles from "./UrlSearch.module.css";

const FINN_PREFIX = "https://www.finn.no/realestate/homes/";

type Status = "idle" | "submitting" | "error";

function errorMessage(error: unknown): string {
  if (error instanceof ApiError) {
    if (error.status === null) {
      return "server error occurred";
    }
  }
  return "Something went wrong.";
}

export function UrlSearch() {
  const [url, setUrl] = useState("");
  const [status, setStatus] = useState<Status>("idle");
  const [message, setMessage] = useState<string | null>(null);

  const submitting = status === "submitting";

  async function handleSubmit(event: FormEvent) {
    event.preventDefault();
    if (submitting) return;

    const trimmed = url.trim();
    if (!trimmed.startsWith(FINN_PREFIX)) {
      setStatus("error");
      setMessage("Only links starting with https://www.finn.no/realestate/homes/ are supported.");
      return;
    }

    setStatus("submitting");
    setMessage(null);
    try {
      const result = await scrapeProperty(trimmed);
      saveScrapeResult(result);
      navigate(`/analysis/${encodeURIComponent(result.analysisId)}`);
    } catch (error) {
      console.error(error);
      setStatus("error");
      setMessage(errorMessage(error));
    }
  }

  return (
    <div className={styles.wrapper}>
      <form
        className={styles.form}
        onSubmit={handleSubmit}
        aria-busy={submitting}
        noValidate
      >
        <input
          className={styles.input}
          type="url"
          value={url}
          onChange={(e) => {
            setUrl(e.target.value);
            if (status !== "submitting") {
              setStatus("idle");
              setMessage(null);
            }
          }}
          placeholder="Paste FINN listing URL here ..."
          aria-label="FINN listing URL"
          aria-invalid={status === "error"}
          aria-describedby={message ? "url-search-message" : undefined}
          disabled={submitting}
        />
        <Button type="submit" disabled={submitting}>
          {submitting ? "Analyzing..." : "Analyze"}
          {!submitting && <ArrowRight size={16} />}
        </Button>
      </form>
      {message && (
        <p id="url-search-message" className={styles.error} role="alert">
          {message}
        </p>
      )}
    </div>
  );
}
