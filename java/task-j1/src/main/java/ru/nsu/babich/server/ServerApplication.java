package ru.nsu.babich.server;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.PrivateKey;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import ru.nsu.babich.server.domain.CertificateGenerator;
import ru.nsu.babich.server.domain.KeyGenerationService;
import ru.nsu.babich.server.domain.KeyGenerator;
import ru.nsu.babich.server.domain.KeyRepository;
import ru.nsu.babich.server.network.AcceptorThread;
import ru.nsu.babich.server.network.ClientResponse;
import ru.nsu.babich.server.network.SenderThread;

public class ServerApplication {

    private static final String USAGE =
            "Usage: server <port> <generator-threads> <issuer-key.pem> <issuer-name>\n"
            + "  issuer-name is an X.500 name, e.g. \"CN=NSU Key Server\"";

    public static void main(String[] args) throws Exception {
        if (args.length != 4) {
            System.err.println(USAGE);
            System.exit(1);
        }

        int port = Integer.parseInt(args[0]);
        int threadCount = Integer.parseInt(args[1]);
        PrivateKey issuerKey = readPrivateKey(Path.of(args[2]));
        String issuerName = args[3];

        if (threadCount < 1) {
            System.err.println("generator-threads must be positive");
            System.exit(1);
        }

        ExecutorService generatorPool = Executors.newFixedThreadPool(threadCount);
        BlockingQueue<ClientResponse> responseQueue = new LinkedBlockingQueue<>();
        KeyGenerationService keyGenerationService = new KeyGenerationService(
                generatorPool,
                responseQueue,
                new KeyRepository(),
                new KeyGenerator(),
                new CertificateGenerator(issuerKey, issuerName));

        AcceptorThread acceptor = new AcceptorThread(port, keyGenerationService);
        SenderThread sender = new SenderThread(responseQueue);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            acceptor.shutdown();
            keyGenerationService.shutdown();
            sender.shutdown();
        }));

        sender.start();
        acceptor.start();

        System.out.println("Listening on port " + port + " with " + threadCount + " generator threads");
        acceptor.join();
        keyGenerationService.shutdown();
        sender.shutdown();
    }

    private static PrivateKey readPrivateKey(Path path) throws IOException {
        try (Reader reader = Files.newBufferedReader(path); PEMParser parser = new PEMParser(reader)) {
            Object object = parser.readObject();
            JcaPEMKeyConverter converter = new JcaPEMKeyConverter();

            if (object instanceof PEMKeyPair keyPair) {
                return converter.getPrivateKey(keyPair.getPrivateKeyInfo());
            }
            if (object instanceof PrivateKeyInfo keyInfo) {
                return converter.getPrivateKey(keyInfo);
            }
            throw new IOException("No unencrypted private key found in " + path);
        }
    }
}
