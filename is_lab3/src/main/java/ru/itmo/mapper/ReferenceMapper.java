package ru.itmo.mapper;

import jakarta.enterprise.context.ApplicationScoped;
import ru.itmo.dto.AddressDTO;
import ru.itmo.dto.CoordinatesDTO;
import ru.itmo.model.Address;
import ru.itmo.model.Coordinates;

@ApplicationScoped
public class ReferenceMapper {

    public AddressDTO toDto(Address a) {
        if (a == null) return null;
        return new AddressDTO(a.getId(), a.getStreet());
    }

    public CoordinatesDTO toDto(Coordinates c) {
        if (c == null) return null;
        return new CoordinatesDTO(c.getId(), c.getX(), c.getY());
    }
}
