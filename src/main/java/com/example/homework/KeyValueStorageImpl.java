package com.example.homework;

import com.example.homework.domain.StorageTable;
import com.example.homework.repos.StorageTableRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.Iterator;

@Component
@Scope("prototype")
public class KeyValueStorageImpl<K, V> implements KeyValueStorage<K, V>, Closeable {

    @Autowired
    StorageTableRepo storageTableRepo;

    public KeyValueStorageImpl() {

    }

    @Override
    public V read(K key) {
        if (storageTableRepo == null) {
            throw new RuntimeException("");
        }

        String keyBase64 = SerializeUtils.toBase64SerializedString(key);
        if (!storageTableRepo.existsByKey(keyBase64)) {
            return null;
        }

        String valueBase64 = storageTableRepo.findByKey(keyBase64).getValue();
        return SerializeUtils.toDeserializedObj(valueBase64);
    }

    @Override
    public boolean exists(K key) {
        if (storageTableRepo == null) {
            throw new RuntimeException("");
        }

        String keyBase64 = SerializeUtils.toBase64SerializedString(key);
        return storageTableRepo.existsByKey(keyBase64);
    }

    @Override
    public void write(K key, V value) {
        if (storageTableRepo == null) {
            throw new RuntimeException("");
        }

        String keyBase64 = SerializeUtils.toBase64SerializedString(key);
        String valueBase64 = SerializeUtils.toBase64SerializedString(value);

        if (storageTableRepo.existsByKey(keyBase64)) {
            StorageTable storageTable = storageTableRepo.findByKey(keyBase64);
            storageTable.setValue(valueBase64);
            storageTableRepo.save(storageTable);
        }
        else {
            StorageTable storageTable = new StorageTable(keyBase64, valueBase64);
            storageTableRepo.save(storageTable);
        }
    }

    @Override
    public void delete(K key) {
        if (storageTableRepo == null) {
            throw new RuntimeException("");
        }

        String keyBase64 = SerializeUtils.toBase64SerializedString(key);
        StorageTable storageTable = storageTableRepo.findByKey(keyBase64);
        if (storageTable != null) {
            storageTableRepo.delete(storageTable);
        }
    }

    @Override
    public Iterator<K> readKeys() {
        if (storageTableRepo == null) {
            throw new RuntimeException("");
        }

        Iterator<StorageTable> iterator = storageTableRepo.findAll().iterator();

        return new Iterator<K>() {
            @Override
            public boolean hasNext() {
                return iterator.hasNext();
            }

            @Override
            public K next() {
                if (!iterator.hasNext()) {
                    return null;
                }
                String keyBase64 = iterator.next().getKey();
                return SerializeUtils.toDeserializedObj(keyBase64);
            }
        };
    }

    @Override
    public int size() {
        if (storageTableRepo == null) {
            throw new RuntimeException("");
        }

        return (int) storageTableRepo.count();
    }

    @Override
    public void flush() {
        if (storageTableRepo != null) {
            storageTableRepo.flush();
        }
    }

    @Override
    public void close() {
        flush();

        storageTableRepo = null;
    }
}
