package ru.itmo.service;

import org.hibernate.Session;
import org.hibernate.Transaction;
import ru.itmo.cache.LogL2Stats;
import ru.itmo.dto.OrganizationRequestDTO;
import ru.itmo.model.Organization;
import ru.itmo.db.OrganizationRepository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.util.List;

import ru.itmo.util.HibernateUtil;
import ru.itmo.util.TxIsolationUtil;
import ru.itmo.validation.OrganizationRequestValidator;
import ru.itmo.websocket.OrganizationWebSocket;

@LogL2Stats
@Named("organizationService")
@ApplicationScoped
public class OrganizationService {

    @Inject
    private OrganizationRepository organizationRepository;
    @Inject
    private HibernateUtil hibernateUtil;
    @Inject
    private OrganizationRequestValidator validator;

    private static final int TX_RETRIES = 3;

    public Organization createOrganization(OrganizationRequestDTO dto) {
        validator.validateForCreate(dto);

        RuntimeException last = null;
        for (int attempt = 1; attempt <= TX_RETRIES; attempt++) {
            try (Session session = hibernateUtil.getSessionFactory().openSession()) {
                Transaction tx = session.beginTransaction();
                TxIsolationUtil.setSerializable(session);
                try {

                    Organization organization = organizationRepository.createFromDtoInSession(dto, session);

                    organizationRepository.ensureUniqueName(session, organization.getName(), null);

                    var c = organization.getCoordinates();
                    var a = organization.getOfficialAddress();
                    if (c != null && a != null) {
                        organizationRepository.ensureUniqueAddressAndCoords(session, a.getStreet(), c.getX(), c.getY(), null);
                    }
                    session.save(organization);
                    session.flush();
                    tx.commit();
                    OrganizationWebSocket.broadcast("{\"type\":\"CREATE\",\"id\":" + organization.getId() + "}");
                    return organization;
                } catch (RuntimeException e) {
                    try {
                        tx.rollback();
                    } catch (Exception ignore) {
                    }
                    if (TxIsolationUtil.isSerializationFailure(e)) {
                        last = e;
                        continue;
                    }
                    throw e;
                }
            }
        }
        throw new IllegalStateException("Транзакция неудалась после нескольких попыток из за ошибки сериализации", last);
    }

        public Organization findById(Long id) {
        return organizationRepository.findById(id);
    }

    public List<Organization> findAll() {
        return organizationRepository.findAll();
    }

    public void updateOrganizationDto(Long id, OrganizationRequestDTO dto) {
        validator.validateForUpdate(dto);

        RuntimeException last = null;
        for (int attempt = 1; attempt <= TX_RETRIES; attempt++) {
            try (Session session = hibernateUtil.getSessionFactory().openSession()) {
                Transaction tx = session.beginTransaction();
                TxIsolationUtil.setSerializable(session);
                try {
                    Organization existing = session.get(Organization.class, id);
                    if (existing == null) throw new IllegalArgumentException("Организация не найдена");

                    Long reqV = dto.getVersion();
                    if (reqV == null) {
                        throw new jakarta.ws.rs.BadRequestException("Поле version обязательно для обновления");
                    }

                    Long dbV = existing.getVersion();
                    if (dbV == null || !reqV.equals(dbV)) {
                        try { tx.rollback(); } catch (Exception ignore) {}
                        throw new jakarta.ws.rs.WebApplicationException("Конфликт версий", 409);
                    }

                    organizationRepository.updateFromDtoInSession(id, dto, session);

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
                    return;

                } catch (RuntimeException e) {
                    try { tx.rollback(); } catch (Exception ignore) {}
                    if (TxIsolationUtil.isSerializationFailure(e)) {
                        last = e;
                        continue;
                    };
                    throw e;
                }
            }
        }
        throw new IllegalStateException("Транзакция неудалась после нескольких попыток из за ошибки сериализации", last);
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
