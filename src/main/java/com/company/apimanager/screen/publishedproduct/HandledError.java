package com.company.apimanager.screen.publishedproduct;

import org.springframework.stereotype.Component;

@Component
public class HandledError {
    private String errorMsg;
    private String humanMsg;
    private int httpResponseCode;

    public HandledError() {

    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public String getHumanMsg() {
        return humanMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = (this.errorMsg != null && !this.errorMsg.isBlank()) ? errorMsg + "\n" + this.errorMsg : errorMsg;
    }

    public void setHumanMsg(String humanMsg) {
        this.humanMsg = humanMsg;
    }

    public int getHttpResponseCode() {
        return httpResponseCode;
    }
    public void setHttpResponseCode(int httpResponseCode) {
        this.httpResponseCode = httpResponseCode;
    }
    public void Assign(HandledError handledError) {
        if (handledError == null) {
            return;
        }
        this.errorMsg = handledError.getErrorMsg();
        this.httpResponseCode = handledError.getHttpResponseCode();
    }
}