package ru.itmo.dto;

public class ErrorResponseDTO {
    public String status;
    public String message;

    public ErrorResponseDTO() {}

    public ErrorResponseDTO(String status, String message) {
        this.status = status;
        this.message = message;
    }
}
