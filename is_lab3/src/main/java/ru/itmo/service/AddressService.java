package ru.itmo.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import ru.itmo.cache.LogL2Stats;
import ru.itmo.db.AddressRepository;
import ru.itmo.model.Address;
import ru.itmo.websocket.OrganizationWebSocket;

import java.util.List;

@LogL2Stats
@ApplicationScoped
public class AddressService {

    @Inject
    private AddressRepository repo;

    public List<Address> findAll() {
        return repo.findAll();
    }

    public void delete(Long id, Long replaceWithId) {
        repo.deleteWithReplace(id, replaceWithId);
        OrganizationWebSocket.broadcast("{\"type\":\"UPDATE\"}");
    }
}
