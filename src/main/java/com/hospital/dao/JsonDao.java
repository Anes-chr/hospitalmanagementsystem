package com.hospital.dao;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public abstract class JsonDao<T> {
    protected final String filePath;
    private final Class<T> entityClass;
    protected final ObjectMapper objectMapper;

    public JsonDao(String filePath, Class<T> entityClass) {
        this.filePath = filePath;
        this.entityClass = entityClass;
        this.objectMapper = new ObjectMapper();

        // Configure ObjectMapper to handle polymorphic types
        this.objectMapper.activateDefaultTyping(
                LaissezFaireSubTypeValidator.instance,
                ObjectMapper.DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.PROPERTY
        );

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

        boolean found = false;
        for (int i = 0; i < entities.size(); i++) {
            if (getId(entities.get(i)).equals(id)) {
                entities.set(i, entity);
                found = true;
                break;
            }
        }

        if (found) {
            saveAllEntities(entities);
        } else {
            // If not found, add as new entity
            save(entity);
        }
    }

    public void delete(String id) throws IOException {
        List<T> entities = getAllEntities();
        boolean removed = entities.removeIf(entity -> getId(entity).equals(id));

        if (removed) {
            saveAllEntities(entities);
        }
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
        File parentDir = file.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }

        // Create a backup of the current file if it exists
        if (file.exists()) {
            File backup = new File(filePath + ".bak");
            if (backup.exists()) {
                backup.delete();
            }
            file.renameTo(backup);
        }

        try {
            // Write to a temporary file first
            File tempFile = new File(filePath + ".tmp");
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(tempFile, entities);

            // If successful, replace the original file
            if (file.exists()) {
                file.delete();
            }
            tempFile.renameTo(file);
        } catch (IOException e) {
            // If something goes wrong, try to restore from backup
            File backup = new File(filePath + ".bak");
            if (backup.exists()) {
                if (file.exists()) {
                    file.delete();
                }
                backup.renameTo(file);
            }
            throw e;
        }
    }

    protected abstract String getId(T entity);
}