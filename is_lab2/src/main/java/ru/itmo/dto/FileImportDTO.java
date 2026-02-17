package ru.itmo.dto;

import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.core.MediaType;
import org.jboss.resteasy.annotations.providers.multipart.PartType;

import java.io.InputStream;

public class FileImportDTO {

    @FormParam("file")
    @PartType(MediaType.APPLICATION_OCTET_STREAM)
    private InputStream file;

    public InputStream getFile() { return file; }
    public void setFile(InputStream file) { this.file = file; }
}
