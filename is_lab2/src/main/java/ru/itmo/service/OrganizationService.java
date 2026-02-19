package ru.itmo.service;

import org.hibernate.Session;
import org.hibernate.Transaction;
import ru.itmo.dto.OrganizationRequestDTO;
import ru.itmo.model.Organization;
import ru.itmo.db.OrganizationRepository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.util.List;

import ru.itmo.util.HibernateUtil;
import ru.itmo.validation.OrganizationRequestValidator;
import ru.itmo.websocket.OrganizationWebSocket;

@Named("organizationService")
@ApplicationScoped
public class OrganizationService {

    @Inject
    private OrganizationRepository organizationRepository;
    @Inject
    private HibernateUtil hibernateUtil;
    @Inject
    private OrganizationRequestValidator validator;

    public Organization createOrganization(OrganizationRequestDTO dto) {
        validator.validateForCreate(dto);
        try (Session session = hibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            try {
                Organization organization = organizationRepository.createFromDtoInSession(dto, session);

                organizationRepository.ensureUniqueName(session, organization.getName(), null);

                var c = organization.getCoordinates();
                var a = organization.getOfficialAddress();
                if (c != null && a != null) {
                    organizationRepository.ensureUniqueAddressAndCoords(session, a.getStreet(), c.getX(), c.getY(), null);
                }
                session.save(organization);
                tx.commit();
                OrganizationWebSocket.broadcast("{\"type\":\"CREATE\",\"id\":" + organization.getId() + "}");
                return organization;
            } catch (RuntimeException e) {
                tx.rollback();
                throw e;
            }
        }
    }

    public Organization findById(Long id) {
        return organizationRepository.findById(id);
    }

    public List<Organization> findAll() {
        return organizationRepository.findAll();
    }

    public void updateOrganizationDto(Long id, OrganizationRequestDTO dto) {
        validator.validateForUpdate(dto);

        try (Session session = hibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            try {
                Organization existing = organizationRepository.updateFromDtoInSession(id, dto, session);

                if (existing == null) throw new IllegalArgumentException("Организация не найдена");

                organizationRepository.ensureUniqueName(session, existing.getName(), id);

                var c = existing.getCoordinates();
                var a = existing.getOfficialAddress();
                if (c != null && a != null) {
                    organizationRepository.ensureUniqueAddressAndCoords(
                            session, a.getStreet(), c.getX(), c.getY(), id
                    );
                }

                session.flush();
                tx.commit();

                OrganizationWebSocket.broadcast("{\"type\":\"UPDATE\",\"id\":" + id + "}");
            } catch (RuntimeException e) {
                tx.rollback();
                throw e;
            }
        }
    }


    public void deleteOrganization(Long id, boolean cascadeRelated) {
        organizationRepository.delete(id);
        OrganizationWebSocket.broadcast("{\"type\":\"DELETED\",\"id\":" + id + "}");
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
        int updated = organizationRepository.fireAllEmployees(orgId);
        if (updated == 0) {
            throw new jakarta.ws.rs.NotFoundException("Organization " + orgId + " not found");
        }
        OrganizationWebSocket.broadcast("{\"type\":\"FIRE_ALL\",\"id\":" + orgId + "}");
    }

    public void hireEmployee(long orgId) {
        int updated = organizationRepository.hireEmployee(orgId);
        if (updated == 0) {
            throw new jakarta.ws.rs.NotFoundException("Organization " + orgId + " not found");
        }
        OrganizationWebSocket.broadcast("{\"type\":\"HIRE\",\"id\":" + orgId + "}");
    }
}
