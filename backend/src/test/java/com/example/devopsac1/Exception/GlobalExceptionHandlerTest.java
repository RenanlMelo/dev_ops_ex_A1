package com.example.devopsac1.Exception;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void mapsIllegalStateExceptionToConflict() {
        ResponseEntity<ApiError> response = handler.handleConflict(
                new IllegalStateException("Average unavailable: the course has not been completed yet."));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody().status()).isEqualTo(409);
        assertThat(response.getBody().message()).isEqualTo(
                "Average unavailable: the course has not been completed yet.");
    }
}
