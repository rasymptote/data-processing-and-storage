package ru.nsu.babich.server.network;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import ru.nsu.babich.shared.dto.ClientRequest;

public class ClientSession {

    private static final int BUFFER_SIZE = 256;
    private static final byte TERMINATOR = 0x00;

    private final SocketChannel channel;
    private final ByteBuffer buffer;
    private String clientName;
    private boolean nameReceived = false;

    public ClientSession(SocketChannel channel) {
        this.channel = channel;
        this.buffer = ByteBuffer.allocate(BUFFER_SIZE);
    }

    public ClientRequest tryGetRequest() throws IOException {
        if (nameReceived) {
            return new ClientRequest(clientName, channel);
        }

        int bytesRead = channel.read(buffer);

        if (bytesRead == -1) {
            throw new IOException("Channel closed by client");
        }

        if (bytesRead == 0) {
            return null;
        }

        buffer.flip();

        int terminatorPosition = findTerminator();

        if (terminatorPosition == -1) {
            buffer.compact();

            if (!buffer.hasRemaining()) {
                throw new IOException("Client name too long (> " + BUFFER_SIZE + " bytes)");
            }

            return null;
        }

        extractClientName(terminatorPosition);
        nameReceived = true;

        return new ClientRequest(clientName, channel);
    }

    private int findTerminator() {
        int position = buffer.position();
        int limit = buffer.limit();

        for (int i = position; i < limit; i++) {
            if (buffer.get(i) == TERMINATOR) {
                return i;
            }
        }

        return -1;
    }

    private void extractClientName(int terminatorPos) {
        int position = buffer.position();
        int nameLength = terminatorPos - position;

        byte[] nameBytes = new byte[nameLength];
        buffer.get(nameBytes);
        buffer.get();

        clientName = new String(nameBytes, StandardCharsets.US_ASCII);
    }
}