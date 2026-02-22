package ru.itmo.mapper;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import ru.itmo.dto.ErrorResponseDTO;

@Provider
public class GlobalExceptionMapper implements ExceptionMapper<Throwable> {


    @Override
    public Response toResponse(Throwable ex) {
        int status = 500;
        if (ex instanceof WebApplicationException wae) {
            status = wae.getResponse() != null ? wae.getResponse().getStatus() : 400;
        } else if (ex instanceof IllegalArgumentException) {
            status = 400;
        }

        String message = human(ex);
        return Response.status(status)
                .type(MediaType.APPLICATION_JSON)
                .entity(new ErrorResponseDTO("FAILED", message))
                .build();
    }

    private String human(Throwable ex) {
        String m = ex.getMessage();
        if (m == null || m.isBlank()) return ex.getClass().getSimpleName();
        m = m.replace("\r", " ").replace("\n", " ").trim();
        return m.length() > 500 ? m.substring(0, 500) : m;
    }
}
