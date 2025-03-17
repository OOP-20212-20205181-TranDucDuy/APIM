package com.company.apimanager.app.kong_object;

import com.company.apimanager.app.kong_object.helper.*;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.stereotype.Component;

@Component
public class ResponseTransformerTransformationsPluginConfig {
    private ResponseReplace replace;
    private ResponseRemove remove;
    private ResponseAdd add;
    private ResponseRename rename;
    private ResponseAppend append;

    public ResponseTransformerTransformationsPluginConfig() {
    }

    public ResponseTransformerTransformationsPluginConfig(ResponseReplace responseReplace, ResponseRemove responseRemove, ResponseAdd responseAdd, ResponseRename responseRename, ResponseAppend responseAppend) {
        this.replace = responseReplace;
        this.remove = responseRemove;
        this.add = responseAdd;
        this.rename = responseRename;
        this.append = responseAppend;
    }
    @JsonProperty("replace")
    public ResponseReplace getResponseReplace() {
        return replace;
    }

    public void setResponseReplace(ResponseReplace responseReplace) {
        this.replace = responseReplace;
    }

    @JsonProperty("remove")
    public ResponseRemove getResponseRemove() {
        return remove;
    }

    public void setResponseRemove(ResponseRemove responseRemove) {
        this.remove = responseRemove;
    }

    @JsonProperty("add")
    public ResponseAdd getResponseAdd() {
        return add;
    }

    public void setResponseAdd(ResponseAdd responseAdd) {
        this.add = responseAdd;
    }

    @JsonProperty("rename")
    public ResponseRename getResponseRename() {
        return rename;
    }

    public void setResponseRename(ResponseRename responseRename) {
        this.rename = responseRename;
    }
    @JsonProperty("append")
    public ResponseAppend getResponseAppend() {
        return append;
    }

    public void setResponseAppend(ResponseAppend responseAppend) {
        this.append = responseAppend;
    }
}
