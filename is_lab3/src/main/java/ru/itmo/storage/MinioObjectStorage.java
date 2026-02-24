package ru.itmo.storage;

import io.minio.*;

import java.io.InputStream;

public class MinioObjectStorage implements ObjectStorage {

    private final MinioClient client;
    private final String bucket;
    private final String region;

    public MinioObjectStorage(MinioClient client, String bucket, String region) {
        this.client = client;
        this.bucket = bucket;
        this.region = region;
    }

    @Override
    public void ensureBucket() {
        try {
            boolean exists = client.bucketExists(BucketExistsArgs.builder().bucket(bucket).build());
            if (!exists) {
                client.makeBucket(MakeBucketArgs.builder().bucket(bucket).region(region).build());
            }
        } catch (Exception e) {
            throw new RuntimeException("MinIO: cannot ensure bucket=" + bucket, e);
        }
    }

    @Override
    public void put(String key, InputStream stream, long size, String contentType) {
        try {
            client.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucket)
                            .object(key)
                            .stream(stream, size, -1)
                            .contentType(contentType != null ? contentType : "application/octet-stream")
                            .build());
        } catch (Exception e) {
            throw new RuntimeException("MinIO: put failed key=" + key, e);
        }
    }

    @Override
    public InputStream get(String key) {
        try {
            return client.getObject(GetObjectArgs.builder().bucket(bucket).object(key).build());
        } catch (Exception e) {
            throw new RuntimeException("MinIO: get failed key=" + key, e);
        }
    }

    @Override
    public void deleteQuietly(String key) {
        try {
            client.removeObject(RemoveObjectArgs.builder().bucket(bucket).object(key).build());
        } catch (Exception ignored) {
        }
    }
}
