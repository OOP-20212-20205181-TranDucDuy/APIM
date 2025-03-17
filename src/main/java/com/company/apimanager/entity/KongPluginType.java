package com.company.apimanager.entity;

import io.jmix.core.metamodel.datatype.impl.EnumClass;

import javax.annotation.Nullable;


public enum KongPluginType implements EnumClass<String> {

    REQUEST_TRANSFORMER("request-transformer"),
    RESPONSE_TRANSFORMER("response-transformer"),
    CROSS_ORIGIN_RESOURCE_SHARING("cors");

    private String id;

    KongPluginType(String value) {
        this.id = value;
    }

    public String getId() {
        return id;
    }

    @Nullable
    public static KongPluginType fromId(Integer id) {
        for (KongPluginType at : KongPluginType.values()) {
            if (at.getId().equals(id)) {
                return at;
            }
        }
        return null;
    }
}