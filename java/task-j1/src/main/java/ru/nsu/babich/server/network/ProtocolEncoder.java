package ru.nsu.babich.server.network;

import java.nio.ByteBuffer;
import java.security.cert.CertificateEncodingException;
import ru.nsu.babich.server.domain.model.CertifiedKeyPair;

public class ProtocolEncoder {

    private static final int LENGTH_HEADER_SIZE = Integer.BYTES;

    public ByteBuffer encode(CertifiedKeyPair response) {
        try {
            byte[] privateKeyBytes = response.getPrivateKeyBytes();
            byte[] publicKeyBytes = response.getPublicKeyBytes();
            byte[] certificateBytes = response.getCertificateBytes();

            int responseSize = LENGTH_HEADER_SIZE + privateKeyBytes.length
                    + LENGTH_HEADER_SIZE + publicKeyBytes.length
                    + LENGTH_HEADER_SIZE + certificateBytes.length;
            ByteBuffer responseBuffer = ByteBuffer.allocate(responseSize);

            responseBuffer.putInt(privateKeyBytes.length);
            responseBuffer.put(privateKeyBytes);

            responseBuffer.putInt(publicKeyBytes.length);
            responseBuffer.put(publicKeyBytes);

            responseBuffer.putInt(certificateBytes.length);
            responseBuffer.put(certificateBytes);

            responseBuffer.flip();
            return responseBuffer;
        } catch (CertificateEncodingException e) {
            throw new RuntimeException(e);
        }
    }
}
