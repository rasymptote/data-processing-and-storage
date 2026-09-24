package ru.nsu.babich.server.domain;

import java.io.IOException;
import java.security.KeyPair;
import java.security.cert.X509Certificate;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import ru.nsu.babich.server.network.ClientRequest;
import ru.nsu.babich.server.network.ClientResponse;

public class KeyGenerationService {
    private final ExecutorService generatorPool;
    private final BlockingQueue<ClientResponse> responseQueue;
    private final KeyRepository keyRepository;
    private final KeyGenerator keyGenerator;
    private final CertificateGenerator certGenerator;

    public KeyGenerationService(
            ExecutorService generatorPool,
            BlockingQueue<ClientResponse> responseQueue,
            KeyRepository keyRepository,
            KeyGenerator keyGenerator,
            CertificateGenerator certGenerator) {

        this.generatorPool = generatorPool;
        this.responseQueue = responseQueue;
        this.keyRepository = keyRepository;
        this.keyGenerator = keyGenerator;
        this.certGenerator = certGenerator;
    }

    public void submit(ClientRequest request) {
        String clientName = request.clientName();
        CompletableFuture<CertifiedKeyPair> pending = new CompletableFuture<>();
        CompletableFuture<CertifiedKeyPair> existing = keyRepository.putIfAbsent(clientName, pending);

        CompletableFuture<CertifiedKeyPair> keyPair = existing != null ? existing : pending;
        keyPair.whenComplete((result, error) -> {
            if (error == null) {
                responseQueue.add(new ClientResponse(request.socketChannel(), result));
            } else {
                closeQuietly(request);
            }
        });

        if (existing == null) {
            try {
                generatorPool.execute(() -> generate(clientName, pending));
            } catch (RuntimeException e) {
                keyRepository.remove(clientName, pending);
                pending.completeExceptionally(e);
            }
        }
    }

    private void generate(String clientName, CompletableFuture<CertifiedKeyPair> pending) {
        try {
            KeyPair keyPair = keyGenerator.generate();
            X509Certificate certificate = certGenerator.generate(keyPair.getPublic(), clientName);
            pending.complete(new CertifiedKeyPair(keyPair, certificate));
        } catch (Exception e) {
            keyRepository.remove(clientName, pending);
            pending.completeExceptionally(e);
        }
    }

    private static void closeQuietly(ClientRequest request) {
        try {
            request.socketChannel().close();
        } catch (IOException ignored) {}
    }

    public void shutdown() {
        generatorPool.shutdownNow();
    }
}
