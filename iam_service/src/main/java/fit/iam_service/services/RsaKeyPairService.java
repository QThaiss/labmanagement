/*
 * @ {#} RsaKeyPairService.java   1.0     09/10/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */
      
package fit.iam_service.services;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Map;

/*
 * @description: Service for loading RSA key pair from PEM files
 * @author: Tran Hien Vinh
 * @date:   09/10/2025
 * @version:    1.0
 */
@Service
@Getter
public class RsaKeyPairService {
    private PrivateKey privateKey;

    private PublicKey publicKey;

    private String publicKeyBase64;

    @PostConstruct
    public void init() throws Exception {
        ClassPathResource privateRes = new ClassPathResource("keys/rsa-private.pem");
        byte[] privateBytes = privateRes.getInputStream().readAllBytes();
        String privateKeyPEM = new String(privateBytes)
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s+", "");
        byte[] decodedPrivate = Base64.getDecoder().decode(privateKeyPEM);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(decodedPrivate);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        privateKey = kf.generatePrivate(keySpec);

        // Đọc public key từ file
        ClassPathResource pubRes = new ClassPathResource("keys/rsa-public.pem");
        byte[] pubBytes = pubRes.getInputStream().readAllBytes();
        String pubKeyPEM = new String(pubBytes)
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s+", "");
        byte[] decodedPub = Base64.getDecoder().decode(pubKeyPEM);
        this.publicKey = kf.generatePublic(new X509EncodedKeySpec(decodedPub));
        publicKeyBase64 = pubKeyPEM;
    }

    public Map<String, Object> encryptPassword(String password) {
        try {
            if (password == null || password.isBlank()) {
                throw new IllegalArgumentException("Missing password field");
            }

            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            byte[] encryptedBytes = cipher.doFinal(password.getBytes(StandardCharsets.UTF_8));

            String encryptedBase64 = Base64.getEncoder().encodeToString(encryptedBytes);

            return Map.of(
                    "password", password,
                    "encrypted", encryptedBase64,
                    "length", encryptedBase64.length()
            );
        } catch (Exception e) {
            throw new RuntimeException("Encryption failed: " + e.getMessage(), e);
        }
    }
}
