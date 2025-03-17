package com.company.apimanager.app.kong_object;

import com.company.apimanager.app.kong_object.helper.*;
import org.springframework.stereotype.Component;

@Component
public class RequestTransformerTransformationsPluginConfig {
    public RequestRename rename;
    public RequestAppend append;
    public Object http_method;
    public RequestReplace replace;
    public RequestAdd add;
    public RequestRemove remove;

    public RequestTransformerTransformationsPluginConfig(RequestRename rename, RequestAppend append, Object http_method, RequestReplace replace, RequestAdd add, RequestRemove remove) {
        this.rename = rename;
        this.append = append;
        this.http_method = http_method;
        this.replace = replace;
        this.add = add;
        this.remove = remove;
    }

    public RequestTransformerTransformationsPluginConfig(RequestRename requestRename, RequestAppend requestAppend, RequestReplace replace, RequestAdd add, RequestRemove requestRemove) {
        this.rename = requestRename;
        this.append = requestAppend;
        this.replace = replace;
        this.add = add;
        this.remove = requestRemove;
        this.http_method = null;
    }

    public RequestTransformerTransformationsPluginConfig() {
//        this(null, null,  Arrays.asList(5, 12, 9, 3, 15, 88), null, null, null);
        this(null, null,  null, null, null, null);
    }

    public RequestRename getRename() {
        return rename;
    }

    public void setRename(RequestRename rename) {
        this.rename = rename;
    }

    public RequestAppend getAppend() {
        return append;
    }

    public void setAppend(RequestAppend append) {
        this.append = append;
    }

    public Object getHttp_method() {
        return http_method;
    }

    public void setHttp_method(Object http_method) {
        this.http_method = http_method;
    }

    public RequestReplace getReplace() {
        return replace;
    }

    public void setReplace(RequestReplace replace) {
        this.replace = replace;
    }

    public RequestAdd getAdd() {
        return add;
    }

    public void setAdd(RequestAdd add) {
        this.add = add;
    }

    public RequestRemove getRemove() {
        return remove;
    }

    public void setRemove(RequestRemove remove) {
        this.remove = remove;
    }
}
