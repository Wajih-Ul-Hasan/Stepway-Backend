package com.example.Stepway.dto;

public class AuthMessageResponse {
    private final String message;

    public AuthMessageResponse(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
