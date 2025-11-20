/*
 * @ {#} EmailServiceImpl.java   1.0     05/10/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package fit.iam_service.services.impl;

import fit.iam_service.entities.User;
import fit.iam_service.services.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

/*
 * @description: Service implementation for handling email operations
 * @author: Tran Hien Vinh
 * @date:   05/10/2025
 * @version:    1.0
 */
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {
    private final JavaMailSender mailSender;

    @Value("${app.reset-password-url}")
    private String resetPasswordUrl;

    @Override
    public void sendPasswordResetEmail(User user, String resetToken) {
        // Create reset link
        String link = resetPasswordUrl + "/" + resetToken;

        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");

            String htmlContent = """
                                    <html>
                                        <head>
                                            <meta charset="UTF-8">
                                        </head>
                                        <body style="font-family: Arial, sans-serif; line-height: 1.6; max-width: 800px; margin: 0 auto; padding: 20px; font-size: medium;">
                                            <div class="email-container">
                                                <div class="header" style="margin-bottom: 20px;">
                                                    <p style="font-size: medium; margin: 0; color: #212529;">Xin chào <strong>%s</strong>,</p>
                                                </div>
                    
                                                <div class="content" style="margin-bottom: 20px;">
                                                    <p style="font-size: medium; margin: 0; color: #212529;">
                                                        Chúng tôi nhận được yêu cầu đặt lại mật khẩu của bạn.\s
                                                        Nếu bạn không yêu cầu, bạn có thể bỏ qua email này.\s
                                                        Nếu thực sự bạn quên mật khẩu, hãy <strong>click ngay vào nút bên dưới\s
                                                        hoặc copy đường link này</strong> vào trình duyệt để đặt lại mật khẩu cho tài khoản.
                                                    </p>
                                                </div>
                    
                                                <div class="link-container" style="margin: 25px 0; padding: 15px; background-color: #f8f9fa; border: 2px solid #e9ecef; border-radius: 8px; text-align: center;">
                                                    <a href="%s" style="display: inline-block; background-color: #007bff; color: white !important; padding: 12px 30px; text-decoration: none; border-radius: 5px; font-weight: bold; margin: 10px 0; font-size: medium;">ĐẶT LẠI MẬT KHẨU</a>
                                                    <br><br>
                                                    <div style="font-size: medium; margin: 10px 0; color: #212529;">Hoặc copy link này:</div>
                                                    <a href="%s" style="display: inline-block; font-size: medium; color: #007bff; text-decoration: underline; word-break: break-all; line-height: 1.4;">%s</a>
                                                </div>
                    
                                                <p style="font-size: medium; color: #dc3545; font-weight: bold; margin: 15px 0;">⚠️ Link này chỉ có hiệu lực trong 15 phút.</p>
                                            </div>
                                        </body>
                                    </html>
                    """.formatted(user.getFullName(), link, link, link);

            helper.setTo(user.getEmail());
            helper.setSubject("Lấy lại mật khẩu đăng nhập");
            helper.setText(htmlContent, true); // true = send HTML

            // Send the email
            mailSender.send(mimeMessage);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send email", e);
        }

    }

    @Override
    public void sendEmailVerifyOtp(User user, String otp) {
        try {
            MimeMessage msg = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(msg, "utf-8");

            String html = """
                        <html><body style="font-family:Arial,sans-serif;line-height:1.6;">
                          <p>Xin chào <strong>%s</strong>,</p>
                          <p>Mã xác minh email của bạn là:</p>
                          <div style="font-size:22px;font-weight:bold;border:2px dashed #0d6efd;display:inline-block;padding:8px 16px;">%s</div>
                          <p style="color:#dc3545;margin-top:12px;font-weight:600;">Mã có hiệu lực trong 5 phút.</p>
                          <p>Nếu bạn không yêu cầu, hãy bỏ qua email này.</p>
                        </body></html>
                    """.formatted(user.getFullName(), otp);

            helper.setTo(user.getEmail());
            helper.setSubject("Mã xác minh email");
            helper.setText(html, true);
            mailSender.send(msg);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send verification OTP", e);
        }
    }
}
