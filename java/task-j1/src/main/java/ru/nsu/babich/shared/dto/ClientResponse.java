package ru.nsu.babich.shared.dto;

import java.nio.channels.SocketChannel;
import ru.nsu.babich.shared.model.CertifiedKeyPair;

public record ClientResponse(SocketChannel socketChannel, CertifiedKeyPair certifiedKeyPair) {
}
