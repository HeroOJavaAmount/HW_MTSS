package ru.netology.service;

import org.springframework.stereotype.Service;
import ru.netology.exception.InvalidInputException;
import ru.netology.exception.TransferException;
import ru.netology.dto.TransferRequest;
import ru.netology.repository.InMemoryTransferRepository;
import ru.netology.service.commission.MixedCommission;

import java.math.BigDecimal;
import java.math.RoundingMode;

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
        
        BigDecimal amountKop = BigDecimal.valueOf(request.amount().value());
        BigDecimal amountRub = BigDecimal.valueOf(request.amount().value());

        BigDecimal commissionRub = commission.calculate(amountRub);
        BigDecimal commissionKop = commissionRub.movePointRight(2).setScale(0, RoundingMode.HALF_UP);
        BigDecimal totalDebitKop = amountKop.add(commissionKop);

        if (!repository.hasSufficientFunds(request.cardFromNumber(), totalDebitKop)) {
            throw new TransferException("Insufficient funds-Не достаточно денег на счете", ERROR_TRANSFER_FAILED);
        }

        String operationId = repository.saveOperation(request);
        logger.logTransfer(request, operationId, "PENDING", commissionRub.doubleValue());
        return operationId;
    }

    @Override
    public String confirmOperation(String operationId, String code) {
        if (operationId == null || operationId.isBlank()) {
            throw new InvalidInputException("Operation ID required", ERROR_OPERATION_NOT_FOUND);
        }
        if (!DEFAULT_VERIFICATION_CODE.equals(code)) {
            throw new InvalidInputException("Invalid verification code", ERROR_INVALID_CODE);
        }

        TransferRequest request = repository.getOperation(operationId);
        if (request == null) {
            throw new InvalidInputException("Operation not found", ERROR_OPERATION_NOT_FOUND);
        }

        String status = repository.getStatus(operationId);
        if (!"PENDING".equals(status)) {
            throw new InvalidInputException("Operation already processed", ERROR_OPERATION_NOT_FOUND);
        }

        BigDecimal amountKop = BigDecimal.valueOf(request.amount().value());
        BigDecimal amountRub = BigDecimal.valueOf(request.amount().value());
        BigDecimal commissionRub = commission.calculate(amountRub);
        BigDecimal commissionKop = commissionRub.movePointRight(2).setScale(0, RoundingMode.HALF_UP);
        BigDecimal totalDebitKop = amountKop.add(commissionKop);

        if (!repository.hasSufficientFunds(request.cardFromNumber(), totalDebitKop)) {
            repository.updateStatus(operationId, "FAILED");
            logger.logTransfer(request, operationId, "FAILED", commissionRub.doubleValue());
            throw new TransferException("Insufficient funds", ERROR_TRANSFER_FAILED);
        }

        boolean withdrawn = repository.withdraw(request.cardFromNumber(), totalDebitKop);
        if (!withdrawn) {
            repository.updateStatus(operationId, "FAILED");
            logger.logTransfer(request, operationId, "FAILED", commissionRub.doubleValue());
            throw new TransferException("Withdraw failed", ERROR_TRANSFER_FAILED);
        }

        boolean deposited = repository.deposit(request.cardToNumber(), amountKop);
        if (!deposited) {
            repository.deposit(request.cardFromNumber(), totalDebitKop);
            repository.updateStatus(operationId, "FAILED");
            logger.logTransfer(request, operationId, "FAILED", commissionRub.doubleValue());
            throw new TransferException("Receiver deposit failed", ERROR_TRANSFER_FAILED);
        }

        repository.updateStatus(operationId, "SUCCESS");
        logger.logTransfer(request, operationId, "SUCCESS", commissionRub.doubleValue());
        return operationId;
    }

    private void validate(TransferRequest request) {
        if (request.cardFromNumber().equals(request.cardToNumber())) {
            throw new InvalidInputException("Same card transfer not allowed-Одинаковые карты запрешены", ERROR_SAME_CARD);
        }

        String currency = request.amount().currency();
        if (currency == null || !("RUB".equalsIgnoreCase(currency) || "RUR".equalsIgnoreCase(currency))) {
            throw new InvalidInputException("Unsupported currency-Валюта недопустимая", ERROR_INVALID_CURRENCY);
        }

        BigDecimal amount = BigDecimal.valueOf(request.amount().value()); 
        if (!commission.isValidAmount(amount)) {
            throw new InvalidInputException("Invalid amount-Сумма перевода некорректна", ERROR_INVALID_AMOUNT);
        }
    }
}
