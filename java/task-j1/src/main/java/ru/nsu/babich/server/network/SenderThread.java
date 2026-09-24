package ru.nsu.babich.server.network;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.concurrent.BlockingQueue;
import ru.nsu.babich.shared.dto.ClientResponse;

public class SenderThread extends Thread {

    private static final long SELECT_TIMEOUT_MS = 100;

    private final BlockingQueue<ClientResponse> responseQueue;
    private final ProtocolEncoder encoder = new ProtocolEncoder();

    private Selector selector;
    private volatile boolean running = true;

    public SenderThread(BlockingQueue<ClientResponse> responseQueue) {
        super("sender");
        this.responseQueue = responseQueue;
    }

    @Override
    public void run() {
        try {
            selector = Selector.open();
            sendLoop();
        } catch (IOException ignored) {}
        finally {
            cleanup();
        }
    }

    private void sendLoop() throws IOException {
        while (running) {
            registerNewResponses();

            if (selector.select(SELECT_TIMEOUT_MS) == 0) {
                continue;
            }

            Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();

            while (iterator.hasNext()) {
                SelectionKey key = iterator.next();
                iterator.remove();

                try {
                    if (key.isWritable()) {
                        handleWrite(key);
                    }
                } catch (IOException e) {
                    closeChannel(key);
                }
            }
        }
    }

    private void registerNewResponses() {
        ClientResponse response;
        while ((response = responseQueue.poll()) != null) {
            SocketChannel channel = response.socketChannel();
            try {
                ByteBuffer data = encoder.encode(response.certifiedKeyPair());
                channel.register(selector, SelectionKey.OP_WRITE, data);
            } catch (Exception e) {
                try {
                    channel.close();
                } catch (IOException ignored) {}
            }
        }
    }

    private void handleWrite(SelectionKey key) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        ByteBuffer data = (ByteBuffer) key.attachment();

        channel.write(data);

        if (!data.hasRemaining()) {
            closeChannel(key);
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

        if (selector == null) {
            return;
        }

        for (SelectionKey key : selector.keys()) {
            closeChannel(key);
        }

        try {
            selector.close();
        } catch (IOException ignored) {}
    }

    public void shutdown() {
        running = false;
        if (selector != null) {
            selector.wakeup();
        }
    }
}
