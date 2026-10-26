package ru.nsu.babich.spider.crawl;

import java.util.List;
import java.util.Map;

public record CrawlResult(List<String> messages, Map<String, String> failures) {
    public boolean isComplete() {
        return failures.isEmpty();
    }
}
