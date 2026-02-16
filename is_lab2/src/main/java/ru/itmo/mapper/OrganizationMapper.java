package ru.itmo.mapper;

import jakarta.enterprise.context.ApplicationScoped;
import ru.itmo.dto.*;
import ru.itmo.model.*;

@ApplicationScoped
public class OrganizationMapper {

    public OrganizationResponseDTO toResponse(Organization o) {
        if (o == null) return null;

        OrganizationResponseDTO dto = new OrganizationResponseDTO();
        dto.setId(o.getId());
        dto.setName(o.getName());
        dto.setCreationDate(o.getCreationDate());
        dto.setAnnualTurnover(o.getAnnualTurnover());
        dto.setEmployeesCount(o.getEmployeesCount());
        dto.setRating(o.getRating());
        dto.setType(o.getType());

        dto.setCoordinates(toDto(o.getCoordinates()));
        dto.setOfficialAddress(toDto(o.getOfficialAddress()));
        dto.setPostalAddress(toDto(o.getPostalAddress()));

        return dto;
    }

    private CoordinatesDTO toDto(Coordinates c) {
        if (c == null) return null;
        CoordinatesDTO dto = new CoordinatesDTO();
        dto.setId(c.getId());
        dto.setX(c.getX());
        dto.setY(c.getY());
        return dto;
    }

    private AddressDTO toDto(Address a) {
        if (a == null) return null;
        AddressDTO dto = new AddressDTO();
        dto.setId(a.getId());
        dto.setStreet(a.getStreet());
        return dto;
    }

    public Organization toNewForCreate(OrganizationRequestDTO dto) {
        if (dto == null) return null;

        Organization o = new Organization();
        o.setName(dto.getName());
        o.setType(dto.getType());
        o.setAnnualTurnover(dto.getAnnualTurnover());
        o.setEmployeesCount(dto.getEmployeesCount());
        o.setRating(dto.getRating());
        return o;
    }

    public void applyBasicToExisting(OrganizationRequestDTO dto, Organization existing) {
        if (dto == null || existing == null) return;

        if (dto.getName() != null) existing.setName(dto.getName());
        if (dto.getType() != null) existing.setType(dto.getType());

        if (dto.getAnnualTurnover() != null) existing.setAnnualTurnover(dto.getAnnualTurnover());
        if (dto.getEmployeesCount() != null) existing.setEmployeesCount(dto.getEmployeesCount());
        if (dto.getRating() != null) existing.setRating(dto.getRating());
    }
}