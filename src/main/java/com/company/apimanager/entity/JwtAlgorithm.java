package com.company.apimanager.entity;

import io.jmix.core.metamodel.datatype.impl.EnumClass;

import javax.annotation.Nullable;


public enum JwtAlgorithm implements EnumClass<String> {

//    HS256("HS256"),
//    HS384("HS384"),
//    HS512("HS512"),
//    RS256("RS256"),
//    RS384("RS384"),
//    RS512("RS512"),
//    ES256("ES256"),
//    ES384("ES384");

    HS256("HS256"),
    RS256("RS256");

    private String id;

    JwtAlgorithm(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    @Nullable
    public static JwtAlgorithm fromId(String id) {
        for (JwtAlgorithm at : JwtAlgorithm.values()) {
            if (at.getId().equals(id)) {
                return at;
            }
        }
        return null;
    }


}