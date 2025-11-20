/*
 * @ {#} TestCodeGenerator.java   1.0     22/10/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package fit.test_order_service.utils;

import fit.test_order_service.entities.TestCatalog;
import fit.test_order_service.repositories.TestCatalogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

/*
 * @description: Utility class for generating and retrieving test codes
 * @author: Tran Hien Vinh
 * @date:   22/10/2025
 * @version:    1.0
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class TestCodeGenerator {

    private final TestCatalogRepository testCatalogRepository;

    private static final Pattern NON_ASCII = Pattern.compile("[^\\p{ASCII}]");
    private static final Pattern NON_ALNUM = Pattern.compile("[^A-Z0-9]");

    /**
     * Tìm local code theo raw test code (có thể là LOINC hoặc local code)
     */
    public String findLocalCodeByTestCode(String testCode) {
        if (testCode == null || testCode.trim().isEmpty()) {
            return null;
        }

        // Thử tìm theo local code trước
        Optional<TestCatalog> byLocal = testCatalogRepository
                .findByLocalCodeIgnoreCaseAndActiveTrue(testCode.trim());
        if (byLocal.isPresent()) {
            return byLocal.get().getLocalCode();
        }

        // Thử tìm theo LOINC code
        Optional<TestCatalog> byLoinc = testCatalogRepository
                .findByLoincCodeAndActiveTrue(testCode.trim());
        if (byLoinc.isPresent()) {
            return byLoinc.get().getLocalCode();
        }

        return null;
    }

    /**
     * Tìm local code theo tên xét nghiệm
     */
    public String findLocalCodeByTestName(String testName) {
        if (testName == null || testName.trim().isEmpty()) {
            return null;
        }

        Optional<TestCatalog> catalog = testCatalogRepository
                .findByTestNameIgnoreCaseAndActiveTrue(testName.trim());

        return catalog.map(TestCatalog::getLocalCode).orElse(null);
    }

    public String generateFromName(String analyteName) {
        if (analyteName == null || analyteName.isBlank()) {
            return generateTemporaryCode();
        }

        // Normalize and remove accents
        String normalized = Normalizer
                .normalize(analyteName, Normalizer.Form.NFD);
        normalized = NON_ASCII.matcher(normalized).replaceAll("");

        // Uppercase and keep only A-Z0-9, replace groups of invalid chars with '_'
        String upper = normalized.toUpperCase(Locale.ROOT);
        upper = NON_ALNUM.matcher(upper).replaceAll("_");

        // Collapse multiple '_' and trim
        upper = upper.replaceAll("_+", "_")
                .replaceAll("^_+", "")
                .replaceAll("_+$", "");

        if (upper.isBlank()) {
            return generateTemporaryCode();
        }

        // Add a short hash suffix to reduce collisions
        int hash = Math.abs(analyteName.hashCode());
        String suffix = Integer.toHexString(hash).toUpperCase(Locale.ROOT);
        // Keep suffix short
        if (suffix.length() > 4) {
            suffix = suffix.substring(0, 4);
        }

        return upper + "_" + suffix;
    }

    /**
     * Generate a random temporary code when no proper code or name is available.
     * Example: "TMP-3F9A7C"
     */
    public String generateTemporaryCode() {
        // Use random UUID, take first 6 hex chars
        String random = UUID.randomUUID().toString().replace("-", "").toUpperCase(Locale.ROOT);
        String shortPart = random.substring(0, 6);
        return "TMP-" + shortPart;
    }
}
