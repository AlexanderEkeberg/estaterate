package no.weather.client;

import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;

import java.util.function.Supplier;

final class HttpRetry {

    private static final long PAUSE_MS = 300;

    private HttpRetry() {
    }

    // prøver én gang til ved timeout, koblingsfeil og 5xx
    static <Response> Response call(Supplier<Response> request) {
        try {
            return request.get();
        } catch (ResourceAccessException | HttpServerErrorException e) {
            pause();
            return request.get();
        }
    }

    private static void pause() {
        try {
            Thread.sleep(PAUSE_MS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
