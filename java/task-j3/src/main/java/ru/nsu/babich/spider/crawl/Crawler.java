package ru.nsu.babich.spider.crawl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public final class Crawler {
    private final PageFetcher fetcher;
    private final Set<String> visited = ConcurrentHashMap.newKeySet();
    private final Queue<String> messages = new ConcurrentLinkedQueue<>();
    private final Map<String, String> failures = new ConcurrentHashMap<>();
    private final AtomicInteger pending = new AtomicInteger();
    private final CountDownLatch done = new CountDownLatch(1);
    private ExecutorService executor;

    public Crawler(PageFetcher fetcher) {
        this.fetcher = fetcher;
    }

    public CrawlResult crawl() throws InterruptedException {
        try (ExecutorService ex = Executors.newVirtualThreadPerTaskExecutor()) {
            this.executor = ex;
            submit("/");
            done.await();
        }
        List<String> sorted = new ArrayList<>(messages);
        Collections.sort(sorted);
        return new CrawlResult(sorted, Map.copyOf(failures));
    }

    static String normalize(String path) {
        return path.startsWith("/") ? path : "/" + path;
    }

    private void submit(String rawPath) {
        String path = normalize(rawPath);
        if (!visited.add(path)) {
            return;
        }
        pending.incrementAndGet();
        executor.submit(() -> {
            try {
                visit(path);
            } finally {
                if (pending.decrementAndGet() == 0) {
                    done.countDown();
                }
            }
        });
    }

    private void visit(String path) {
        try {
            Page page = fetcher.fetch(path);
            messages.add(page.message());
            page.successors().forEach(this::submit);
        } catch (FetchException e) {
            failures.put(path, e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            failures.put(path, "interrupted");
        }
    }
}
