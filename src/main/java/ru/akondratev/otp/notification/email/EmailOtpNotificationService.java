package ru.akondratev.otp.notification.email;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.util.Properties;

public class EmailOtpNotificationService {

    private final String smtpHost;
    private final int smtpPort;
    private final String smtpUsername;
    private final String smtpPassword;
    private final String fromEmail;
    private final boolean smtpAuth;
    private final boolean startTlsEnabled;

    public EmailOtpNotificationService(
            String smtpHost,
            int smtpPort,
            String smtpUsername,
            String smtpPassword,
            String fromEmail,
            boolean smtpAuth,
            boolean startTlsEnabled
    ) {
        this.smtpHost = smtpHost;
        this.smtpPort = smtpPort;
        this.smtpUsername = smtpUsername;
        this.smtpPassword = smtpPassword;
        this.fromEmail = fromEmail;
        this.smtpAuth = smtpAuth;
        this.startTlsEnabled = startTlsEnabled;
    }

    public void sendOtpCode(String toEmail, long userId, String operationId, String code) throws Exception {
        if (toEmail == null || toEmail.isBlank()) {
            throw new IllegalArgumentException("Email получателя обязателен");
        }

        Properties properties = new Properties();
        properties.put("mail.smtp.host", smtpHost);
        properties.put("mail.smtp.port", String.valueOf(smtpPort));
        properties.put("mail.smtp.auth", String.valueOf(smtpAuth));
        properties.put("mail.smtp.starttls.enable", String.valueOf(startTlsEnabled));

        Session session;
        if (smtpAuth) {
            session = Session.getInstance(properties, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(smtpUsername, smtpPassword);
                }
            });
        } else {
            session = Session.getInstance(properties);
        }

        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(fromEmail));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
        message.setSubject("Your OTP Code");

        String html = """
        <html>
        <body style="margin:0; padding:0; background-color:#f4f4f7; font-family:Arial, sans-serif; color:#222222;">
            <table role="presentation" width="100%%" cellspacing="0" cellpadding="0" style="background-color:#f4f4f7; padding:40px 0;">
                <tr>
                    <td align="center">
                        <table role="presentation" width="600" cellspacing="0" cellpadding="0"
                               style="background:#ffffff; border-radius:12px; padding:40px; box-shadow:0 4px 12px rgba(0,0,0,0.08);">
                            <tr>
                                <td>
                                    <h1 style="margin:0 0 24px; font-size:32px; line-height:1.2; color:#111111;">
                                        Your OTP Code
                                    </h1>

                                    <p style="margin:0 0 16px; font-size:18px; color:#333333;">
                                        Use the following one-time password to continue:
                                    </p>

                                    <div style="margin:24px 0; padding:20px; text-align:center; background:#f7f8fa; border:1px solid #e5e7eb; border-radius:10px;">
                                        <span style="font-size:36px; font-weight:bold; letter-spacing:6px; color:#111111;">
                                            %s
                                        </span>
                                    </div>

                                    <p style="margin:0 0 10px; font-size:18px; color:#333333;">
                                        <strong>Operation ID:</strong> %s
                                    </p>

                                    <p style="margin:0 0 24px; font-size:18px; color:#333333;">
                                        <strong>User ID:</strong> %d
                                    </p>

                                    <p style="margin:0 0 10px; font-size:16px; color:#666666;">
                                        This code is valid for a limited time.
                                    </p>

                                    <p style="margin:0; font-size:14px; color:#999999;">
                                        If you did not request this code, you can ignore this email.
                                    </p>
                                </td>
                            </tr>
                        </table>
                    </td>
                </tr>
            </table>
        </body>
        </html>
        """.formatted(code, operationId, userId);

        message.setContent(html, "text/html; charset=UTF-8");

        Transport.send(message);
    }
}