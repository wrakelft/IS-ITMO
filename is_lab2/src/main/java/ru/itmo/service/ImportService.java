package ru.itmo.service;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.BadRequestException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import ru.itmo.db.OrganizationRepository;
import ru.itmo.dto.OrganizationRequestDTO;
import ru.itmo.util.HibernateUtil;
import ru.itmo.validation.OrganizationRequestValidator;
import ru.itmo.websocket.OrganizationWebSocket;

import java.io.IOException;
import java.io.InputStream;

@ApplicationScoped
public class ImportService {
    @Inject private HibernateUtil hibernateUtil;
    @Inject private OrganizationRepository organizationRepository;
    @Inject private OrganizationRequestValidator validator;

    private final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());

    public int importOrganizationJsonArray(InputStream jsonStream) {
        if (jsonStream == null) {
            throw new BadRequestException("file is required");
        }

        try (Session session = hibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            int added = 0;
            int index = -1;

            try (JsonParser parser = mapper.getFactory().createParser(jsonStream)) {
                if (parser.nextToken() != JsonToken.START_ARRAY) {
                    throw new BadRequestException("Expected JSON array at root: [ {...}, {...} ]");
                }

                while (parser.nextToken() != JsonToken.END_ARRAY) {
                    index++;
                    OrganizationRequestDTO dto = mapper.readValue(parser, OrganizationRequestDTO.class);

                    validator.validateForCreate(dto);

                    organizationRepository.createFromDtoInSession(dto, session);
                    added++;

                    if (added % 100 == 0) {
                        session.flush();
                        session.clear();
                    }
                }

                tx.commit();
                OrganizationWebSocket.broadcast("{\"type\":\"IMPORT\",\"added\":" + added + "}");
                return added;
            } catch (RuntimeException e) {
                safeRollback(tx);
                throw new BadRequestException("Import failed at element #" + (index + 1) + ": " + humanMsg(e));
            } catch (IOException e) {
                safeRollback(tx);
                throw new BadRequestException("Invalid JSON at element #" + (index + 1) + ": " + humanMsg(e));
            }
        }
    }

    private void safeRollback(Transaction tx) {
        if (tx != null) {
            try {tx.rollback();} catch (Exception ignore) {}
        }
    }

    private String humanMsg(Exception e) {
        String m = e.getMessage();
        return (m != null && !m.isBlank()) ? m : e.getClass().getSimpleName();
    }
}
