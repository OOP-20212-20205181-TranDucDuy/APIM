package com.company.apimanager.app.kong_object.helper;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.util.ArrayList;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(use = JsonTypeInfo.Id.NONE, include = JsonTypeInfo.As.PROPERTY)
@JsonSubTypes({
        @JsonSubTypes.Type(value = RequestAdd.class, name = "add"),
        @JsonSubTypes.Type(value = RequestAppend.class, name = "append"),
        @JsonSubTypes.Type(value = RequestRemove.class, name = "remove"),
        @JsonSubTypes.Type(value = RequestRename.class, name = "rename"),
        @JsonSubTypes.Type(value = RequestReplace.class, name = "replace"),

        @JsonSubTypes.Type(value = ResponseAdd.class, name = "add"),
        @JsonSubTypes.Type(value = ResponseAppend.class, name = "append"),
        @JsonSubTypes.Type(value = ResponseRemove.class, name = "remove"),
        @JsonSubTypes.Type(value = ResponseRename.class, name = "rename"),
        @JsonSubTypes.Type(value = ResponseReplace.class, name = "replace")
})
public abstract class BaseTransformer {
    public BaseTransformer() {
    }

    private ArrayList<String> body;
    private ArrayList<String> headers;
    private ArrayList<String> querystring;
    private String uri;
    private ArrayList<String> json;
    private ArrayList<String> json_types;

    public ArrayList<String> getBody() {
        return body;
    }

    public void setBody(ArrayList<String> body) {
        this.body = body;
    }

    public ArrayList<String> getHeaders() {
        return headers;
    }

    public void setHeaders(ArrayList<String> headers) {
        this.headers = headers;
    }

    public ArrayList<String> getQuerystring() {
        return querystring;
    }

    public void setQuerystring(ArrayList<String> querystring) {
        this.querystring = querystring;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public ArrayList<String> getJson() {
        return json;
    }

    public void setJson(ArrayList<String> json) {
        this.json = json;
    }

    public ArrayList<String> getJson_types() {
        return json_types;
    }

    public void setJson_types(ArrayList<String> json_types) {
        this.json_types = json_types;
    }
}
