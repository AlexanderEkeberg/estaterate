package org.example.crimerate.client;

public class StatbankException extends RuntimeException {

    public StatbankException(String message) {
        super(message);
    }

    public StatbankException(String message, Throwable cause) {
        super(message, cause);
    }
}
