package ru.nsu.babich.spider.crawl;

import java.util.List;

public record Page(String message, List<String> successors) {
    public Page {
        successors = List.copyOf(successors);
    }
}
