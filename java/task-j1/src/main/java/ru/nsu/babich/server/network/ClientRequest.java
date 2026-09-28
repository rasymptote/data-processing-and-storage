package ru.nsu.babich.server.network;

import java.nio.channels.SocketChannel;

public record ClientRequest(String clientName, SocketChannel socketChannel) {
}
