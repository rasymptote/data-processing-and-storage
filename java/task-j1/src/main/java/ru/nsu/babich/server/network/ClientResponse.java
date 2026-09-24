package ru.nsu.babich.server.network;

import java.nio.channels.SocketChannel;
import ru.nsu.babich.server.domain.CertifiedKeyPair;

public record ClientResponse(SocketChannel socketChannel, CertifiedKeyPair certifiedKeyPair) {
}
