package org.example.controller;

import org.example.model.Organization;
import org.example.service.OrganizationService;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;

public class OrganizationController {

    @Inject
    private OrganizationService organizationService;

    @POST
    public Response create(Organization organization) {
        Organization created = organizationService.createOrganization(organization);
        return Response.status(Response.Status.CREATED).entity(created).build();
    }

    @GET
    @Path("/{id}")
    public Response findById(@PathParam("id") Long id) {
        Organization organization = organizationService.findById(id);
        if (organization == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(organization).build();
    }

    @GET
    public Response findAll() {
        List<Organization> organizations = organizationService.findAll();
        return Response.ok(organizations).build();
    }

    @PUT
    @Path("/{id}")
    public Response update(@PathParam("id") Long id, Organization updated) {
        updated.setId(id);
        organizationService.updateOrganization(updated);
        return Response.ok(updated).build();
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") Long id, @QueryParam("cascade") @DefaultValue("false") boolean cascadeRelated) {
        organizationService.deleteOrganization(id, cascadeRelated);
        return Response.noContent().build();
    }

    @GET
    @Path("/average-rating")
    public Response averageRating() {
        Double avg = organizationService.getAverageRating();
        return Response.ok(avg).build();
    }

    @GET
    @Path("/rating-greater-than/{rating}")
    public Response ratingGreaterThan(@PathParam("rating") int rating) {
        List<Organization> result = organizationService.getWithRatingGreaterThan(rating);
        return Response.ok(result).build();
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
