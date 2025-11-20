/*
 * @ {#} RsaDecryptUtils.java   1.0     09/10/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package fit.iam_service.utils;

import fit.iam_service.services.RsaKeyPairService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/*
 * @description: Utility class for RSA decryption
 * @author: Tran Hien Vinh
 * @date:   09/10/2025
 * @version:    1.0
 */
@Component
@RequiredArgsConstructor
public class RsaDecryptUtils {
    private final RsaKeyPairService rsaService;

    public String decrypt(String encrypted) {
        try {
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.DECRYPT_MODE, rsaService.getPrivateKey());
            byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encrypted));
            return new String(decryptedBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Failed to decrypt password", e);
        }
    }
}
