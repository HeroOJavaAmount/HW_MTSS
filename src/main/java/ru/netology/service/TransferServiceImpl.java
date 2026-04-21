package ru.netology.service;

import org.springframework.stereotype.Service;
import ru.netology.exception.InvalidInputException;
import ru.netology.exception.TransferException;
import ru.netology.model.TransferRequest;
import ru.netology.repository.InMemoryTransferRepository;
import ru.netology.service.commission.MixedCommission;

import java.math.BigDecimal;

import static ru.netology.config.ConstantContainer.*;

@Service
public class TransferServiceImpl implements TransferService {

    private final InMemoryTransferRepository repository;
    private final MixedCommission commission;
    private final LoggingService logger;

    public TransferServiceImpl(InMemoryTransferRepository repository,
                               MixedCommission commission,
                               LoggingService logger) {
        this.repository = repository;
        this.commission = commission;
        this.logger = logger;
    }

    @Override
    public String initiateTransfer(TransferRequest request) {
        validate(request);

        if (!repository.cardExists(request.cardFromNumber())) {
            throw new InvalidInputException("Sender card not found-Не найдена карта оотправителя", ERROR_INVALID_CARD_DATA);
        }
        if (!repository.cardExists(request.cardToNumber())) {
            throw new InvalidInputException("Receiver card not found-Карта получателя нe найдена", ERROR_INVALID_CARD_DATA);
        }

        BigDecimal amount = BigDecimal.valueOf(request.amount().value(), 2);
        BigDecimal commissionAmount = commission.calculate(amount);
        BigDecimal totalDebit = amount.add(commissionAmount);

        if (!repository.hasSufficientFunds(request.cardFromNumber(), totalDebit)) {
            throw new TransferException("Insufficient funds-Нет достаточно денег", ERROR_TRANSFER_FAILED);
        }

        String operationId = repository.saveOperation(request);
        logger.logTransfer(request, operationId, "PENDING", commissionAmount.doubleValue());

        return operationId;
    }

    @Override
    public String confirmOperation(String operationId, String code) {
        if (operationId == null || operationId.isBlank()) {
            throw new InvalidInputException("Operation ID required-ID операции обязателен", ERROR_OPERATION_NOT_FOUND);
        }
        if (!DEFAULT_VERIFICATION_CODE.equals(code)) {
            throw new InvalidInputException("Invalid verification code-Неверный код", ERROR_INVALID_CODE);
        }

        TransferRequest request = repository.getOperation(operationId);
        if (request == null) {
            throw new InvalidInputException("Operation not found-Операцию найти невозможно", ERROR_OPERATION_NOT_FOUND);
        }

        String status = repository.getStatus(operationId);
        if (!"PENDING".equals(status)) {
            throw new InvalidInputException("Operation already processed-Данная оперция уже обработана", ERROR_OPERATION_NOT_FOUND);
        }

        BigDecimal amount = BigDecimal.valueOf(request.amount().value(), 2);
        BigDecimal commissionAmount = commission.calculate(amount);
        BigDecimal totalDebit = amount.add(commissionAmount);

        if (!repository.hasSufficientFunds(request.cardFromNumber(), totalDebit)) {
            repository.updateStatus(operationId, "FAILED");
            logger.logTransfer(request, operationId, "FAILED", commissionAmount.doubleValue());
            throw new TransferException("Insufficient funds-Не хватает средств", ERROR_TRANSFER_FAILED);
        }

        try {
            repository.withdraw(request.cardFromNumber(), totalDebit);
            repository.deposit(request.cardToNumber(), amount);

            repository.updateStatus(operationId, "SUCCESS");
            logger.logTransfer(request, operationId, "SUCCESS", commissionAmount.doubleValue());

            return operationId;
        } catch (Exception e) {
            repository.updateStatus(operationId, "FAILED");
            logger.logTransfer(request, operationId, "FAILED", commissionAmount.doubleValue());
            throw new TransferException("Transfer failed-Перевод провален", ERROR_TRANSFER_FAILED);
        }
    }
    // В учебных целях проверяем только формат срока и CVV через аннотации в DTO, дополнительная бизнес-валидация не требуется.
    private void validate(TransferRequest request) {
        if (request.cardFromNumber().equals(request.cardToNumber())) {
            throw new InvalidInputException("Same card transfer not allowed-Одинаковые карты запрешены", ERROR_SAME_CARD);
        }

        String currency = request.amount().currency();
        if (currency == null || !("RUB".equalsIgnoreCase(currency) || "RUR".equalsIgnoreCase(currency))) {
            throw new InvalidInputException("Unsupported currency-Валюта недопустимая", ERROR_INVALID_CURRENCY);
        }

        // Конвертируем копейки в рубли проверяем лимиты.
        BigDecimal amount = BigDecimal.valueOf(request.amount().value(), 2);
        if (!commission.isValidAmount(amount)) {
            throw new InvalidInputException("Invalid amount-Сумма перевода некорректна", ERROR_INVALID_AMOUNT);
        }
    }
}