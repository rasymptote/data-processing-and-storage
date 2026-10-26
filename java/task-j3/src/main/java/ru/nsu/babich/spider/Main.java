package ru.nsu.babich.spider;

import java.net.URI;
import java.util.Map;
import ru.nsu.babich.spider.crawl.CrawlResult;
import ru.nsu.babich.spider.crawl.Crawler;
import ru.nsu.babich.spider.http.HttpPageFetcher;
import ru.nsu.babich.spider.crawl.PageFetcher;

public class Main {
    private static final int MAX_CONCURRENT_REQUESTS = 1000;

    public static void main(String[] args) throws InterruptedException {
        int port = args.length > 0 ? Integer.parseInt(args[0]) : 8080;
        PageFetcher fetcher = new HttpPageFetcher(URI.create("http://localhost:" + port), MAX_CONCURRENT_REQUESTS);

        CrawlResult result = new Crawler(fetcher).crawl();

        result.messages().forEach(System.out::println);
        if (!result.isComplete()) {
            System.err.println("Incomplete crawl, failed paths: " + result.failures().size());
            for (Map.Entry<String, String> e : result.failures().entrySet()) {
                System.err.println("  " + e.getKey() + " -> " + e.getValue());
            }
            System.exit(1);
        }
    }
}
