package com.hospital.dao;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public abstract class JsonDao<T> {
    private final String filePath;
    private final Class<T> entityClass;
    protected final ObjectMapper objectMapper;

    public JsonDao(String filePath, Class<T> entityClass) {
        this.filePath = filePath;
        this.entityClass = entityClass;
        this.objectMapper = new ObjectMapper();

        // Configure ObjectMapper to ignore unknown properties
        this.objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    public void save(T entity) throws IOException {
        List<T> entities = getAllEntities();
        entities.add(entity);
        saveAllEntities(entities);
    }

    public void update(T entity) throws IOException {
        List<T> entities = getAllEntities();
        String id = getId(entity);

        for (int i = 0; i < entities.size(); i++) {
            if (getId(entities.get(i)).equals(id)) {
                entities.set(i, entity);
                break;
            }
        }

        saveAllEntities(entities);
    }

    public void delete(String id) throws IOException {
        List<T> entities = getAllEntities();
        entities.removeIf(entity -> getId(entity).equals(id));
        saveAllEntities(entities);
    }

    public T getById(String id) {
        List<T> entities = getAllEntities();

        if (id == null) {
            return entities.isEmpty() ? null : entities.get(0);
        }

        return entities.stream()
                .filter(entity -> getId(entity).equals(id))
                .findFirst()
                .orElse(null);
    }

    public List<T> getAllEntities() {
        File file = new File(filePath);
        if (file.exists()) {
            try {
                return objectMapper.readValue(file,
                        objectMapper.getTypeFactory().constructCollectionType(List.class, entityClass));
            } catch (IOException e) {
                e.printStackTrace();
                return new ArrayList<>();
            }
        }
        return new ArrayList<>();
    }

    protected void saveAllEntities(List<T> entities) throws IOException {
        File file = new File(filePath);
        file.getParentFile().mkdirs();
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(file, entities);
    }

    protected abstract String getId(T entity);
}