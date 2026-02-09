package ru.itmo.controller;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import ru.itmo.dto.CoordinatesDTO;
import ru.itmo.mapper.ReferenceMapper;
import ru.itmo.model.Coordinates;
import ru.itmo.service.CoordinatesService;

import java.util.List;

@Path("/api/coordinates")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CoordinatesController {

    @Inject
    private CoordinatesService service;
    @Inject
    private ReferenceMapper referenceMapper;

    @GET
    public Response list() {
        List<CoordinatesDTO> dto = service.findAll().stream()
                .map(referenceMapper::toDto)
                .toList();
        return Response.ok(dto).build();
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") Long id,
                           @QueryParam("replaceWith") Long replaceWithId) {
        service.delete(id, replaceWithId);
        return Response.noContent().build();
    }
}

