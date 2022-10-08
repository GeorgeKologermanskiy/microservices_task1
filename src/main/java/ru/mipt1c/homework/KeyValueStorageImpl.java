package ru.mipt1c.homework;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.UUID;

public class KeyValueStorageImpl<K, V> implements KeyValueStorage<K, V>, Closeable {

    KeyValueStorageImpl(String path) {
        this.path = Paths.get(path);
        if (!Files.exists(this.path)) {
            throw new MalformedDataException("Directory does not exists");
        }

        this.meta = new MetaDataStorage<>(this.path.resolve(".metadata"));
        this.cache = new ValueCacheStorage<>(this.path, cacheSize);
    }

    @Override
    public V read(K key) {
        if (meta == null) {
            throw new RuntimeException("");
        }

        String filename = meta.find(key);
        if (filename == null) {
            return null;
        }

        return cache.getValue(filename);
    }

    @Override
    public boolean exists(K key) {
        if (meta == null) {
            throw new RuntimeException("");
        }

        return meta.exists(key);
    }

    @Override
    public void write(K key, V value) {
        if (meta == null) {
            throw new RuntimeException("");
        }

        if (!meta.exists(key)) {
            meta.write(key, generateFilename());
        }
        String filename = meta.find(key);

        cache.putValue(filename, value);
    }

    @Override
    public void delete(K key) {
        if (meta == null) {
            throw new RuntimeException("");
        }

        meta.delete(key);
    }

    @Override
    public Iterator<K> readKeys() {
        if (meta == null) {
            throw new RuntimeException("");
        }

        return meta.readKeys();
    }

    @Override
    public int size() {
        if (meta == null) {
            throw new RuntimeException("");
        }

        return meta.size();
    }

    @Override
    public void flush() {
        if (meta == null) {
            throw new RuntimeException("");
        }
        meta.flush();

        cache.flush();
    }

    @Override
    public void close() {
        if (meta == null) {
            return;
        }

        flush();

        meta = null;
        cache = null;
    }

    private String generateFilename() {
        String filename;
        do {
            filename = UUID.randomUUID().toString();
        } while(Files.exists(path.resolve(filename)));

        return filename;
    }

    private final Path path;

    private MetaDataStorage<K> meta;

    private static final int cacheSize = 32;
    private ValueCacheStorage<V> cache;
}
