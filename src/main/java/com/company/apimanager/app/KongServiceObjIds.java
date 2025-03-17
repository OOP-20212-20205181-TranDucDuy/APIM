package com.company.apimanager.app;

import org.springframework.stereotype.Component;

@Component
public class KongServiceObjIds {
    private String[] restApiList;
    private String[] servicesIdList;
    private String[] routesIdList;
    private String[] aclsIdList;
    private String[] provisionKeyList;

    public KongServiceObjIds() {

    }

    public String[] getRestApiList() {
        return restApiList;
    }

    public void setRestApiList(String[] restApiList) {
        this.restApiList = restApiList;
    }

    public String[] getAclsIdList() {
        return aclsIdList;
    }

    public String[] getRoutesIdList() {
        return routesIdList;
    }

    public String[] getServicesIdList() {
        return servicesIdList;
    }

    public void setAclsIdList(String[] aclsIdList) {
        this.aclsIdList = aclsIdList;
    }

    public void setRoutesIdList(String[] routesIdList) {
        this.routesIdList = routesIdList;
    }

    public void setServicesIdList(String[] servicesIdList) {
        this.servicesIdList = servicesIdList;
    }

    public String[] getProvisionKeyList() {
        return provisionKeyList;
    }

    public void setProvisionKeyList(String[] provisionKeyList) {
        this.provisionKeyList = provisionKeyList;
    }
}