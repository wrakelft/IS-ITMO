package ru.itmo.controller;

import ru.itmo.dto.AddressDTO;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import ru.itmo.mapper.ReferenceMapper;
import ru.itmo.model.Address;
import ru.itmo.service.AddressService;

import java.util.List;

@Path("/api/addresses")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AddressController {

    @Inject
    private AddressService service;
    @Inject
    private ReferenceMapper referenceMapper;

    @GET
    public Response list() {
        List<AddressDTO> dto = service.findAll().stream()
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
