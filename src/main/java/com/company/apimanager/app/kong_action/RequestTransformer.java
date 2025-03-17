package com.company.apimanager.app.kong_action;

import com.company.apimanager.app.KongServicePlugin;
import com.company.apimanager.entity.KongPluginType;
import com.company.apimanager.screen.publishedproduct.HandledError;

public class RequestTransformer {

    private static final String PLUGIN_DESCRIPTION = "Request Transformer";
    private static final String PLUGIN_TYPE = KongPluginType.REQUEST_TRANSFORMER.getId();
    private String kongManagementUrl;
    public RequestTransformer(String kongManagementUrl) {
        this.kongManagementUrl = kongManagementUrl;
    }

    public int createOrUpdate(String serviceName, String value, StringBuilder pluginId,
                              HandledError handledError) {
        Helper helper = new Helper(kongManagementUrl, PLUGIN_TYPE);
        if(pluginId.isEmpty()){
            int createRet = helper.createPlugin(serviceName, pluginId, handledError, value);
            if (createRet == 201) { //Successful
                return 0;
            }
            if (createRet != 409) {
                handledError.setErrorMsg("Could not create " + PLUGIN_DESCRIPTION + " : "
                        + handledError.getErrorMsg());
                return -1;
            }
        }
        KongServicePlugin kongServicePlugin = helper.getServicePluginByType(serviceName, PLUGIN_TYPE, handledError);
        if (kongServicePlugin == null) {
            handledError.setErrorMsg("Could not get " + PLUGIN_DESCRIPTION + " : "
                    + handledError.getErrorMsg());
            return -1;
        }
        int updateRet = helper.updatePlugin(serviceName,
                kongServicePlugin.getKongId(),
                handledError, value);
        if (updateRet != 200) {
            handledError.setErrorMsg("Could not update " + PLUGIN_DESCRIPTION + " : "
                    + handledError.getErrorMsg());
            return -1;
        }
        return 0;
    }

    public String getKongManagementUrl() {
        return kongManagementUrl;
    }

    public void setKongManagementUrl(String kongManagementUrl) {
        this.kongManagementUrl = kongManagementUrl;
    }
}
