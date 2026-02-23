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
import ru.itmo.cache.LogL2Stats;
import ru.itmo.db.ImportOperationRepository;
import ru.itmo.db.OrganizationRepository;
import ru.itmo.dto.ImportOperationResponseDTO;
import ru.itmo.dto.OrganizationRequestDTO;
import ru.itmo.mapper.ImportOperationMapper;
import ru.itmo.model.ImportOperation;
import ru.itmo.model.Organization;
import ru.itmo.util.HibernateUtil;
import ru.itmo.util.TxIsolationUtil;
import ru.itmo.validation.OrganizationRequestValidator;
import ru.itmo.websocket.OrganizationWebSocket;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.io.ByteArrayInputStream;

@LogL2Stats
@ApplicationScoped
public class ImportService {
    @Inject private HibernateUtil hibernateUtil;
    @Inject private OrganizationRepository organizationRepository;
    @Inject private OrganizationRequestValidator validator;
    @Inject private ImportOperationRepository importOpRepo;
    @Inject private ImportOperationMapper importOpMapper;

    private static final int TX_RETRIES = 10;

    private final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());

    public int importOrganizationJsonArray(InputStream jsonStream, String username, String role) {
        if (jsonStream == null) {
            throw new BadRequestException("Требуется файл");
        }

        long opId = importOpRepo.create(username, role, "organizations");

        final byte[] payload;
        try {
            payload = jsonStream.readAllBytes();
        } catch (IOException e) {
            importOpRepo.markAsFailed(opId, shortMsg(e));
            throw new BadRequestException("Не удалось прочитать файл");
        }

        RuntimeException lastSerialization = null;
        for (int attempt = 1; attempt < TX_RETRIES; attempt++) {
            try (Session session = hibernateUtil.getSessionFactory().openSession()) {
                Transaction tx = session.beginTransaction();
                TxIsolationUtil.setSerializable(session);

                int added = 0;
                int index = -1;

                try (JsonParser parser = mapper.getFactory().createParser(new ByteArrayInputStream(payload))) {
                    if (parser.nextToken() != JsonToken.START_ARRAY) {
                        throw new BadRequestException("Ожидаемый JSON массив: [ {...}, {...} ]");
                    }

                    while (parser.nextToken() != JsonToken.END_ARRAY) {
                        index++;
                        OrganizationRequestDTO dto = mapper.readValue(parser, OrganizationRequestDTO.class);

                        validator.validateForCreate(dto);

                        Organization org = organizationRepository.createFromDtoInSession(dto, session);

                        organizationRepository.ensureUniqueName(session, org.getName(), null);

                        var c = org.getCoordinates();
                        var a = org.getOfficialAddress();
                        if (c != null && a != null) {
                            organizationRepository.ensureUniqueAddressAndCoords(
                                    session, a.getStreet(), c.getX(), c.getY(), null
                            );
                        }

                        session.save(org);
                        added++;

                        if (added % 100 == 0) {
                            session.flush();
                            session.clear();
                        }
                    }

                    session.flush();
                    tx.commit();

                    importOpRepo.markAsSuccess(opId, added);

                    OrganizationWebSocket.broadcast("{\"type\":\"IMPORT\",\"added\":" + added + "}");
                    return added;
                } catch (RuntimeException e) {
                    safeRollback(tx);
                    if (TxIsolationUtil.isSerializationFailure(e)) {
                        lastSerialization = e;
                        long base = 100L * (1L << Math.min(attempt, 6));
                        long jitter = java.util.concurrent.ThreadLocalRandom.current().nextLong(0, 200);
                        try { Thread.sleep(base + jitter); } catch (InterruptedException ignore) {}
                        continue;
                    }
                    importOpRepo.markAsFailed(opId, shortMsg(e));
                    OrganizationWebSocket.broadcast(
                            "{\"type\":\"IMPORT\",\"status\":\"FAILED\",\"opId\":" + opId +
                                    ",\"message\":\"" + shortMsg(e).replace("\\", "\\\\").replace("\"", "\\\"") + "\"}"
                    );
                    throw new BadRequestException("Ошибка импорта на элементе #" + (index + 1));
                } catch (IOException e) {
                    safeRollback(tx);
                    importOpRepo.markAsFailed(opId, shortMsg(e));
                    OrganizationWebSocket.broadcast(
                            "{\"type\":\"IMPORT\",\"status\":\"FAILED\",\"opId\":" + opId +
                                    ",\"message\":\"" + shortMsg(e).replace("\\", "\\\\").replace("\"", "\\\"") + "\"}"
                    );
                    throw new BadRequestException("Ошибка формата JSON на элементе #" + (index + 1));
                }
            }
        }
        importOpRepo.markAsFailed(opId, "Serialization failure after retries: " + shortMsg(lastSerialization));
        OrganizationWebSocket.broadcast(
                "{\"type\":\"IMPORT\",\"status\":\"FAILED\",\"opId\":" + opId +
                        ",\"message\":\"" + ("Serialization failure after retries: " + shortMsg(lastSerialization))
                        .replace("\\", "\\\\").replace("\"", "\\\"") + "\"}"
        );
        throw new BadRequestException("Импорт не выполнен из-за конкуренции транзакций (40001). Повторите попытку.");
    }

    public List<ImportOperationResponseDTO> getHistory(String username, String role, int limit) {
        int lim = (limit <= 0 || limit > 500) ? 100 : limit;

        List<ImportOperation> ops = "ADMIN".equals(role)
                ? importOpRepo.findAll(lim)
                : importOpRepo.findByUser(username, lim);

        return ops.stream().map(importOpMapper::toDto).toList();
    }

    private void safeRollback(Transaction tx) {
        if (tx != null) {
            try {tx.rollback();} catch (Exception ignore) {}
        }
    }

    private String shortMsg(Throwable e) {
        String m = e.getMessage();
        if (m == null || m.isBlank()) return e.getClass().getSimpleName();
        m = m.replace("\r", " ").replace("\n", " ").trim();
        return m.length() > 200 ? m.substring(0, 200) : m;
    }
}
