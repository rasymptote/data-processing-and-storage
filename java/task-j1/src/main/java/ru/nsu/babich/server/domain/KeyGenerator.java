package ru.nsu.babich.server.domain;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;

public class KeyGenerator {

    private static final int KEY_SIZE = 8192;
    private static final String ALGORITHM = "RSA";

    private final KeyPairGenerator keyPairGenerator;

    public KeyGenerator() throws NoSuchAlgorithmException {
        this.keyPairGenerator = KeyPairGenerator.getInstance(ALGORITHM);
        this.keyPairGenerator.initialize(KEY_SIZE);
    }

    public KeyPair generate() {
        return keyPairGenerator.generateKeyPair();
    }
}