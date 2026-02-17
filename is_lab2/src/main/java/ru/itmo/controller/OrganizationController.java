package ru.itmo.controller;

import ru.itmo.model.Organization;
import ru.itmo.service.OrganizationService;
import ru.itmo.dto.OrganizationRequestDTO;
import ru.itmo.dto.OrganizationResponseDTO;
import ru.itmo.mapper.OrganizationMapper;
import java.util.stream.Collectors;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;


@Path("/api/organizations")
public class OrganizationController {

    @Inject
    private OrganizationService organizationService;
    @Inject
    private OrganizationMapper organizationMapper;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response create(OrganizationRequestDTO dto) {
        Organization created = organizationService.createOrganization(dto);
        return Response.status(Response.Status.CREATED).entity(organizationMapper.toResponse(created)).build();
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response findById(@PathParam("id") Long id) {
        Organization organization = organizationService.findById(id);
        if (organization == null) {
            throw new NotFoundException("Organization " + id + " not found");
        }
        return Response.ok(organizationMapper.toResponse(organization)).build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response findAll() {
        List<Organization> organizations = organizationService.findAll();
        List<OrganizationResponseDTO> response = organizations.stream()
                .map(organizationMapper::toResponse)
                .collect(Collectors.toList());
        return Response.ok(response).build();
    }

    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response update(@PathParam("id") Long id, OrganizationRequestDTO dto) {
        organizationService.updateOrganizationDto(id, dto);
        return Response.noContent().build();
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") Long id, @QueryParam("cascade") @DefaultValue("false") boolean cascadeRelated) {
        organizationService.deleteOrganization(id, cascadeRelated);
        return Response.noContent().build();
    }

    @GET
    @Path("/average-rating")
    @Produces(MediaType.APPLICATION_JSON)
    public Response averageRating() {
        Double avg = organizationService.getAverageRating();
        return Response.ok(avg).build();
    }

    @GET
    @Path("/min-rating")
    @Produces(MediaType.APPLICATION_JSON)
    public Response minRatingOrg() {
        Organization org = organizationService.getWithMinRating();
        if (org == null) throw new NotFoundException("No organizations");
        OrganizationResponseDTO response = organizationMapper.toResponse(org);
        return Response.ok(response).build();
    }

    @GET
    @Path("/rating-greater-than/{rating}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response ratingGreaterThan(@PathParam("rating") int rating) {
        List<Organization> result = organizationService.getWithRatingGreaterThan(rating);
        List<OrganizationResponseDTO> response = result.stream()
                .map(organizationMapper::toResponse)
                .collect(Collectors.toList());
        return Response.ok(response).build();
    }

    @PUT
    @Path("/{id}/fire-all")
    public Response fireAll(@PathParam("id") Long id) {
        organizationService.fireAllEmployees(id);
        return Response.ok().build();
    }

    @PUT
    @Path("/{id}/hire")
    public Response hire(@PathParam("id") Long id) {
        organizationService.hireEmployee(id);
        return Response.ok().build();
    }
}
