package com.company.apimanager.app;

import org.springframework.stereotype.Component;

@Component
public class CallDrupalApiError {
    private String errorMsg;
    private int httpResponseCode;

    public CallDrupalApiError() {

    }

    public void setHttpResponseCode(int httpResponseCode) {
        this.httpResponseCode = httpResponseCode;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public int getHttpResponseCode() {
        return httpResponseCode;
    }
}