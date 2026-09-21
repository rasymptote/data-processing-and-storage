package ru.nsu.babich.server.network;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import ru.nsu.babich.shared.dto.ClientRequest;

public class ProtocolDecoder {

    private static final byte STREAM_TERMINATOR = 0x00;

    public ClientRequest decode(SocketChannel channel, ByteBuffer sessionBuffer) throws IOException {
        int bytesRead = channel.read(sessionBuffer);
        switch (bytesRead) {
            case -1 -> throw new IOException("Channel has reached end-of-stream");
            case 0 -> {
                return null;
            }
        }

        sessionBuffer.flip();

        int terminatorPosition = -1;
        for (int i = sessionBuffer.position(); i < sessionBuffer.limit(); i++) {
            if (sessionBuffer.get(i) == STREAM_TERMINATOR) {
                terminatorPosition = i;
                break;
            }
        }

        if (terminatorPosition == -1) {
            sessionBuffer.compact();
            return null;
        }

        String clientName = parseClientName(sessionBuffer, terminatorPosition);
        sessionBuffer.get();

        if (sessionBuffer.hasRemaining()) {
            sessionBuffer.compact();
        } else {
            sessionBuffer.clear();
        }

        return new ClientRequest(clientName);
    }

    private String parseClientName(ByteBuffer sessionBuffer, int terminatorPosition) {
        int nameLength = terminatorPosition - sessionBuffer.position();
        byte[] nameBytes = new byte[nameLength];
        sessionBuffer.get(nameBytes);
        return new String(nameBytes, StandardCharsets.US_ASCII);
    }
}
