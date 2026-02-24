package ru.itmo.storage;

import java.io.InputStream;

public interface ObjectStorage {
    void ensureBucket();
    void put(String key, InputStream stream, long size, String contentType);
    InputStream get(String key);
    void deleteQuietly(String key);
}