package com.company.apimanager.app.kong_object.helper;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.UUID;

public class BaseService {
    @JsonProperty("id")
    private UUID id;

    public BaseService(UUID id) {
        this.id = id;
    }

    public BaseService() {}

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }
}
