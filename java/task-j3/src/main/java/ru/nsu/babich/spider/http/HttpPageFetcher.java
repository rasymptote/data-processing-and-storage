package ru.nsu.babich.spider.http;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.Semaphore;
import ru.nsu.babich.spider.crawl.FetchException;
import ru.nsu.babich.spider.crawl.Page;
import ru.nsu.babich.spider.crawl.PageFetcher;

public final class HttpPageFetcher implements PageFetcher {
    private static final int MAX_ATTEMPTS = 3;
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(30);
    private static final long BACKOFF_MS = 200;

    private final URI base;
    private final HttpClient client;
    private final Semaphore permits;

    public HttpPageFetcher(URI base, int maxConcurrentRequests) {
        this.base = base;
        this.permits = new Semaphore(maxConcurrentRequests);
        this.client = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    @Override
    public Page fetch(String path) throws FetchException, InterruptedException {
        FetchException last = null;
        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            try {
                return fetchOnce(path);
            } catch (FetchException e) {
                if (!e.isRetryable()) {
                    throw e;
                }
                last = e;
                if (attempt < MAX_ATTEMPTS) {
                    Thread.sleep(BACKOFF_MS * attempt);
                }
            }
        }
        throw last;
    }

    private Page fetchOnce(String path) throws FetchException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(toUri(path))
                .GET()
                .timeout(REQUEST_TIMEOUT)
                .build();
        HttpResponse<String> response;
        permits.acquire();
        try {
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException e) {
            throw new FetchException("I/O error for " + path, e, true);
        } finally {
            permits.release();
        }
        int status = response.statusCode();
        if (status != 200) {
            throw new FetchException("HTTP " + status + " for " + path, status >= 500);
        }
        return PageParser.parse(response.body());
    }

    private URI toUri(String path) throws FetchException {
        try {
            return URI.create(base + path);
        } catch (IllegalArgumentException e) {
            try {
                return new URI(base.getScheme(), null, base.getHost(), base.getPort(), path,
                        null, null);
            } catch (URISyntaxException ex) {
                throw new FetchException("Bad path: " + path, ex, false);
            }
        }
    }
}
