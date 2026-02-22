package ru.itmo.validation;

import jakarta.enterprise.context.ApplicationScoped;
import ru.itmo.dto.AddressDTO;
import ru.itmo.dto.CoordinatesDTO;
import ru.itmo.dto.OrganizationRequestDTO;

@ApplicationScoped
public class OrganizationRequestValidator {
    public void validateForCreate(OrganizationRequestDTO dto) {
        baseValidate(dto);

        xorRequired("coordinates", dto.getCoordinates(), "coordinatesId", dto.getCoordinatesId());

        xorRequired("officialAddress", dto.getOfficialAddress(), "officialAddressId", dto.getOfficialAddressId());

        xorOptional("postalAddress", dto.getPostalAddress(), "postalAddressId", dto.getPostalAddressId());

        if (dto.getCoordinates() != null) validateCoordinatesDto(dto.getCoordinates());
        if (dto.getOfficialAddress() != null) validateAddressDto(dto.getOfficialAddress(), "officialAddress");
        if (dto.getPostalAddress() != null) validateAddressDto(dto.getPostalAddress(), "postalAddress");
    }

    public void validateForUpdate(OrganizationRequestDTO dto) {
        if (dto.getAnnualTurnover() != null && dto.getAnnualTurnover() <= 0)
            throw new IllegalArgumentException("Оборот должен быть > 0");

        if (dto.getEmployeesCount() != null && dto.getEmployeesCount() < 0)
            throw new IllegalArgumentException("Количество сотрудников должно быть >= 0");

        if (dto.getRating() != null && dto.getRating() <= 0)
            throw new IllegalArgumentException("Рейтинг должен быть > 0");

        if (dto.getName() != null && dto.getName().trim().isEmpty())
            throw new IllegalArgumentException("Имя не может быть пустым");

        // XOR rules only if something from pair is present
        xorOptional("coordinates", dto.getCoordinates(), "coordinatesId", dto.getCoordinatesId());
        xorOptional("officialAddress", dto.getOfficialAddress(), "officialAddressId", dto.getOfficialAddressId());
        xorOptional("postalAddress", dto.getPostalAddress(), "postalAddressId", dto.getPostalAddressId());

        if (dto.getCoordinates() != null) validateCoordinatesDto(dto.getCoordinates());
        if (dto.getOfficialAddress() != null) validateAddressDto(dto.getOfficialAddress(), "officialAddress");
        if (dto.getPostalAddress() != null) validateAddressDto(dto.getPostalAddress(), "postalAddress");
    }

    private void baseValidate(OrganizationRequestDTO dto) {
        if (dto.getName() == null || dto.getName().trim().isEmpty())
            throw new IllegalArgumentException("Требуется название");

        if (dto.getAnnualTurnover() == null || dto.getAnnualTurnover() <= 0)
            throw new IllegalArgumentException("Оборот должен быть > 0");

        if (dto.getEmployeesCount() == null || dto.getEmployeesCount() < 0)
            throw new IllegalArgumentException("Количество сотрудников должно быть >= 0");

        if (dto.getRating() == null || dto.getRating() <= 0)
            throw new IllegalArgumentException("Рейтинг должен быть > 0");
    }

    private void validateCoordinatesDto(CoordinatesDTO c) {
        if (c.getX() == null || c.getY() == null)
            throw new IllegalArgumentException("Требуются координата.x и координата.y");
    }

    private void validateAddressDto(AddressDTO a, String field) {
        if (a.getStreet() == null || a.getStreet().trim().isEmpty())
            throw new IllegalArgumentException(field + ".улица не указана");
    }

    private void xorRequired(String aName, Object aVal, String bName, Long bVal) {
        boolean a = aVal != null;
        boolean b = bVal != null;
        if (a == b) {
            throw new IllegalArgumentException("Provide exactly one of " + aName + " or " + bName);
        }
    }

    private void xorOptional(String aName, Object aVal, String bName, Long bVal) {
        boolean a = aVal != null;
        boolean b = bVal != null;
        if (a && b) {
            throw new IllegalArgumentException("Provide only one of " + aName + " or " + bName);
        }
    }
}
