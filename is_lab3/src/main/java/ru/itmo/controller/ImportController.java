package ru.itmo.controller;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;
import ru.itmo.dto.FileImportDTO;
import ru.itmo.dto.ImportOperationResponseDTO;
import ru.itmo.service.ImportService;

@Path("/api/import")
public class ImportController {

    @Inject private ImportService importService;


    @POST
    @Path("/organizations")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public Response importOrganizations(@MultipartForm FileImportDTO form,
                                        @QueryParam("username") @DefaultValue("guest") String username,
                                        @QueryParam("role") @DefaultValue("USER") String role) {
        String fileName = form != null ? form.getFileName() : null;
        String contentType = form != null ? form.getContentType() : null;

        int added = importService.importOrganizationJsonArray(form != null ? form.getFile() : null,
                username,
                role,
                fileName,
                contentType);

        ImportOperationResponseDTO resp = new ImportOperationResponseDTO();
        resp.status = "SUCCESS";
        resp.addedCount = added;
        return Response.ok(resp).build();
    }

    @GET
    @Path("/history")
    @Produces(MediaType.APPLICATION_JSON)
    public Response history(@QueryParam("username") @DefaultValue("guest") String username,
                            @QueryParam("role") @DefaultValue("USER") String role,
                            @QueryParam("limit") @DefaultValue("100") int limit) {

        return Response.ok(importService.getHistory(username, role, limit)).build();
    }

    @GET
    @Path("/{opId}/file")
    public Response downloadFile(@PathParam("opId") long opId,
                                 @QueryParam("username") @DefaultValue("guest") String username,
                                 @QueryParam("role") @DefaultValue("USER") String role) {

        return importService.downloadImportFile(opId, username, role);
    }

    @GET
    @Path("/ping")
    @Produces(MediaType.TEXT_PLAIN)
    public String ping() {
        return "ok";
    }

}
