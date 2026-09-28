package ru.nsu.babich.server.domain;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class KeyRepository {
    private final Map<String, CompletableFuture<CertifiedKeyPair>> keys = new ConcurrentHashMap<>();

    public CompletableFuture<CertifiedKeyPair> putIfAbsent(String clientName,
                                                           CompletableFuture<CertifiedKeyPair> pending) {
        return keys.putIfAbsent(clientName, pending);
    }

    public void remove(String clientName, CompletableFuture<CertifiedKeyPair> pending) {
        keys.remove(clientName, pending);
    }
}
