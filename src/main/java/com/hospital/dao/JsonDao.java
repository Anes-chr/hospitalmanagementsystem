package com.hospital.dao;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.type.CollectionType;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public abstract class JsonDao<T> {
    protected final ObjectMapper objectMapper;
    protected final String filePath;
    protected final Class<T> entityType;

    public JsonDao(String filePath, Class<T> entityType) {
        this.filePath = filePath;
        this.entityType = entityType;
        this.objectMapper = new ObjectMapper();
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    protected void createFileIfNotExists() {
        File file = new File(filePath);
        if (!file.exists()) {
            try {
                File parentDir = file.getParentFile();
                if (parentDir != null && !parentDir.exists()) {
                    parentDir.mkdirs();
                }
                saveEntities(new ArrayList<>());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public List<T> getAllEntities() {
        createFileIfNotExists();
        try {
            File file = new File(filePath);
            CollectionType listType = objectMapper.getTypeFactory().constructCollectionType(ArrayList.class, entityType);
            return objectMapper.readValue(file, listType);
        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public void saveEntities(List<T> entities) throws IOException {
        File file = new File(filePath);
        objectMapper.writeValue(file, entities);
    }

    public abstract T getById(String id);
    public abstract void save(T entity) throws IOException;
    public abstract void update(T entity) throws IOException;
    public abstract void delete(String id) throws IOException;
}