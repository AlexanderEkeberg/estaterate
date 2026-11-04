package org.example.crimerate.client;

import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;

import java.util.function.Supplier;

final class HttpRetry {

    private static final long PAUSE_MS = 300;

    private HttpRetry() {
    }

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
