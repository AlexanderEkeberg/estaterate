import { Heart } from "lucide-react";
import { useAuth } from "../auth/AuthProvider.js";
import { useFavourites } from "../hooks/useFavourites.js";
import { Navbar } from "../components/layout/Navbar.js";
import { FavouriteCard } from "../components/favourites/FavouriteCard.js";
import { Message } from "../components/ui/Message.js";
import { CompareTray } from "../components/compare/CompareTray.js";
import { navigate } from "../main/router.js";
import ui from "../style/ui.module.css";
import styles from "./FavouritesPage.module.css";

const icon = <Heart size={28} aria-hidden />;

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
        icon={icon}
        title="Login is unavailable"
        text="We could not reach the login service. Try again later."
      />
    );
  }
  if (status === "anonymous") {
    return (
      <Message
        icon={icon}
        title="Log in to see your favourites"
        text="Favourites are saved on your account."
        action={{ label: "Log in", onClick: login }}
      />
    );
  }
  if (!favourites) {
    return error ? (
      <Message icon={icon} title="Something went wrong" text={error} />
    ) : null;
  }
  if (favourites.length === 0) {
    return (
      <Message
        icon={icon}
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
      <CompareTray />
    </div>
  );
}
