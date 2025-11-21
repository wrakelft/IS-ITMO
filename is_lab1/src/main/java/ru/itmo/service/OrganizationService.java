package ru.itmo.service;

import ru.itmo.model.Organization;
import ru.itmo.db.OrganizationRepository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.util.List;
import ru.itmo.websocket.OrganizationWebSocket;

@Named("organizationService")
@ApplicationScoped
public class OrganizationService {

    @Inject
    private OrganizationRepository organizationRepository;

    public Organization createOrganization(Organization organization) {
        Organization created = organizationRepository.create(organization);
        OrganizationWebSocket.broadcast("{\"type\":\"CREATE\",\"id\":" + created.getId() + "}");
        return created;
    }

    public Organization findById(Long id) {
        return organizationRepository.findById(id);
    }

    public List<Organization> findAll() {
        return organizationRepository.findAll();
    }

    public void updateOrganization(Organization organization) {
        organizationRepository.update(organization);
        OrganizationWebSocket.broadcast("{\"type\":\"UPDATE\",\"id\":" + organization.getId() + "}");
    }

    public void deleteOrganization(Long id, boolean cascadeRelated) {
        organizationRepository.delete(id, cascadeRelated);
        OrganizationWebSocket.broadcast("{\"type\":\"DELETED\",\"id\":" + id + "}");
    }

    public List<Organization> findByCoordinatesId(Long coordinatesId) {
        return organizationRepository.findByCoordinatesId(coordinatesId);
    }

    public List<Organization> findByOfficialAddressId(Long addressId) {
        return organizationRepository.findByOfficialAddressId(addressId);
    }

    public Double getAverageRating() {
        return organizationRepository.getAverageRating();
    }

    public Organization getWithMinRating() {
        return organizationRepository.getWithMinRating();
    }

    public List<Organization> getWithRatingGreaterThan(int rating) {
        return organizationRepository.getWithRatingGreaterThan(rating);
    }

    public void fireAllEmployees(long orgId) {
        organizationRepository.fireAllEmployees(orgId);
        OrganizationWebSocket.broadcast("{\"type\":\"FIRE_ALL\",\"id\":" + orgId + "}");
    }

    public void hireEmployee(long orgId) {
        organizationRepository.hireEmployee(orgId);
        OrganizationWebSocket.broadcast("{\"type\":\"HIRE\",\"id\":" + orgId + "}");
    }
}
