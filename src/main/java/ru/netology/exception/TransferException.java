package ru.netology.exception;

public class TransferException extends RuntimeException {
    private final int errorId;

    public TransferException(String message, int errorId) {
        super(message);
        this.errorId = errorId;
    }

    public int getErrorId() {
        return errorId;
    }
}