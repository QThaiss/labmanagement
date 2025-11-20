/*
 * @ (#) OtpServiceImpl.java    1.0    06/10/2025
 * Copyright (c) 2025 IUH. All rights reserved.
 */
package fit.iam_service.services.impl;/*
 * @description:
 * @author: Bao Thong
 * @date: 06/10/2025
 * @version: 1.0
 */

import fit.iam_service.entities.EmailOtp;
import fit.iam_service.entities.User;
import fit.iam_service.enums.DeletedReason;
import fit.iam_service.enums.OtpPurpose;
import fit.iam_service.exceptions.*;
import fit.iam_service.repositories.EmailOtpRepository;
import fit.iam_service.services.EmailService;
import fit.iam_service.services.OtpService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Service
@RequiredArgsConstructor
public class OtpServiceImpl implements OtpService {
    private final EmailOtpRepository otpRepo;
    private final PasswordEncoder encoder;
    private final EmailService emailService;

    private static final Duration TTL = Duration.ofMinutes(5);
    private static final int MAX_ATTEMPTS = 5;
    private static final long RESEND_COOLDOWN_SECONDS = 60;

    private static String generateNumericOtp(int len) {
        SecureRandom r = new SecureRandom();
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) sb.append(r.nextInt(10));
        return sb.toString();
    }

    @Override
    @Transactional
    public void createAndSendEmailVerifyOtp(User user) {
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);

        if (Boolean.TRUE.equals(user.getEmailVerified())) {
            throw new AlreadyVerifiedException("Email đã được xác minh");
        }

        // 1) Lấy OTP đang hoạt động (nếu có)
        EmailOtp active = otpRepo.findFirstByUserAndPurposeAndDeletedFalseOrderByCreatedAtDesc(
                user, OtpPurpose.EMAIL_VERIFY).orElse(null);

        // 2) Cooldown 60s dựa trên active
        if (active != null) {
            LocalDateTime lastTouched = active.getUpdatedAt() != null ? active.getUpdatedAt() : active.getCreatedAt();
            long diff = java.time.Duration.between(lastTouched, now).getSeconds();
            if (diff < RESEND_COOLDOWN_SECONDS) {
                long retryAfter = RESEND_COOLDOWN_SECONDS - diff;
                throw new TooManyRequestsException("Vui lòng đợi trước khi gửi lại mã", retryAfter);
            }

            // 3) Soft-delete bản cũ để giữ lịch sử
            active.setDeleted(true);
            active.setDeletedAt(now);
            active.setDeletedReason(DeletedReason.REPLACED); // hoặc REVOKED nếu không muốn thêm enum
            otpRepo.saveAndFlush(active);
        }

        // 4) Tạo bản ghi OTP mới (id UUID @PrePersist)
        String rawOtp = generateNumericOtp(6);
        String hash = encoder.encode(rawOtp);

        EmailOtp fresh = EmailOtp.builder()
                .user(user)
                .purpose(OtpPurpose.EMAIL_VERIFY)
                .codeHash(hash)
                .attempts(0)
                .maxAttempts(MAX_ATTEMPTS)
                .expiresAt(now.plus(TTL))
                .build();

        otpRepo.saveAndFlush(fresh);

        // 5) Gửi mail OTP
        emailService.sendEmailVerifyOtp(user, rawOtp);
    }

    @Override
    @Transactional
    public boolean verifyEmailOtp(User user, String rawOtp) {
        EmailOtp otp = otpRepo.findFirstByUserAndPurposeAndDeletedFalseOrderByCreatedAtDesc(user, OtpPurpose.EMAIL_VERIFY)
                .orElseThrow(() -> new OtpNotFoundException("OTP không tồn tại hoặc đã được xác minh"));

        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);

        // Hết hạn -> soft delete (EXPIRED)
        if (now.isAfter(otp.getExpiresAt())) {
            otp.setDeleted(true);
            otp.setDeletedAt(now);
            otp.setDeletedReason(DeletedReason.EXPIRED);
            otpRepo.saveAndFlush(otp);
            throw new OtpExpiredException("OTP đã hết hạn, vui lòng yêu cầu mã mới");
        }

        // Vượt số lần -> soft delete (ATTEMPTS_EXCEEDED)
        if (otp.getAttempts() >= otp.getMaxAttempts()) {
            otp.setDeleted(true);
            otp.setDeletedAt(now);
            otp.setDeletedReason(DeletedReason.ATTEMPTS_EXCEEDED);
            otpRepo.saveAndFlush(otp);
            throw new OtpAttemptsExceededException("Bạn đã nhập sai quá số lần cho phép, vui lòng yêu cầu mã mới");
        }

        boolean ok = encoder.matches(rawOtp, otp.getCodeHash());
        if (!ok) {
            otp.setAttempts(otp.getAttempts() + 1);
            // không xóa, user có thể thử tiếp tới khi đạt max
            otpRepo.saveAndFlush(otp);
            return false;
        }

        // Thành công -> đánh dấu CONSUMED + soft delete
        otp.setConsumedAt(now);
        otp.setDeleted(true);
        otp.setDeletedAt(now);
        otp.setDeletedReason(DeletedReason.CONSUMED);
        otpRepo.saveAndFlush(otp);

        return true;
    }

}