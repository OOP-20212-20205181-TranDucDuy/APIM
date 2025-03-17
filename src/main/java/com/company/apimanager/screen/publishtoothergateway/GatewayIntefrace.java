package com.company.apimanager.screen.publishtoothergateway;

import com.company.apimanager.app.*;
import com.company.apimanager.app.kong_action.Cors;
import com.company.apimanager.app.kong_action.RequestTransformer;
import com.company.apimanager.entity.*;
import com.company.apimanager.screen.brandlogin.ApiRegister;
import com.company.apimanager.screen.publishedproduct.HandledError;
import io.jmix.ui.Notifications;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.net.URI;
import java.util.List;
import java.util.Locale;

@Component
public class GatewayIntefrace {
    private Site site;
    private static final String API_KEY = "api-key";
    private static final String[] OAUTH_SCOPES = {"api"};
    private static final String OBJ_ID_DELIMITER = ";";

    public GatewayIntefrace() {

    }

    public GatewayIntefrace(Site site) {
        this.site = site;
    }

    public void setSite(Site site) {
        this.site = site;
    }

    public Site getSite() {
        return site;
    }

    public int publishProductToGateway(Product productToPublish, String gwMgtEndpoint,
                                       List<ApiRegister> apiRegisters, StringBuilder kongObjIds,
                                       StringBuilder oauthObjInfo, StringBuilder kongMappingInfo, HandledError handledError) {
        int intRet = 0;
        KongConnector kongConnector = new KongConnector(gwMgtEndpoint);

        String tmpKongObjIds = "";
        String tmpOAuthInfo = "";
        String tmpMappingInfo = "";

        try {
            StringBuilder stringBuilder = new StringBuilder();
            StringBuilder info = new StringBuilder();
            StringBuilder mappingInfo = new StringBuilder();

            for (int i = 0; i < apiRegisters.size(); i++) {
                if (this.publishSingleApi(kongConnector, productToPublish, apiRegisters.get(i).getApi(),
                        stringBuilder, info, mappingInfo, handledError) != 0) {
                    intRet = -1;
                    handledError.setErrorMsg("Could not create API of  " +
                            apiRegisters.get(i).getApi().getName() + " : " + handledError.getErrorMsg());
                    break;
                } else {
                    if (i != apiRegisters.size() - 1) {
                        tmpKongObjIds = tmpKongObjIds + stringBuilder.toString() + " ";
                        tmpOAuthInfo = tmpOAuthInfo + info.toString() + " ";
                        tmpMappingInfo = tmpMappingInfo + mappingInfo.toString() + " ";
                    } else {
                        tmpKongObjIds = tmpKongObjIds + stringBuilder.toString();
                        tmpOAuthInfo = tmpOAuthInfo + info.toString();
                        tmpMappingInfo = tmpMappingInfo + mappingInfo.toString();
                    }
                }
            }
        } catch (Exception e) {
            handledError.setErrorMsg(e.getMessage());
            intRet = -1;
        }
        if (intRet == 0) {
            kongObjIds.setLength(0);
            kongObjIds.append(tmpKongObjIds);

            oauthObjInfo.setLength(0);
            oauthObjInfo.append(tmpOAuthInfo);
            kongMappingInfo.setLength(0);
            kongMappingInfo.append(tmpMappingInfo);
        }
        return intRet;
    }

    public int publishProduct(Product productToPublish, GatewayService gatewayService,
                              List<ApiRegister> apiRegisters, StringBuilder kongObjIds,
                              HandledError handledError) {
        int intRet = 0;
        //String gwMgtEndpoint = site.getGateway().getManagement_endpoint();
        String gwMgtEndpoint = gatewayService.getManagement_endpoint();
        KongConnector kongConnector = new KongConnector(gwMgtEndpoint);

        String tmpKongObjIds = "";
        try {
            StringBuilder stringBuilder = new StringBuilder();
            StringBuilder oauthInfo = new StringBuilder();
            StringBuilder mappingInfo = new StringBuilder();

            for (int i = 0; i < apiRegisters.size(); i++) {
                if (this.publishSingleApi(kongConnector, productToPublish,
                        apiRegisters.get(i).getApi(), stringBuilder, oauthInfo, mappingInfo,
                        handledError) != 0) {
                    intRet = -1;
                    handledError.setErrorMsg("Could not create API of  " +
                            apiRegisters.get(i).getApi().getName() + " : " + handledError.getErrorMsg());
                    break;
                } else {
                    if (i != apiRegisters.size() - 1) {
                        tmpKongObjIds = tmpKongObjIds + stringBuilder.toString() + " ";
                    } else {
                        tmpKongObjIds = tmpKongObjIds + stringBuilder.toString();
                    }
                }
            }
        } catch (Exception e) {
            handledError.setErrorMsg(e.getMessage());
            intRet = -1;
        }
        if (intRet == 0) {
            kongObjIds.setLength(0);
            kongObjIds.append(tmpKongObjIds);
        }
        return intRet;
    }

    public int publishSingleApi(KongConnector kongConnector, Product org_product, RestApi restApi,
                                StringBuilder apiObjIdBuilder, StringBuilder oauthServiceInfo, StringBuilder kongMappingInfo,
                                HandledError publishApiError) {
        if ((kongConnector == null) || (org_product == null) || (restApi == null)
                || (apiObjIdBuilder == null) || (publishApiError == null)) {
            if (publishApiError != null) {
                publishApiError.setErrorMsg("Parameter is NULL");
            }
            return -1;
        }

        int intRet = 0;
        String apiObjId = "";
        String kongObjInfo = "";
        String routeId = "";
        ApiSecurityMethod method = restApi.getSecurity_method();
        HandledError handledError = new HandledError();
        String serviceName = restApi.getOwner().getUsername()
                + "." + site.getName() + "." + restApi.getName();

        KongNewServiceInfo kongNewServiceInfo;
        if (restApi.getHost_group_path() != null) {    //Use balanced host group (upstream target)
            kongNewServiceInfo = new KongNewServiceInfo();
            kongNewServiceInfo.setName(serviceName);
            String upstreamName = serviceName + "_upstream";
            kongNewServiceInfo.setUpstreamName(upstreamName);
            kongNewServiceInfo.setUpstreamPath(restApi.getHost_group_path());
        } else {    //Not Use balanced host group (upstream target)
            kongNewServiceInfo = new KongNewServiceInfo(serviceName,
                    restApi.getTarget_endpoint());
        }

        if (restApi.getHost_group_path() != null) {    //Use balanced host group (upstream target)
            //Create upstream & targets
            String upstreamName = serviceName + "_upstream";
            KongUpstreamDetails kongUpstreamDetails = new KongUpstreamDetails();
            if (kongConnector.createOrUpdateUpStream(upstreamName, kongUpstreamDetails, handledError) != 0) {
                publishApiError.setErrorMsg("Could not create upstream on the API Gateway: " + handledError.getErrorMsg());
                return -1;
            }
            if (restApi.getBalancedHosts() == null) {
                publishApiError.setErrorMsg("Could not create upstream on the API Gateway: No backend host");
                return -1;
            } else {
                if (restApi.getBalancedHosts().size() == 0) {
                    publishApiError.setErrorMsg("Could not create upstream on the API Gateway: No backend host");
                    return -1;
                } else {
                    for (int i = 0; i < restApi.getBalancedHosts().size(); i++) {
                        KongTargetDetails kongTargetDetails = new KongTargetDetails();
                        if (kongConnector.createOrUpdateUpstreamTarget(upstreamName,
                                restApi.getBalancedHosts().get(i).getHostname(),
                                restApi.getBalancedHosts().get(i).getHost_port(),
                                restApi.getBalancedHosts().get(i).getWeight(),
                                kongTargetDetails, handledError) != 0) {
                            publishApiError.setErrorMsg("Could not create target of upstream on the API Gateway: "
                                    + handledError.getErrorMsg());
                            return -1;
                        }
                    }
                }
            }
        }

        KongNewRouteInfo kongNewRouteInfo = new KongNewRouteInfo(serviceName,
                serviceName + "_defaultRoute", "/"
                + restApi.getOwner().getUsername().toLowerCase()
                + "/" + site.getName().toLowerCase() + restApi.getBase_path().toLowerCase());
        //restApi.getName());

        KongServiceDetails kongServiceDetails = new KongServiceDetails();
        KongRouteDetails kongRouteDetails = new KongRouteDetails();

        int intService = kongConnector.createOrUpdateService(kongNewServiceInfo,
                kongServiceDetails, handledError);
        if (intService != 0) { // Not Successful
            publishApiError.setErrorMsg("Could not create service on the API Gateway: " + handledError.getErrorMsg());
            return -1;
        }

        int intRoute = kongConnector.createOrUpdateRoute(kongNewRouteInfo,
                kongRouteDetails, handledError);
        if (intRoute != 0) {
            publishApiError.setErrorMsg("Could not create route on the API Gateway: " + handledError.getErrorMsg());
            return -1;
        }

        apiObjId = kongServiceDetails.getId();

        //Security method
        boolean createAuth = true;
        if (method.getName().equals("Basic")) {
            if (kongConnector.enableOrUpdateServiceBasicAuth(apiObjId, handledError) != 0) {
                publishApiError.setErrorMsg("Could not create Basic auth the API Gateway: " + handledError.getErrorMsg());
                createAuth = false;
            } else {
                String oauthInfo = "" + OBJ_ID_DELIMITER + kongServiceDetails.getId();
                oauthServiceInfo.setLength(0);
                oauthServiceInfo.append(oauthInfo);
            }
        } else {
            if (method.getName().equals("API key")) {
                if (kongConnector.enableOrUpdateServiceApiKeyAuth(apiObjId, API_KEY, handledError) != 0) {
                    createAuth = false;
                    publishApiError.setErrorMsg("Could not create Key auth the API Gateway: " +
                            handledError.getErrorMsg());
                } else {
                    String oauthInfo = "" + OBJ_ID_DELIMITER + kongServiceDetails.getId();
                    oauthServiceInfo.setLength(0);
                    oauthServiceInfo.append(oauthInfo);
                }
            } else {
                //if (method.getName().equals("OAuth (client credentials)")) {
                if (method.getName().startsWith("OAuth")) {
                    int grantType = 2;
                    if (method.getName().equals("OAuth (Authorization code)")) {
                        grantType = 1;
                    }
                    else {
                        grantType = 2;
                    }
                    KongServiceOAuth kongServiceOAuth = kongConnector.enableOrUpdateServiceOAuth(apiObjId,
                            OAUTH_SCOPES, grantType, handledError);
                    if (kongServiceOAuth == null) {
                        createAuth = false;
                        publishApiError.setErrorMsg("Could not create OAuth auth the API Gateway: " +
                                handledError.getErrorMsg());
                    } else {
                        String oauthInfo = kongServiceOAuth.getProvisionKey() + OBJ_ID_DELIMITER +
                                kongServiceDetails.getId();
                        oauthServiceInfo.setLength(0);
                        oauthServiceInfo.append(oauthInfo);
                    }
                } else {
                    if (method.getName().equals("JWT")) {
                        if (kongConnector.enableOrUpdateServiceJwtAuth(apiObjId, handledError) != 0) {
                            publishApiError.setErrorMsg("Could not create JWT auth the API Gateway: " + handledError.getErrorMsg());
                            createAuth = false;
                        } else {
                            String oauthInfo = "" + OBJ_ID_DELIMITER + kongServiceDetails.getId();
                            oauthServiceInfo.setLength(0);
                            oauthServiceInfo.append(oauthInfo);
                        }
                    } else {
                        createAuth = false;
                        publishApiError.setErrorMsg("The API has unknown security method!");
                    }
                }
            }
        }
        if (!createAuth) {
            return -1;
        }

        String newGroupName = site.getOwner().getUsername() + "."
                + site.getName() + "." + org_product.getName();
        KongAclDetails kongAclDetails = new KongAclDetails();
        if (kongConnector.createOrUpdateAcl(serviceName, newGroupName, kongAclDetails, handledError) != 0) {
            publishApiError.setErrorMsg("Could not update ACL of service: " + handledError.getErrorMsg());
            return -1;
        }

        int ret = 0;

        //Create Request Transformer
        if (StringUtils.hasText(restApi.getRequestTransformer())) {
            StringBuilder pluginId = new StringBuilder();
            RequestTransformer requestTransformerAction = new RequestTransformer(kongConnector.getKongManagementUrl());
            ret = requestTransformerAction.createOrUpdate(serviceName, restApi.getRequestTransformer(), pluginId, handledError);
            if (ret != 0) {
                publishApiError.setErrorMsg("Could not set Request Transformer: " + handledError.getErrorMsg());
                return -1;
            }
        }

        //Create Response Transformer
        if (restApi.getResponseTransformer() != null) {
            if (!restApi.getResponseTransformer().isEmpty() && !restApi.getResponseTransformer().isBlank()) {
                StringBuilder pluginId = new StringBuilder();
                ret = kongConnector.createOrUpdateResponseTransformerPlugin(serviceName, restApi.getResponseTransformer(), pluginId, handledError);
                if (ret != 0) {
                    publishApiError.setErrorMsg("Could not set Response Transformer: " + handledError.getErrorMsg());
                    return -1;
                }
            }
        }

        // CORS
        if (StringUtils.hasText(restApi.getCors())) {
            StringBuilder pluginId = new StringBuilder();
            Cors corsAction = new Cors(kongConnector.getKongManagementUrl());
            ret = corsAction.createOrUpdate(serviceName, restApi.getCors(), pluginId, handledError);
            if (ret != 0) {
                publishApiError.setErrorMsg("Could not set CORS: " + handledError.getErrorMsg());
                return -1;
            }
        }

        //Schema validation
        if (restApi.getEnable_validation_schema() != null && restApi.getEnable_validation_schema() &&
                (!restApi.getBody_schema().isBlank() || !restApi.getParameter_schema().isBlank())
        ) {
            if (kongConnector.createOrUpdateValidationSchemaPlugin(serviceName, restApi, handledError) != 0) {
                publishApiError.setErrorMsg("Could not set validation schema plugin: " + handledError.getErrorMsg());
                return -1;
            }
        }

        apiObjId = kongServiceDetails.getId() + OBJ_ID_DELIMITER + kongRouteDetails.getId() + OBJ_ID_DELIMITER
                + kongAclDetails.getId();
        kongObjInfo = kongServiceDetails.getId() + OBJ_ID_DELIMITER + restApi.getId().toString();
        apiObjIdBuilder.setLength(0);
        apiObjIdBuilder.append(apiObjId);
        kongMappingInfo.setLength(0);
        kongMappingInfo.append(kongObjInfo);
        return 0;
    }

    private int setHostPath(KongServiceDetails kongServiceDetails, RestApi restApi) {
        String host = null;
        String path = null;
        int intRet = 0;
        try {
            URI uri = new URI(restApi.getTarget_endpoint());
            host = uri.getHost();
            ;
            path = uri.getPath();
        } catch (Exception e) {
            host = null;
            path = null;
            intRet = -1;
        }
        if (intRet == 0) {
            kongServiceDetails.setHost(host);
            kongServiceDetails.setPath(path);
        }
        return intRet;
    }
}