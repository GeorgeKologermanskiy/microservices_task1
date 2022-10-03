package ru.mipt1c.homework;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class ValueCacheStorage<V> {

    public ValueCacheStorage(Path path, int cacheSize) {
        this.path = path;
        this.cacheSize = cacheSize;
        this.cache = new HashMap<>();
    }

    public V getValue(String filename) {
        if (cache.containsKey(filename)) {
            return cache.get(filename);
        }

        V value = getValueFromFile(filename);
        putCache(filename, value);

        return value;
    }

    public void putValue(String filename, V value) {
        putCache(filename, value);
    }

    public void flush() {
        for (String filename : cache.keySet()) {
            V value = cache.get(filename);

            putValueToFile(filename, value);
        }
    }

    private void putCache(String filename, V value) {
        cache.put(filename, value);

        if (cache.size() <= cacheSize) {
            return;
        }

        String removeKey = new ArrayList<>(cache.keySet())
                .get(new Random().nextInt(cache.size()));

        putValueToFile(removeKey, cache.get(removeKey));
        cache.remove(removeKey);
    }

    private void delete(String filename) {
        cache.remove(filename);

        deleteFile(filename);
    }

    private V getValueFromFile(String filename) {
        Path filepath = path.resolve(filename);

        if (Files.notExists(filepath)) {
            throw new MalformedDataException("file not found");
        }

        try {
            return SerializeUtils.toDeserializedObj(
                    Files.readAllBytes(filepath));
        }
        catch (IOException e) {
            throw new MalformedDataException(e);
        }
    }

    private void putValueToFile(String filename, V value) {
        byte[] bytes = SerializeUtils.serialize(value);
        Path filepath = path.resolve(filename);

        try {
            Files.write(filepath, bytes);
        }
        catch (IOException ignored) {

        }
    }

    private void deleteFile(String filename) {
        Path filepath = path.resolve(filename);
        try {
            Files.delete(filepath);
        }
        catch (IOException ignored) {

        }
    }

    private final Path path;
    private final int cacheSize;
    private final Map<String, V> cache;
}
