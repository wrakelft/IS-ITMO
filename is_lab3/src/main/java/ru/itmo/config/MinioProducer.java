package ru.itmo.config;

import io.minio.MinioClient;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import ru.itmo.storage.MinioObjectStorage;
import ru.itmo.storage.ObjectStorage;

import java.io.InputStream;
import java.util.Properties;

@ApplicationScoped
public class MinioProducer {

    private final Properties properties;

    public MinioProducer() {
        properties = new Properties();
        try (InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream("minio.properties")) {
            if (in == null) throw new IllegalStateException("minio.properties not found in resources");
            properties.load(in);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load minio.properties", e);
        }
    }

    @Produces
    @ApplicationScoped
    public ObjectStorage objectStorage() {
        String endpoint = properties.getProperty("minio.endpoint");
        String accessKey = properties.getProperty("minio.accessKey");
        String secretKey = properties.getProperty("minio.secretKey");
        String bucket = properties.getProperty("minio.bucket", "imports");
        String region = properties.getProperty("minio.region", "us-east-1");

        MinioClient client = MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .build();

        MinioObjectStorage storage = new MinioObjectStorage(client, bucket, region);
        storage.ensureBucket();
        return storage;
    }
}
