package no.property.validation;

import no.property.exception.InvalidFinnUrlException;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URISyntaxException;

@Component
public class FinnUrlValidator {

    private static final String ALLOWED_HOST = "www.finn.no";
    private static final String ALLOWED_PATH_PREFIX = "/realestate/homes/";
    private static final int MAX_LENGTH = 2048;
    public URI validate(String raw) {
        if (raw == null || raw.isBlank() || raw.length() > MAX_LENGTH) {
            throw new InvalidFinnUrlException("URL is missing or too long");
        }
        URI uri;
        try {
            uri = new URI(raw.trim());
        } catch (URISyntaxException e) {
            throw new InvalidFinnUrlException("URL is malformed");
        }

        if (!"https".equalsIgnoreCase(uri.getScheme())) {
            throw new InvalidFinnUrlException("Only https is allowed");
        }
        if (uri.getRawUserInfo() != null) {
            throw new InvalidFinnUrlException("User info is not allowed");
        }
        if (!ALLOWED_HOST.equalsIgnoreCase(uri.getHost())) {
            throw new InvalidFinnUrlException("Host is not allowed");
        }
        if (uri.getPort() != -1 && uri.getPort() != 443) {
            throw new InvalidFinnUrlException("Port is not allowed");
        }

        String path = uri.normalize().getPath();
        if (path == null || !path.startsWith(ALLOWED_PATH_PREFIX)) {
            throw new InvalidFinnUrlException("Path is not allowed");
        }
        return uri;
    }
}
