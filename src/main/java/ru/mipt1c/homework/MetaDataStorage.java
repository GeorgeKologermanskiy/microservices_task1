package ru.mipt1c.homework;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.stream.Collectors;

public class MetaDataStorage<K> {

    public MetaDataStorage(Path path) throws MalformedDataException {
        this.path = path;
        if (Files.exists(this.path)) {
            readFromFile();
        }
        else
        {
            this.meta = new HashMap<>();
            try {
                Files.createFile(this.path);
            }
            catch (IOException e) {
                throw new MalformedDataException(e);
            }
        }
    }

    public String find(K key) {
        return meta.get(key);
    }

    public boolean exists(K key) {
        return meta.containsKey(key);
    }

    public void write(K key, String filename) {
        meta.put(key, filename);
    }

    public void delete(K key) {
        meta.remove(key);
    }

    public int size() {
        return meta.size();
    }

    public Iterator<K> readKeys() {
        return meta.keySet().iterator();
    }

    public void flush() {
        JSONArray metaObj = new JSONArray();
        for (Map.Entry<K, String> it : this.meta.entrySet()) {
            JSONObject obj = new JSONObject();
            String base64 = SerializeUtils.toBase64SerializedString(it.getKey());
            obj.put("key", base64);
            obj.put("filename", it.getValue());

            metaObj.put(obj);
        }

        try {
            BufferedWriter writer = Files.newBufferedWriter(this.path);
            writer.write(metaObj.toString());
            writer.close();
        }
        catch (IOException e) {
            throw new MalformedDataException(e);
        }

    }

    private void readFromFile() {
        try (BufferedReader reader = Files.newBufferedReader(this.path)) {
            JSONArray metaObj = new JSONArray(reader.lines().collect(Collectors.joining()));

            this.meta = new HashMap<>();
            for (int i = 0; i < metaObj.length(); ++i) {
                JSONObject obj = metaObj.getJSONObject(i);
                K key = SerializeUtils.toDeserializedObj(obj.getString("key"));
                String filename = obj.getString("filename");

                this.meta.put(key, filename);
            }
        }
        catch (IOException e) {
            throw new MalformedDataException(e);
        }

    }

    private final Path path;

    private Map<K, String> meta;
}
