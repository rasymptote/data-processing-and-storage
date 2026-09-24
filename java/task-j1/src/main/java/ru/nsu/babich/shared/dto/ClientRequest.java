package ru.nsu.babich.shared.dto;

import java.nio.channels.SocketChannel;

public record ClientRequest(String clientName, SocketChannel socketChannel) {
}
