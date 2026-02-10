package ru.itmo.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import ru.itmo.db.CoordinatesRepository;
import ru.itmo.dto.CoordinatesDTO;
import ru.itmo.model.Coordinates;
import ru.itmo.websocket.OrganizationWebSocket;

import java.util.List;

@ApplicationScoped
public class CoordinatesService {

    @Inject
    private CoordinatesRepository repo;

    public List<Coordinates> findAll() {
        return repo.findAll();
    }

    public void delete(Long id, Long replaceWithId) {
        repo.deleteWithReplace(id, replaceWithId);
        OrganizationWebSocket.broadcast("{\"type\":\"UPDATE\"}");
    }
}
