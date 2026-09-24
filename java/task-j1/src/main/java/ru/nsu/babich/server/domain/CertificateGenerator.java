package ru.nsu.babich.server.domain;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Date;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.X500NameBuilder;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;


public class CertificateGenerator {

    private static final String SIGNING_ALGORITHM = "SHA256WithRSAEncryption";
    private static final long CERTIFICATE_VALIDITY_MS = 365L * 24 * 60 * 60 * 1000;
    private static final int SERIAL_NUMBER_BITS = 64;

    private final SecureRandom random = new SecureRandom();

    private final PrivateKey privateKey;
    private final X500Name issuer;

    public CertificateGenerator(PrivateKey privateKey, String issuerName) {
        this.privateKey = privateKey;
        this.issuer = new X500Name(issuerName);
    }

    public X509Certificate generate(PublicKey publicKey, String subjectName) throws OperatorCreationException,
            CertificateException {
        X500Name subject = new X500NameBuilder(BCStyle.INSTANCE)
                .addRDN(BCStyle.CN, subjectName)
                .build();

        BigInteger serialNumber = new BigInteger(SERIAL_NUMBER_BITS, random);

        Date notBefore = new Date();
        Date notAfter = new Date(System.currentTimeMillis() + CERTIFICATE_VALIDITY_MS);

        SubjectPublicKeyInfo pubKeyInfo = SubjectPublicKeyInfo.getInstance(
                publicKey.getEncoded()
        );

        X509v3CertificateBuilder certificateBuilder = new X509v3CertificateBuilder(
                issuer,
                serialNumber,
                notBefore,
                notAfter,
                subject,
                pubKeyInfo
        );

        var contentSigner = new JcaContentSignerBuilder(SIGNING_ALGORITHM)
                .build(privateKey);

        return new JcaX509CertificateConverter()
                .getCertificate(certificateBuilder.build(contentSigner));
    }
}
