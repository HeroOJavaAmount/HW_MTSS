package ru.netology.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.netology.dto.TransferRequest;
import java.math.BigDecimal;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class LoggingServiceImpl implements LoggingService {

    private static final Logger log = LoggerFactory.getLogger(LoggingServiceImpl.class);
    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final Path logFilePath;
    private final Object logLock = new Object();

    public LoggingServiceImpl(@Value("${app.logging.file-path:transfer.log}") String logFilePath) {
        this.logFilePath = Paths.get(logFilePath);
        try {
            Path parent = this.logFilePath.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            if (!Files.exists(this.logFilePath)) {
                Files.createFile(this.logFilePath);
            }
        } catch (IOException e) {
            throw new RuntimeException("Не удалось создать файл лога: " + logFilePath, e);
        }
    }

    @Override
    public void logTransfer(TransferRequest request, String operationId,
                            String status, double commission) {
        String logEntry = buildLogEntry(request, operationId, status, commission);
        log.info("Transfer operation: {}", logEntry);
        writeToFile(logEntry);
    }

    private String buildLogEntry(TransferRequest request, String operationId,
                                 String status, double commission) {
        BigDecimal amountRub = BigDecimal.valueOf(request.amount().value(), 2);
        return String.format(
                "%s | From: %s | To: %s | Amount: %d %s | Commission: %.2f | Status: %s | OperationID: %s",
                LocalDateTime.now().format(DATE_FORMAT),
                maskCardNumber(request.cardFromNumber()),
                maskCardNumber(request.cardToNumber()),
                request.amount().value(),
                request.amount().currency(),
                commission,
                status,
                operationId
        );
    }

    private String maskCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 8) {
            return "****";
        }
        return "****" + cardNumber.substring(cardNumber.length() - 4);
    }

    private void writeToFile(String logEntry) {
        synchronized (logLock) {
            try (PrintWriter out = new PrintWriter(
                    new BufferedWriter(new FileWriter(logFilePath.toFile(), true)))) {
                out.println(logEntry);
            } catch (IOException e) {
                log.error("Failed to write to log file", e);
            }
        }
    }
}
