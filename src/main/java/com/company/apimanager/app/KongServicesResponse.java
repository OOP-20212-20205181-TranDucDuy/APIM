package com.company.apimanager.app;

import org.springframework.stereotype.Component;

@Component
public class KongServicesResponse {
    private KongServiceDetails[] data;

    public KongServicesResponse() {

    }

    public KongServiceDetails[] getData() {
        return data;
    }

    public void setData(KongServiceDetails[] data) {
        this.data = data;
    }
}