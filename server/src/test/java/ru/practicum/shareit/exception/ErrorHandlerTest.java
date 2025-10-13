package ru.practicum.shareit.exception;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.Map;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest
class ErrorHandlerTest {

    @Autowired
    private ErrorHandler errorHandler;

    @Test
    void handleNotFound_ShouldReturn404() {
        NoSuchElementException exception = new NoSuchElementException("Пользователь не найден");

        Map<String, String> response = errorHandler.handleNotFound(exception);

        assertEquals("Пользователь не найден", response.get("error"));
    }

    @Test
    void handleForbidden_ShouldReturn403() {
        ForbiddenException exception = new ForbiddenException("Доступ запрещен");

        Map<String, String> response = errorHandler.handleForbidden(exception);

        assertEquals("Доступ запрещен", response.get("error"));
    }

    @Test
    void handleConflict_ShouldReturn409() {
        IllegalArgumentException exception = new IllegalArgumentException("Некорректный аргумент");

        Map<String, String> response = errorHandler.handleConflict(exception);

        assertEquals("Некорректный аргумент", response.get("error"));
    }

    @Test
    void handleValidationException_ShouldReturn400() {
        ValidationException exception = new ValidationException("Ошибка валидации");

        Map<String, String> response = errorHandler.handleValidationException(exception);

        assertEquals("Ошибка валидации", response.get("error"));
    }

    @Test
    void handleConflictException_ShouldReturn409() {
        ConflictException exception = new ConflictException("Конфликт данных");

        Map<String, String> response = errorHandler.handleConflictException(exception);

        assertEquals("Конфликт данных", response.get("error"));
    }

    @Test
    void handleInternalError_ShouldReturn500() {
        Exception exception = new Exception("Внутренняя ошибка");

        Map<String, String> response = errorHandler.handleInternalError(exception);

        assertEquals("Internal server error", response.get("error"));
    }

    @Test
    void handleInternalError_WithRuntimeException_ShouldReturn500() {
        RuntimeException exception = new RuntimeException("Runtime ошибка");

        Map<String, String> response = errorHandler.handleInternalError(exception);

        assertEquals("Internal server error", response.get("error"));
    }

    @Test
    void handleValidationExceptions_ShouldReturn400() {
        // Создаем мок MethodArgumentNotValidException
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        org.springframework.validation.BindingResult bindingResult = mock(org.springframework.validation.BindingResult.class);
        org.springframework.validation.FieldError fieldError = mock(org.springframework.validation.FieldError.class);

        when(exception.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(java.util.List.of(fieldError));
        when(fieldError.getDefaultMessage()).thenReturn("Поле не может быть пустым");

        Map<String, String> response = errorHandler.handleValidationExceptions(exception);

        assertTrue(response.get("error").contains("Ошибка валидации"));
        assertTrue(response.get("error").contains("Поле не может быть пустым"));
    }

    @Test
    void handleValidationExceptions_WithEmptyFieldErrors_ShouldReturn400() {
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        org.springframework.validation.BindingResult bindingResult = mock(org.springframework.validation.BindingResult.class);

        when(exception.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(java.util.List.of());

        Map<String, String> response = errorHandler.handleValidationExceptions(exception);

        assertTrue(response.get("error").contains("Ошибка валидации"));
    }

    @Test
    void handleNotFound_WithNullMessage_ShouldReturn404() {
        NoSuchElementException exception = new NoSuchElementException();

        Map<String, String> response = errorHandler.handleNotFound(exception);

        assertTrue(response.containsKey("error"));
        assertNull(response.get("error"));
    }

    @Test
    void handleForbidden_WithNullMessage_ShouldReturn403() {
        ForbiddenException exception = new ForbiddenException(null);

        Map<String, String> response = errorHandler.handleForbidden(exception);

        assertNull(response.get("error"));
    }

    @Test
    void handleConflict_WithNullMessage_ShouldReturn409() {
        IllegalArgumentException exception = new IllegalArgumentException();

        Map<String, String> response = errorHandler.handleConflict(exception);

        assertNull(response.get("error"));
    }
}