import { Heart } from "lucide-react";
import { useAuth } from "../auth/AuthProvider.js";
import { useFavourites } from "../hooks/useFavourites.js";
import { Navbar } from "../components/layout/Navbar.js";
import { FavouriteCard } from "../components/favourites/FavouriteCard.js";
import { navigate } from "../main/router.js";
import { cx } from "../lib/cx.js";
import ui from "../style/ui.module.css";
import styles from "./FavouritesPage.module.css";

type MessageProps = {
  title: string;
  text: string;
  action?: { label: string; onClick: () => void };
};

function Message({ title, text, action }: MessageProps) {
  return (
    <div className={cx(ui.card, styles.message)}>
      <span className={styles.messageIcon}>
        <Heart size={28} aria-hidden />
      </span>
      <h2 className={styles.messageTitle}>{title}</h2>
      <p className={styles.messageText}>{text}</p>
      {action && (
        <button
          type="button"
          className={cx(ui.btn, ui.primary, ui.lg)}
          onClick={action.onClick}
        >
          {action.label}
        </button>
      )}
    </div>
  );
}

function Content() {
  const { status, login } = useAuth();
  const { favourites, error, remove } = useFavourites(
    status === "authenticated",
  );

  if (status === "loading") {
    return null;
  }
  if (status === "unavailable") {
    return (
      <Message
        title="Login is unavailable"
        text="We could not reach the login service. Try again later."
      />
    );
  }
  if (status === "anonymous") {
    return (
      <Message
        title="Log in to see your favourites"
        text="Favourites are saved on your account."
        action={{ label: "Log in", onClick: login }}
      />
    );
  }
  if (!favourites) {
    return error ? <Message title="Something went wrong" text={error} /> : null;
  }
  if (favourites.length === 0) {
    return (
      <Message
        title="No favourites yet"
        text="Analyze a property and press Save. It will show up here so you can come back to it."
        action={{ label: "Analyze a property", onClick: () => navigate("/") }}
      />
    );
  }

  return (
    <>
      <p className={styles.count}>
        {favourites.length === 1
          ? "1 saved property"
          : `${favourites.length} saved properties`}
      </p>
      {error && (
        <p role="alert" className={styles.error}>
          {error}
        </p>
      )}
      <ul className={styles.grid}>
        {favourites.map((favourite) => (
          <li key={favourite.analysisId}>
            <FavouriteCard
              favourite={favourite}
              onRemove={() => remove(favourite.analysisId)}
            />
          </li>
        ))}
      </ul>
    </>
  );
}

export function FavouritesPage() {
  return (
    <div className={styles.page}>
      <Navbar />
      <main className={ui.page}>
        <h1 className={ui.h1}>My favourites</h1>
        <Content />
      </main>
    </div>
  );
}
