package com.erikandreas.exchangedataservice.model;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ApiErrorResponse {
    private String message;
    private String errorCode;
    private LocalDateTime timestamp;
    private String details;  // Optional field for development environments
}
