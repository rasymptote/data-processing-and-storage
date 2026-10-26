package ru.nsu.babich.spider.crawl;

public interface PageFetcher {
    Page fetch(String path) throws FetchException, InterruptedException;
}
