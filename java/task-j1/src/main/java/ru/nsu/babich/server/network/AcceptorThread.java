package ru.nsu.babich.server.network;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;
import ru.nsu.babich.server.domain.KeyGenerationService;

public class AcceptorThread extends Thread {

    private final KeyGenerationService keyGenerationService;
    private final ServerSocketChannel serverSocketChannel;
    private final Selector selector;
    private volatile boolean running = true;

    public AcceptorThread(int port, KeyGenerationService keyGenerationService) throws IOException {
        this.keyGenerationService = keyGenerationService;
        this.serverSocketChannel = ServerSocketChannel.open();

        try {
            serverSocketChannel.socket().setReuseAddress(true);
            serverSocketChannel.bind(new InetSocketAddress(port));
            serverSocketChannel.configureBlocking(false);

            selector = Selector.open();
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        } catch (IOException e) {
            serverSocketChannel.close();
            throw e;
        }
    }

    @Override
    public void run() {
        try {
            acceptLoop();
        } catch (IOException ignored) {}
        finally {
            cleanup();
        }
    }

    private void acceptLoop() throws IOException {
        while (running) {
            int readyCount = selector.select(1000);

            if (readyCount == 0) {
                continue;
            }

            Set<SelectionKey> selectedKeys = selector.selectedKeys();
            Iterator<SelectionKey> iterator = selectedKeys.iterator();

            while (iterator.hasNext()) {
                SelectionKey key = iterator.next();
                iterator.remove();

                try {
                    if (key.isAcceptable()) {
                        handleAccept(key);
                    } else if (key.isReadable()) {
                        handleRead(key);
                    }
                } catch (Exception e) {
                    closeChannel(key);
                }
            }
        }
    }

    private void handleAccept(SelectionKey key) throws IOException {
        ServerSocketChannel server = (ServerSocketChannel) key.channel();
        SocketChannel clientChannel = server.accept();

        if (clientChannel == null) {
            return;
        }

        clientChannel.configureBlocking(false);

        ClientSession session = new ClientSession(clientChannel);
        clientChannel.register(selector, SelectionKey.OP_READ, session);
    }

    private void handleRead(SelectionKey key) throws Exception {
        ClientSession session = (ClientSession) key.attachment();

        ClientRequest request = session.tryGetRequest();

        if (request != null) {
            key.cancel();
            keyGenerationService.submit(request);
        }
    }

    private void closeChannel(SelectionKey key) {
        key.cancel();
        try {
            key.channel().close();
        } catch (IOException ignored) {}
    }

    private void cleanup() {
        running = false;

        try {
            selector.close();
        } catch (IOException ignored) {}

        try {
            serverSocketChannel.close();
        } catch (IOException ignored) {}
    }

    public void shutdown() {
        running = false;
        selector.wakeup();
    }
}