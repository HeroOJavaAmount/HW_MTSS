package ru.netology.exception;

public class InvalidInputException extends RuntimeException {
    private final int errorId;

    public InvalidInputException(String message, int errorId) {
        super(message);
        this.errorId = errorId;
    }

    public int getErrorId() {
        return errorId;
    }
}
