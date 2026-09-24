package ru.nsu.babich.shared.model;

import java.security.KeyPair;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;

public record CertifiedKeyPair(KeyPair keyPair, X509Certificate certificate) {
    public byte[] getPrivateKeyBytes() {
        return keyPair.getPrivate().getEncoded();
    }

    public byte[] getPublicKeyBytes() {
        return keyPair.getPublic().getEncoded();
    }

    public byte[] getCertificateBytes() throws CertificateEncodingException {
        return certificate.getEncoded();
    }
}
