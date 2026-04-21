package ru.netology.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import ru.netology.controller.GlobalExceptionHandler;
import ru.netology.exception.InvalidInputException;
import ru.netology.exception.TransferException;
import ru.netology.model.ErrorResponse;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void testHandleInvalidInputException() {
        // given
        InvalidInputException ex = new InvalidInputException("Test error", 4001);

        // when
        ResponseEntity<ErrorResponse> response = handler.handleInvalidInput(ex);

        // then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Test error", response.getBody().getMessage());
        assertEquals(4001, response.getBody().getId());
    }

    @Test
    void testHandleTransferException() {
        // given
        TransferException ex = new TransferException("Transfer failed", 5000);

        // when
        ResponseEntity<ErrorResponse> response = handler.handleTransferException(ex);

        // then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Transfer failed", response.getBody().getMessage());
        assertEquals(5000, response.getBody().getId());
    }

    @Test
    void testHandleGenericException() {
        // given
        Exception ex = new RuntimeException("Unexpected error");

        // when
        ResponseEntity<ErrorResponse> response = handler.handleGenericException(ex);

        // then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Internal server error", response.getBody().getMessage());
        assertEquals(5000, response.getBody().getId());
    }

    // Для MethodArgumentNotValidException нужна более сложная настройка,
    // можно протестировать что обработчик вообще существует
    @Test
    void testHandlerMethodsExist() {
        assertDoesNotThrow(() -> {
            GlobalExceptionHandler handler = new GlobalExceptionHandler();
            // Проверяем что методы существуют
            handler.handleInvalidInput(new InvalidInputException("test", 4000));
            handler.handleTransferException(new TransferException("test", 5000));
            handler.handleGenericException(new Exception("test"));
        });
    }
}