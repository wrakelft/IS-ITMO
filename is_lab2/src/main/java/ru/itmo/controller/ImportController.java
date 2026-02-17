package ru.itmo.controller;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;
import ru.itmo.dto.FileImportDTO;
import ru.itmo.service.ImportService;

@Path("/api/import")
public class ImportController {

    @Inject private ImportService importService;

    @POST
    @Path("/organizations")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public Response importOrganizations(@MultipartForm FileImportDTO form) {
        int added = importService.importOrganizationJsonArray(form != null ? form.getFile() : null);

        String json = "{\"status\":\"SUCCESS\",\"addedCount\":" + added + "}";
        return Response.ok(json).build();
    }
}
