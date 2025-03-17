package com.company.apimanager.dto;

public class ProductSourceDTO {
    private String id;
    private String name;
    private String description;
    private String created_at;
    private String updated_at;
    private int version_count;
    private int document_count;
    private String latest_version;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCreated_at() {
        return created_at;
    }

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }

    public String getUpdated_at() {
        return updated_at;
    }

    public void setUpdated_at(String updated_at) {
        this.updated_at = updated_at;
    }

    public int getVersion_count() {
        return version_count;
    }

    public void setVersion_count(int version_count) {
        this.version_count = version_count;
    }

    public int getDocument_count() {
        return document_count;
    }

    public void setDocument_count(int document_count) {
        this.document_count = document_count;
    }

    public String getLatest_version() {
        return latest_version;
    }

    public void setLatest_version(String latest_version) {
        this.latest_version = latest_version;
    }
}