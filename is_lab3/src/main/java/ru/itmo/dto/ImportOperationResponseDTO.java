package ru.itmo.dto;

import java.time.LocalDateTime;

public class ImportOperationResponseDTO {
    public Long id;
    public String status;
    public String username;
    public String role;
    public String entityType;
    public Integer addedCount;
    public LocalDateTime startedAt;
    public LocalDateTime finishedAt;
    public String message;
    public String fileName;
    public String fileContentType;
    public Long fileSize;
    public Boolean hasFile;
}
