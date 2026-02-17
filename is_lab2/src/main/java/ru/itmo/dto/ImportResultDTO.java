package ru.itmo.dto;

public class ImportResultDTO {
    private String status;
    private Integer addedCount;
    private String message;
    private Integer failedIndex;

    public ImportResultDTO() {}

    public static ImportResultDTO success(int addedCount) {
        ImportResultDTO dto = new ImportResultDTO();
        dto.status = "SUCCESS";
        dto.addedCount = addedCount;
        return dto;
    }

    public static ImportResultDTO failed(String message, Integer failedIndex) {
        ImportResultDTO dto = new ImportResultDTO();
        dto.status = "FAILED";
        dto.message = message;
        dto.failedIndex = failedIndex;
        return dto;
    }
    public String getStatus() { return status; }
    public Integer getAddedCount() { return addedCount; }
    public String getMessage() { return message; }
    public Integer getFailedIndex() { return failedIndex; }

    public void setStatus(String status) { this.status = status; }
    public void setAddedCount(Integer addedCount) { this.addedCount = addedCount; }
    public void setMessage(String message) { this.message = message; }
    public void setFailedIndex(Integer failedIndex) { this.failedIndex = failedIndex; }
}
