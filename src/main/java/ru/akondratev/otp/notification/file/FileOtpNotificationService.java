package ru.akondratev.otp.notification.file;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;

public class FileOtpNotificationService {

    private final Path outputFilePath;

    public FileOtpNotificationService(String outputFilePath) {
        this.outputFilePath = Path.of(outputFilePath);
    }

    public void saveOtpCode(long userId, String operationId, String code) throws IOException {
        Path parent = outputFilePath.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }

        String line = String.format(
                "[%s] userId=%d, operationId=%s, code=%s%n",
                LocalDateTime.now(),
                userId,
                operationId,
                code
        );

        Files.writeString(
                outputFilePath,
                line,
                StandardOpenOption.CREATE,
                StandardOpenOption.APPEND
        );
    }
}