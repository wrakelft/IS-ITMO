package ru.itmo.mapper;

import jakarta.enterprise.context.ApplicationScoped;
import ru.itmo.dto.ImportOperationResponseDTO;
import ru.itmo.model.ImportOperation;


@ApplicationScoped
public class ImportOperationMapper {

    public ImportOperationResponseDTO toDto(ImportOperation op) {
        if (op == null) return null;
        ImportOperationResponseDTO d = new ImportOperationResponseDTO();
        d.id = op.getId();
        d.status = op.getStatus();
        d.username = op.getUsername();
        d.role = op.getRole();
        d.entityType = op.getEntityType();
        d.addedCount = op.getAddedCount();
        d.startedAt = op.getStartedAt();
        d.finishedAt = op.getFinishedAt();
        d.message = op.getErrorMessage();
        return d;
    }
}
