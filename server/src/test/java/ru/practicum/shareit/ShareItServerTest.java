package ru.practicum.shareit;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ShareItServerTest {

    @Test
    void main_ShouldStartApplicationWithoutErrors() {
        String[] args = new String[]{};

        assertDoesNotThrow(() -> ShareItServer.main(args));
    }

    @Test
    void main_ShouldStartApplicationWithArguments() {
        String[] args = new String[]{"--server.port=8080", "--spring.profiles.active=test"};

        assertDoesNotThrow(() -> ShareItServer.main(args));
    }

    @Test
    void contextLoads_ShouldStartSpringContext() {
    }
}