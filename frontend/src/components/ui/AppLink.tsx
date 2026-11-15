import type { AnchorHTMLAttributes, MouseEvent } from "react";
import { navigate } from "../../main/router.js";

type AppLinkProps = Omit<AnchorHTMLAttributes<HTMLAnchorElement>, "href"> & {
  to: string;
};

export function AppLink({ to, ...rest }: AppLinkProps) {
  function onClick(event: MouseEvent<HTMLAnchorElement>) {
    if (
      event.button !== 0 ||
      event.metaKey ||
      event.ctrlKey ||
      event.shiftKey
    ) {
      return;
    }
    event.preventDefault();
    navigate(to);
  }

  return <a {...rest} href={to} onClick={onClick} />;
}
