package ru.nsu.babich.spider.crawl;

public class FetchException extends Exception {
    private final boolean retryable;

    public FetchException(String message, boolean retryable) {
        super(message);
        this.retryable = retryable;
    }

    public FetchException(String message, Throwable cause, boolean retryable) {
        super(message, cause);
        this.retryable = retryable;
    }

    public boolean isRetryable() {
        return retryable;
    }
}
