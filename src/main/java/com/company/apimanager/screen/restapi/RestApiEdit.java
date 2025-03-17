package com.company.apimanager.screen.restapi;

import com.company.apimanager.app.Utility;
import com.company.apimanager.app.kong_object.*;
import com.company.apimanager.app.kong_object.cors.CorsPlugin;
import com.company.apimanager.app.kong_object.cors.CorsPluginConfig;
import com.company.apimanager.app.kong_object.helper.*;
import com.company.apimanager.entity.*;
import com.company.apimanager.screen.apicall.ApiCallEdit;
import com.company.apimanager.screen.ide.IdeScreen;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.hash.Hashing;
import com.company.apimanager.screen.publishedproduct.HandledError;
import io.jmix.core.EntityStates;
import io.jmix.core.security.CurrentAuthentication;
import io.jmix.ui.Notifications;
import io.jmix.ui.ScreenBuilders;
import io.jmix.ui.Screens;
import io.jmix.ui.component.*;
import io.jmix.ui.screen.*;
import org.jsoup.Jsoup;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;


import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Pattern;


//import javax.jws.soap.SOAPBinding;

@UiController("RestApi.edit")
@UiDescriptor("rest-api-edit.xml")
@EditedEntityContainer("restApiDc")
public class RestApiEdit extends StandardEditor<RestApi> {


    private boolean isNewEntity;
    @Autowired
    private CurrentAuthentication currentAuthentication;
    @Autowired
    private EntityStates entityStates;
    @Autowired
    private TextField<String> nameField;
    @Autowired
    private Notifications notifications;
    @Autowired
    private TextField<String> base_pathField;
    @Autowired
    private EntityPicker<ApiSecurityMethod> security_methodField;

    @Autowired
    private RichTextArea documentationField;

    @Autowired
    private CheckBox enable_docField;
    @Autowired
    private ComboBox target_securityField;
    @Autowired
    private TextField target_userField;
    @Autowired
    private TextField target_passwordField;
    @Autowired
    private TextField target_apiKeyNameField;
    @Autowired
    private TextField target_apiKeyValueField;
    @Autowired
    private RichTextArea body_schemaField;
    @Autowired
    private RichTextArea parameter_schemaField;
    @Autowired
    private TextArea allow_content_typeField;
    @Autowired
    Utility utility;

    @Autowired
    private CheckBox enable_validation_schemaField;
    @Autowired
    private CheckBox verbose_responseField;
    @Autowired
    private GroupBoxLayout jwt_target_group;
    @Autowired
    private GroupBoxLayout basic_target_group;
    @Autowired
    private GroupBoxLayout apikey_target_group;
    @Autowired
    private GroupBoxLayout jwt_sub_group;
    @Autowired
    private TextArea jwtSignatureField;
    @Autowired
    private TextArea jwtPayloadrField;
    @Autowired
    private TextArea jwtHeadedrField;
    @Autowired
    private TextArea target_jwtValueField;


    /// REQUEST TRANSFORMER
//    @Autowired
//    private TextArea txtAddBodyRequest;
//    @Autowired
//    private TextArea txtRemoveBodyRequestTxt;
//    @Autowired
//    private TextArea txtRenameBodyRequestTxt;
//    @Autowired
//    private TextArea txtReplaceBodyRequestTxt;
//    @Autowired
//    private TagField txtRemoveBodyRequestTagField;

    //////////////////////////////////////////

    @Autowired
    private ValuesPicker vpReplaceHeaderRequestValuePicker;
    @Autowired
    private ValuesPicker vpRenameHeaderRequestValuePicker;
    @Autowired
    private ValuesPicker vpRemoveHeaderRequestValuePicker;
    @Autowired
    private ValuesPicker vpAppendHeaderRequestValuePicker;
    @Autowired
    private ValuesPicker vpAddHeaderRequestValuePicker;
    @Autowired
    private ValuesPicker<String> vpReplaceBodyRequestValuePicker;
    @Autowired
    private ValuesPicker<String> vpRenameBodyRequestValuePicker;
    @Autowired
    private ValuesPicker<String> vpRemoveBodyRequestValuePicker;
    @Autowired
    private ValuesPicker<String> vpAppendBodyRequestValuePicker;
    @Autowired
    private ValuesPicker<String> vpAddBodyRequestValuePicker;
    @Autowired
    private ValuesPicker<String> vpAddQueryRequestValuePicker;
    @Autowired
    private ValuesPicker<String> vpReplaceQueryRequestValuePicker;
    @Autowired
    private ValuesPicker<String> vpRenameQueryRequestValuePicker;
    @Autowired
    private ValuesPicker<String> vpRemoveQueryRequestValuePicker;
    @Autowired
    private ValuesPicker<String> vpAppendQueryRequestValuePicker;
    @Autowired
    private Screens screens;
    @Autowired
    private TextField<String> titleField;
    @Autowired
    private Button testEndpointBtn;

    @Subscribe
    public void onInitEntity(InitEntityEvent<RestApi> event) {
        isNewEntity = true;
    }


    private String jsonFormater(String json) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(json);
            return jsonNode.toString();
        } catch (Exception e) {
            notifications.create(Notifications.NotificationType.WARNING).withCaption("JWT is not valid").show();
        }
        return "";
    }

    private void hideAllTargetSercurities() {
        jwt_target_group.setVisible(false);
        basic_target_group.setVisible(false);
        apikey_target_group.setVisible(false);
    }

    @Autowired
    private CheckBox use_balancingGroupField;
    @Autowired
    private TextField<String> target_hostGroupPathField;
    @Autowired
    private TextField<String> target_endpointField;
    @Autowired
    private Table<BalancedHost> hostsTable;
    @Autowired
    private GroupBoxLayout hostsBox;

    @Subscribe
    public void onAfterInit(AfterInitEvent event) {
        User currUser = (User) currentAuthentication.getUser();
    }


    @Subscribe
    public void onAfterShow(AfterShowEvent event) throws JsonProcessingException {

        //Pricing plan
        User currUser = (User) currentAuthentication.getUser();
        PricingPlan currPlan = currUser.getPricingPlan();
        if (currPlan == null) {
            notifications.create(Notifications.NotificationType.WARNING).
                    withCaption("You have no pricing plan. Please contact Cloud administrator").show();
            nameField.setEnabled(false);
            base_pathField.setEnabled(false);
            security_methodField.setEnabled(false);
            use_balancingGroupField.setEnabled(false);
            target_hostGroupPathField.setEnabled(false);
            hostsTable.setEnabled(false);
            hostsBox.setEnabled(false);
            target_endpointField.setEnabled(false);
            target_passwordField.setEnabled(false);
            target_userField.setEnabled(false);
            titleField.setEnabled(false);
            enable_docField.setEnabled(false);
            enable_validation_schemaField.setEnabled(false);
            testEndpointBtn.setEnabled(false);
            return;
        }

        if (isNewEntity) {
            use_balancingGroupField.setValue(false);
        } else {
            setResponseTransformerForUI();
            setRequestTransformerForUI();

            if (StringUtils.hasText(getEditedEntity().getRequestTransformer())) {
                request_transformerCheckBox.setValue(true);
            }
            if (StringUtils.hasText(getEditedEntity().getResponseTransformer())) {
                response_transformerCheckBox.setValue(true);
            }
            if (StringUtils.hasText(getEditedEntity().getCors())) {
                corsCheckBox.setValue(true);
                setValueForCorsUI();
            }
        }
        jwtHeadedrField.setValue("{\"alg\":\"HS256\",\"typ\":\"JWT\"}");
        jwtPayloadrField.setValue("{\"sub\":\"1234567890\",\"name\":\"John Doe\",\"iat\":1516239022}");
        jwtSignatureField.setValue("your-256-bit-secret");
        jwt_sub_group.setExpanded(false);
        List<String> secutityList = new ArrayList<String>();
        secutityList.add("Basic");
        secutityList.add("API key");
        secutityList.add("JWT");
        target_securityField.setOptionsList(secutityList);
        target_securityField.setTextInputAllowed(false);
        use_balancingGroupField.setValue(false);
        target_hostGroupPathField.setVisible(false);
        target_endpointField.setVisible(true);

        if (!entityStates.isNew(getEditedEntity())) {
            nameField.setEditable(false);
            base_pathField.setEditable(false);
            security_methodField.setEditable(false);

            if ((getEditedEntity().getBalancedHosts() == null) || (getEditedEntity().getHost_group_path() == null)) {
                use_balancingGroupField.setValue(false);
                target_hostGroupPathField.setVisible(false);
                hostsTable.setVisible(false);
                hostsBox.setVisible(false);
                target_endpointField.setVisible(true);
            } else {
                if ((getEditedEntity().getBalancedHosts().size() == 0) || (getEditedEntity().getHost_group_path().isEmpty())) {
                    use_balancingGroupField.setValue(false);
                    target_hostGroupPathField.setVisible(false);
                    hostsTable.setVisible(false);
                    hostsBox.setVisible(false);
                    target_endpointField.setVisible(true);
                } else {
                    use_balancingGroupField.setValue(true);
                    target_hostGroupPathField.setVisible(true);
                    hostsTable.setVisible(true);
                    hostsBox.setVisible(true);
                    target_endpointField.setVisible(false);
                }
            }

            String upstreamHeaderName = getEditedEntity().getUpstream_header_name();
            String upstreamHeaderValue = getEditedEntity().getUpstream_header_value();
            if (upstreamHeaderName == null) {
                target_securityField.setValue("");
                hideAllTargetSercurities();
            } else {
                if (upstreamHeaderName.equals("")) {
                    target_securityField.setValue("");
                    hideAllTargetSercurities();
                } else {
                    if (upstreamHeaderName.equals("Authorization")) {
                        if (upstreamHeaderValue.contains("Bearer ")) {
                            jwt_target_group.setVisible(true);
                            target_securityField.setValue("JWT");
                            String[] splitStr = upstreamHeaderValue.split("\\s+");
                            String[] message = splitStr[1].split("\\.");
                            target_jwtValueField.setValue(upstreamHeaderValue);
                            jwtHeadedrField.setValue(Utility.decodeBase64(message[0]));
                            jwtPayloadrField.setValue(Utility.decodeBase64(message[1]));
                            jwtSignatureField.setValue("");
                        } else {
                            hideAllTargetSercurities();
                            target_passwordField.setVisible(true);
                            target_userField.setVisible(true);

                            if (upstreamHeaderValue != null) {
                                if (upstreamHeaderValue.length() > 7) {
                                    String basicStr = upstreamHeaderValue.substring(0, 6);
                                    String encodedStr = upstreamHeaderValue.substring(6, upstreamHeaderValue.length());
                                    if (basicStr.equals("Basic ")) {
                                        String decodedStr = Utility.decodeBase64(encodedStr);
                                        if (!decodedStr.equals("")) {
                                            String[] listStr = decodedStr.split(":");
                                            if (listStr != null) {
                                                if (listStr.length > 1) {
                                                    target_securityField.setValue("Basic");
                                                    target_userField.setValue(listStr[0]);
                                                    try {
                                                        target_passwordField.setValue(decodedStr.substring(listStr[0].length() + 1, decodedStr.length()));
                                                    } catch (Exception e) {
                                                        target_passwordField.setValue("");
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                    } else {
                        target_securityField.setValue("API key");
                        hideAllTargetSercurities();
                        apikey_target_group.setVisible(true);
                        target_apiKeyNameField.setValue(upstreamHeaderName);
                        target_apiKeyValueField.setValue(upstreamHeaderValue);
                    }
                }
            }
        }
    }

    @Subscribe("use_balancingGroupField")
    public void onUse_balancingGroupFieldValueChange(HasValue.ValueChangeEvent<Boolean> event) {
        if (event == null) {
            target_hostGroupPathField.setVisible(false);
            hostsBox.setVisible(false);
            hostsTable.setVisible(false);
            return;
        }
        if (event.getValue()) {
            target_hostGroupPathField.setVisible(true);
            hostsBox.setVisible(true);
            hostsTable.setVisible(true);
            target_endpointField.setVisible(false);
        } else {
            target_hostGroupPathField.setVisible(false);
            hostsTable.setVisible(false);
            hostsBox.setVisible(false);
            target_endpointField.setVisible(true);

            if (!entityStates.isNew(getEditedEntity())) {
                target_endpointField.setValue("");
            }
        }
    }

    @Subscribe("target_securityField")
    public void onTarget_securityFieldValueChange(HasValue.ValueChangeEvent event) {

        if (event.getValue() == null) {
            hideAllTargetSercurities();
            return;
        }
        String valueSecurity = event.getValue().toString();
        if (valueSecurity == null) {
            hideAllTargetSercurities();
            return;
        }
        if (valueSecurity.equals("")) {
            hideAllTargetSercurities();
            return;
        }
        if (valueSecurity.equals("Basic")) {
            hideAllTargetSercurities();
            basic_target_group.setVisible(true);
            return;

        }
        if (valueSecurity.equals("API key")) {
            hideAllTargetSercurities();
            apikey_target_group.setVisible(true);
            return;
        }
        if (valueSecurity.equals("JWT")) {
            hideAllTargetSercurities();
            jwt_target_group.setVisible(true);
            return;
        }
        hideAllTargetSercurities();
    }

    @Subscribe("nameField")
    public void onNameFieldValueChange(HasValue.ValueChangeEvent<String> event) {
        String nameValue = nameField.getValue();
        if (nameValue == null) {
            nameValue = "";
        }

        if (isNewEntity) {
            base_pathField.setValue("/" + nameValue.toLowerCase());
        }
    }

    @Subscribe
    public void onBeforeShow(BeforeShowEvent event) {
        boolean enableDoc = getEditedEntity().getEnable_doc() != null && getEditedEntity().getEnable_doc();
        documentationField.setVisible(enableDoc);
    }


    @Subscribe(id = "enable_docField")
    public void onEnableChange(HasValue.ValueChangeEvent<Boolean> event) {
        documentationField.setVisible(Boolean.TRUE.equals(event.getValue()));
    }

    @Subscribe(id = "enable_validation_schemaField")
    public void onEnableValidateChange(HasValue.ValueChangeEvent<Boolean> event) {
        body_schemaField.setVisible(Boolean.TRUE.equals(event.getValue()));
        parameter_schemaField.setVisible(Boolean.TRUE.equals(event.getValue()));
        allow_content_typeField.setVisible(Boolean.TRUE.equals(event.getValue()));
        verbose_responseField.setVisible(Boolean.TRUE.equals(event.getValue()));
    }


    @Subscribe("genHs256Button")
    protected void onGenHs256ButtonClick(Button.ClickEvent event) throws DecoderException {
        try {
            String encodedHeader = Base64.getUrlEncoder().encodeToString(jsonFormater(jwtHeadedrField.getRawValue()).getBytes());
            String encodedPayload = Base64.getUrlEncoder().encodeToString(jsonFormater(jwtPayloadrField.getRawValue()).getBytes());
            String concatenated = encodedHeader + '.' + encodedPayload;
            String key = jwtSignatureField.getRawValue();
            String hash = Hashing.hmacSha256(key.getBytes(StandardCharsets.UTF_8)).hashString(concatenated.replace("=", ""), StandardCharsets.UTF_8).toString();
            byte[] decodedHex = Hex.decodeHex(hash.toCharArray());
            String result = org.apache.commons.codec.binary.Base64.encodeBase64String(decodedHex).replace("/", "_").replace("=", "");

            target_jwtValueField.setValue("Bearer " + concatenated.replace("=", "") + '.' + result);
        } catch (Exception ex) {
            notifications.create(Notifications.NotificationType.WARNING).withCaption("JWT token cannot be generated").show();
        }

    }

    @Subscribe
    public void onBeforeCommitChanges(BeforeCommitChangesEvent event) {
        // Tab transformer
        if (this.doTargetSecurity() != 0) {
            event.preventCommit();
            return;
        }
        checkValid(event);
        //------------
        String name = getEditedEntity().getName();
        if (Utility.findSpecialChar(name) != 0) {
            notifications.create(Notifications.NotificationType.WARNING).withCaption("Name must not contain special characters").show();
            event.preventCommit();
            return;
        }

        String basePath = getEditedEntity().getBase_path();
        String tmpBasePath = basePath.substring(1, basePath.length());
        if (Utility.findSpecialChar(tmpBasePath) != 0) {
            notifications.create(Notifications.NotificationType.WARNING).withCaption("Base path must not contain special characters").show();
            event.preventCommit();
            return;
        }
        if (!basePath.substring(0, 1).equals("/")) {
            notifications.create(Notifications.NotificationType.WARNING).withCaption("Base path not valid").show();
            event.preventCommit();
            return;
        }

        HandledError handledError = new HandledError();

        if (getEditedEntity().getBody_schema() != null && getEditedEntity().getEnable_validation_schema()) {
            String schema = Jsoup.parse(getEditedEntity().getBody_schema()).text();
            boolean validateSchema = utility.validateBodySchema(schema, handledError);
            if (!validateSchema) {
                notifications.create(Notifications.NotificationType.WARNING).withCaption(handledError.getErrorMsg()).show();
                event.preventCommit();
                return;
            }
            getEditedEntity().setBody_schema(schema);
        }

        if (getEditedEntity().getParameter_schema() != null && getEditedEntity().getEnable_validation_schema()) {
            String schema = Jsoup.parse(getEditedEntity().getParameter_schema()).text();
            boolean validateSchema = utility.validateSchema(schema, handledError);
            if (!validateSchema) {
                notifications.create(Notifications.NotificationType.WARNING).withCaption(handledError.getErrorMsg()).show();
                event.preventCommit();
                return;
            }
            getEditedEntity().setParameter_schema(schema);
        }

        User currUser = (User) currentAuthentication.getUser();

        if (getEditedEntity().getEnable_doc() == null || !getEditedEntity().getEnable_doc()) {
            getEditedEntity().setDocumentation("");
        }


        if (use_balancingGroupField.getValue()) {
            boolean hostNull = false;
            if (getEditedEntity().getBalancedHosts() == null) {
                hostNull = true;
            } else {
                if (getEditedEntity().getBalancedHosts().size() == 0) {
                    hostNull = true;
                }
            }
            if ((hostNull) || (target_hostGroupPathField.getValue() == null)) {
                notifications.create(Notifications.NotificationType.WARNING).withCaption("Target host group or path NULL").show();
                event.preventCommit();
                return;
            } else {
                String groupPath = target_hostGroupPathField.getValue();
                String tmpGroupPath = groupPath.substring(1, groupPath.length());
                if (Utility.findSpecialChar(tmpGroupPath) != 0) {
                    notifications.create(Notifications.NotificationType.WARNING).withCaption("Host group path must not contain special characters").show();
                    event.preventCommit();
                    return;
                }

                if (!groupPath.substring(0, 1).equals("/")) {
                    notifications.create(Notifications.NotificationType.WARNING).withCaption("Host group path not valid. Must begin with character \"/\"").show();
                    event.preventCommit();
                    return;
                }
            }
            getEditedEntity().setTarget_endpoint(target_hostGroupPathField.getValue());
        } else {
            if (!Utility.validateUrl(target_endpointField.getValue())) {
                notifications.create(Notifications.NotificationType.WARNING).withCaption("Target endpoint is not valid URL").show();
                event.preventCommit();
                return;
            }
            getEditedEntity().setHost_group_path(null);
            getEditedEntity().setBalancedHosts(null);
        }

        getEditedEntity().setOwner(currUser);
        getEditedEntity().setOwner_apiname(currUser.getUsername() + "_" + getEditedEntity().getName());
        getEditedEntity().setPath_in_gw(currUser.getUsername().toLowerCase() + "/" + getEditedEntity().getBase_path().toLowerCase());

        loadRequestTransformerDataFromUI();
        loadResponseTransformerDataFromUI();
        getValueFromCorsUI();

    }

    private void loadRequestTransformerDataFromUI() {
        if (request_transformerCheckBox.isChecked()) {
            ArrayList<String> rn = Utility.vpToArrLst(vpRenameBodyRequestValuePicker);
            ArrayList<String> ad = Utility.vpToArrLst(vpAddBodyRequestValuePicker);
            ArrayList<String> rm = Utility.vpToArrLst(vpRemoveBodyRequestValuePicker);
            ArrayList<String> rp = Utility.vpToArrLst(vpReplaceBodyRequestValuePicker);
            ArrayList<String> ap = Utility.vpToArrLst(vpAppendBodyRequestValuePicker);

            ArrayList<String> rnq = Utility.vpToArrLst(vpRenameQueryRequestValuePicker);
            ArrayList<String> adq = Utility.vpToArrLst(vpAddQueryRequestValuePicker);
            ArrayList<String> rmq = Utility.vpToArrLst(vpRemoveQueryRequestValuePicker);
            ArrayList<String> rpq = Utility.vpToArrLst(vpReplaceQueryRequestValuePicker);
            ArrayList<String> apq = Utility.vpToArrLst(vpAppendQueryRequestValuePicker);
            getEditedEntity().setRequestTransformer(getRequestTransformerFromUI(Utility.vpToArrLst(vpRemoveHeaderRequestValuePicker), Utility.vpToArrLst(vpRenameHeaderRequestValuePicker), Utility.vpToArrLst(vpReplaceHeaderRequestValuePicker), Utility.vpToArrLst(vpAddHeaderRequestValuePicker), Utility.vpToArrLst(vpAppendHeaderRequestValuePicker), rm, rn, rp, ad, ap, rmq, rnq, rpq, adq, apq));
        } else {
            getEditedEntity().setRequestTransformer(null);
        }
    }

    private void loadResponseTransformerDataFromUI() {
        if (response_transformerCheckBox.isChecked()) {
            getEditedEntity().setResponseTransformer(getResponseTransformerFromUI(Utility.vpToArrLst(vpRemoveHeaderResponseValuePicker), Utility.vpToArrLst(vpRenameHeaderResponseValuePicker), Utility.vpToArrLst(vpReplaceHeaderResponseValuePicker), Utility.vpToArrLst(vpAddHeaderResponseValuePicker), Utility.vpToArrLst(vpAppendHeaderResponseValuePicker), Utility.vpToArrLst(vpRemoveJsonResponseValuePicker), Utility.vpToArrLst(vpReplaceJsonResponseValuePicker), Utility.vpToArrLst(vpAddJsonResponseValuePicker), Utility.vpToArrLst(vpAppendJsonResponseValuePicker), Utility.vpToArrLst(vpReplaceTypeResponseValuePicker), Utility.vpToArrLst(vpAddTypeResponseValuePicker), Utility.vpToArrLst(vpAppendTypeResponseValuePicker)));
        } else {
            getEditedEntity().setResponseTransformer(null);
        }
    }

    private String getRequestTransformerFromUI(ArrayList<String> removeHeader, ArrayList<String> renameHeader, ArrayList<String> replaceHeader, ArrayList<String> addHeader, ArrayList<String> appendHeader, ArrayList<String> removeBody, ArrayList<String> renameBody, ArrayList<String> replaceBody, ArrayList<String> addBody, ArrayList<String> appendBody, ArrayList<String> removeQuery, ArrayList<String> renameQuery, ArrayList<String> replaceQuery, ArrayList<String> addQuery, ArrayList<String> appendQuery) {
        try {
            RequestRemove requestRemove = new RequestRemove(removeBody, removeHeader, removeQuery);
            RequestRename requestRename = new RequestRename(renameBody, renameHeader, renameQuery);
            RequestReplace requestReplace = new RequestReplace(replaceBody, replaceHeader, replaceQuery, null);
            RequestAdd requestAdd = new RequestAdd(addBody, addHeader, addQuery);
            RequestAppend requestAppend = new RequestAppend(appendBody, appendHeader, appendQuery);

            RequestTransformerTransformationsPluginConfig config = new RequestTransformerTransformationsPluginConfig(requestRename, requestAppend, requestReplace, requestAdd, requestRemove);
            RequestTransformerTransformationsPlugin requestTransformer = new RequestTransformerTransformationsPlugin(config);
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
            return objectMapper.writeValueAsString(requestTransformer);
        } catch (Exception e) {
            notifications.create(Notifications.NotificationType.WARNING).withCaption("Can't Get Body Request Transformer: " + e.getMessage()).show();
        }
        return "";
    }

    @Autowired
    private ValuesPicker<String> vpReplaceTypeResponseValuePicker;
    @Autowired
    private ValuesPicker<String> vpReplaceJsonResponseValuePicker;
    @Autowired
    private ValuesPicker<String> vpReplaceHeaderResponseValuePicker;
    @Autowired
    private ValuesPicker<String> vpRemoveJsonResponseValuePicker;
    @Autowired
    private ValuesPicker<String> vpRenameHeaderResponseValuePicker;
    @Autowired
    private ValuesPicker<String> vpRemoveHeaderResponseValuePicker;
    @Autowired
    private ValuesPicker<String> vpAppendTypeResponseValuePicker;
    @Autowired
    private ValuesPicker<String> vpAppendJsonResponseValuePicker;
    @Autowired
    private ValuesPicker<String> vpAppendHeaderResponseValuePicker;
    @Autowired
    private ValuesPicker<String> vpAddTypeResponseValuePicker;
    @Autowired
    private ValuesPicker<String> vpAddJsonResponseValuePicker;
    @Autowired
    private ValuesPicker<String> vpAddHeaderResponseValuePicker;

    private String getResponseTransformerFromUI(ArrayList<String> removeHeader, ArrayList<String> renameHeader, ArrayList<String> replaceHeader, ArrayList<String> addHeader, ArrayList<String> appendHeader, ArrayList<String> removeJson, ArrayList<String> replaceJson, ArrayList<String> addJson, ArrayList<String> appendJson, ArrayList<String> replaceJsonType, ArrayList<String> addJsonType, ArrayList<String> appendJsonType) {
        try {
            ResponseRemove responseRemove = new ResponseRemove(removeHeader, removeJson);
            ResponseRename responseRename = new ResponseRename(renameHeader);
//            ResponseReplace responseReplace = new ResponseReplace(replaceHeader, replaceJson, replaceJsonType);
//            ResponseAdd responseAdd = new ResponseAdd(addHeader, addJson, addJsonType);
//            ResponseAppend responseAppend = new ResponseAppend(appendHeader, appendJson, appendJsonType);
            ResponseReplace responseReplace = new ResponseReplace(replaceHeader, replaceJson, null);
            ResponseAdd responseAdd = new ResponseAdd(addHeader, addJson, null);
            ResponseAppend responseAppend = new ResponseAppend(appendHeader, appendJson, null);

            ResponseTransformerTransformationsPluginConfig config = new ResponseTransformerTransformationsPluginConfig(responseReplace, responseRemove, responseAdd, responseRename, responseAppend);
            ResponseTransformerTransformationsPlugin responseTransformer = new ResponseTransformerTransformationsPlugin(config);

            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
            return objectMapper.writeValueAsString(responseTransformer);
        } catch (Exception e) {
            notifications.create(Notifications.NotificationType.WARNING).withCaption("Can't Get Body Response Transformer: " + e.getMessage()).show();
        }
        return "";
    }

    private boolean checkRegexAndNotification(ValuesPicker<String> vp, Pattern p) {
        if (vp.getValue() == null) {
            return false;
        }
        if (!vp.getValue().stream().allMatch(st -> p.matcher(st).matches())) {
            notifications.create(Notifications.NotificationType.WARNING).withCaption("Please check " + vp.getDescription()).show();
            vp.addStyleName("v-label-failure");
            return true;
        }
        vp.removeStyleName("v-label-failure");
        return false;
    }

    private boolean checkKeyAndNotification(ValuesPicker<String> vp) {
        if (vp.getValue() == null) {
            return false;
        }
        List<String> keyLst = new ArrayList<String>();
        for (String ele : Utility.vpToLst(vp)) {
            keyLst.add(ele.split(":", 0)[0]);
        }
        Set<String> valueSet = new HashSet<>(keyLst);
        if (valueSet.size() != keyLst.size()) {
            notifications.create(Notifications.NotificationType.WARNING).withCaption("Please check " + vp.getDescription()).show();
            vp.addStyleName("v-label-failure");
            return true;
        }
        vp.removeStyleName("v-label-failure");
        return false;
    }

    private static Pattern pValue = Pattern.compile("^[a-zA-Z0-9_-]*");
    private static Pattern pKeyValue = Pattern.compile("(^[\\w- \\.]+:[^\\:]+)$");
    private static Pattern pKeyKey = Pattern.compile("(^[\\w- \\.]+:[\\w- \\.]+)$");
    private static Pattern pJsonType = Pattern.compile("boolean|number|string");

    private void checkValid(BeforeCommitChangesEvent event) {
        List<ValuesPicker> checkPValueList = new ArrayList<ValuesPicker>(Arrays.asList(vpRemoveBodyRequestValuePicker, vpRemoveQueryRequestValuePicker, vpRemoveJsonResponseValuePicker, vpRemoveHeaderResponseValuePicker));
        for (ValuesPicker ele : checkPValueList) {
            if (checkRegexAndNotification(ele, pValue)) {
                event.preventCommit();
                return;
            }
        }
        List<ValuesPicker> checkPKeyValueList = new ArrayList<ValuesPicker>(Arrays.asList(vpAddHeaderRequestValuePicker, vpAppendHeaderRequestValuePicker,
                vpAddBodyRequestValuePicker, vpAppendBodyRequestValuePicker,
                vpAddQueryRequestValuePicker, vpAppendQueryRequestValuePicker));
        List<ValuesPicker> checkPKeyKeyList = new ArrayList<ValuesPicker>(Arrays.asList(vpRenameHeaderRequestValuePicker, vpReplaceHeaderRequestValuePicker,
                vpRenameBodyRequestValuePicker, vpReplaceBodyRequestValuePicker,
                vpRenameQueryRequestValuePicker, vpReplaceQueryRequestValuePicker));
        List<ValuesPicker> checkPKeyValueResponseList = new ArrayList<ValuesPicker>(Arrays.asList(vpAppendHeaderResponseValuePicker, vpAddJsonResponseValuePicker, vpAddHeaderResponseValuePicker, vpAppendJsonResponseValuePicker));
        List<ValuesPicker> checkPKeyKeyResponseList = new ArrayList<ValuesPicker>(Arrays.asList(vpReplaceJsonResponseValuePicker, vpReplaceHeaderResponseValuePicker, vpRenameHeaderResponseValuePicker));
        checkPKeyValueList.addAll(checkPKeyValueResponseList);
        checkPKeyKeyList.addAll(checkPKeyKeyResponseList);
        for (ValuesPicker ele : checkPKeyValueList) {
            if (checkRegexAndNotification(ele, pKeyValue)) {
                event.preventCommit();
                return;
            }
        }
        for (ValuesPicker ele : checkPKeyKeyList) {
            if (checkRegexAndNotification(ele, pKeyKey)) {
                event.preventCommit();
                return;
            }
        }
        checkPKeyValueList.addAll(checkPKeyKeyList);
        for (ValuesPicker ele : checkPKeyValueList) {
            if (checkKeyAndNotification(ele)) {
                event.preventCommit();
                return;
            }
        }

        List<ValuesPicker> checkJsonTypeList = new ArrayList<ValuesPicker>(Arrays.asList(vpAppendTypeResponseValuePicker, vpReplaceTypeResponseValuePicker, vpAddTypeResponseValuePicker));
        for (ValuesPicker ele : checkJsonTypeList) {
            if (checkRegexAndNotification(ele, pJsonType)) {
                event.preventCommit();
                return;
            }
        }
    }

    private void setRequestTransformerForUI() {
        if (isNewEntity) {
            return;
        }
        this.setRequestTransformerForUI(getEditedEntity().getRequestTransformer());
    }

    private void setRequestTransformerForUI(String content) {
        if (content == null) {
            content = getEditedEntity().getRequestTransformer();
        }
        if (!StringUtils.hasText(getEditedEntity().getRequestTransformer())) {
            return;
        }
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
            RequestTransformerTransformationsPlugin plugin = objectMapper.readValue(content, RequestTransformerTransformationsPlugin.class);
            RequestTransformerTransformationsPluginConfig configFromDB = plugin.getConfig();

            vpRemoveHeaderRequestValuePicker.setValue(configFromDB.getRemove() == null ? null : (configFromDB.getRemove().getHeaders() == null ? null : configFromDB.getRemove().getHeaders()));
            vpAddHeaderRequestValuePicker.setValue(configFromDB.getAdd() == null ? null : (configFromDB.getAdd().getHeaders() == null ? null : configFromDB.getAdd().getHeaders()));
            vpRenameHeaderRequestValuePicker.setValue(configFromDB.getRemove() == null ? null : (configFromDB.getRename().getHeaders() == null ? null : configFromDB.getRename().getHeaders()));
            vpReplaceHeaderRequestValuePicker.setValue(configFromDB.getReplace() == null ? null : (configFromDB.getReplace().getHeaders() == null ? null : configFromDB.getReplace().getHeaders()));
            vpAppendHeaderRequestValuePicker.setValue(configFromDB.getAppend() == null ? null : (configFromDB.getAppend().getHeaders() == null ? null : configFromDB.getAppend().getHeaders()));

            vpRemoveBodyRequestValuePicker.setValue(configFromDB.getRemove() == null ? null : (configFromDB.getRemove().getBody() == null ? null : configFromDB.getRemove().getBody()));
            vpAddBodyRequestValuePicker.setValue(configFromDB.getAdd() == null ? null : (configFromDB.getAdd().getBody() == null ? null : configFromDB.getAdd().getBody()));
            vpRenameBodyRequestValuePicker.setValue(configFromDB.getRemove() == null ? null : (configFromDB.getRename().getBody() == null ? null : configFromDB.getRename().getBody()));
            vpReplaceBodyRequestValuePicker.setValue(configFromDB.getReplace() == null ? null : (configFromDB.getReplace().getBody() == null ? null : configFromDB.getReplace().getBody()));
            vpAppendBodyRequestValuePicker.setValue(configFromDB.getAppend() == null ? null : (configFromDB.getAppend().getBody() == null ? null : configFromDB.getAppend().getBody()));

            vpRemoveQueryRequestValuePicker.setValue(configFromDB.getRemove() == null ? null : (configFromDB.getRemove().getQuerystring() == null ? null : configFromDB.getRemove().getQuerystring()));
            vpAddQueryRequestValuePicker.setValue(configFromDB.getAdd() == null ? null : (configFromDB.getAdd().getQuerystring() == null ? null : configFromDB.getAdd().getQuerystring()));
            vpRenameQueryRequestValuePicker.setValue(configFromDB.getRemove() == null ? null : (configFromDB.getRename().getQuerystring() == null ? null : configFromDB.getRename().getQuerystring()));
            vpReplaceQueryRequestValuePicker.setValue(configFromDB.getReplace() == null ? null : (configFromDB.getReplace().getQuerystring() == null ? null : configFromDB.getReplace().getQuerystring()));
            vpAppendQueryRequestValuePicker.setValue(configFromDB.getAppend() == null ? null : (configFromDB.getAppend().getQuerystring() == null ? null : configFromDB.getAppend().getQuerystring()));
        } catch (Exception e) {
            notifications.create(Notifications.NotificationType.WARNING).withCaption("Can't Set Request Transformer: " + e.getMessage()).show();
        }

    }

    private void setResponseTransformerForUI() {
        if (isNewEntity) {
            return;
        }
        this.setResponseTransformerForUI(getEditedEntity().getResponseTransformer());
    }


    private void setResponseTransformerForUI(String content) {
        if (content == null) {
            content = getEditedEntity().getResponseTransformer();
        }
        if (!StringUtils.hasText(content)) {
            return;
        }
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
            ResponseTransformerTransformationsPlugin plugin = objectMapper.readValue(content, ResponseTransformerTransformationsPlugin.class);
            ResponseTransformerTransformationsPluginConfig configFromDB = plugin.getConfig();

            vpRemoveHeaderResponseValuePicker.setValue(configFromDB.getResponseRemove() == null ? null : (configFromDB.getResponseRemove().getHeaders() == null ? null : configFromDB.getResponseRemove().getHeaders()));
            vpAddHeaderResponseValuePicker.setValue(configFromDB.getResponseAdd() == null ? null : (configFromDB.getResponseAdd().getHeaders() == null ? null : configFromDB.getResponseAdd().getHeaders()));
            vpRenameHeaderResponseValuePicker.setValue(configFromDB.getResponseRename() == null ? null : (configFromDB.getResponseRename().getHeaders() == null ? null : configFromDB.getResponseRename().getHeaders()));
            vpReplaceHeaderResponseValuePicker.setValue(configFromDB.getResponseReplace() == null ? null : (configFromDB.getResponseReplace().getHeaders() == null ? null : configFromDB.getResponseReplace().getHeaders()));
            vpAppendHeaderResponseValuePicker.setValue(configFromDB.getResponseAdd() == null ? null : (configFromDB.getResponseAppend().getHeaders() == null ? null : configFromDB.getResponseAppend().getHeaders()));


            vpRemoveJsonResponseValuePicker.setValue(configFromDB.getResponseRemove() == null ? null : (configFromDB.getResponseRemove().getJson() == null ? null : configFromDB.getResponseRemove().getJson()));
            vpAddJsonResponseValuePicker.setValue(configFromDB.getResponseAdd() == null ? null : (configFromDB.getResponseAdd().getJson() == null ? null : configFromDB.getResponseAdd().getJson()));
            vpReplaceJsonResponseValuePicker.setValue(configFromDB.getResponseReplace() == null ? null : (configFromDB.getResponseReplace().getJson() == null ? null : configFromDB.getResponseReplace().getJson()));
            vpAppendJsonResponseValuePicker.setValue(configFromDB.getResponseAdd() == null ? null : (configFromDB.getResponseAppend().getJson() == null ? null : configFromDB.getResponseAppend().getJson()));

//            vpAddTypeResponseValuePicker.setValue(configFromDB.getResponseAdd() == null ? null : (configFromDB.getResponseAdd().getJson_types() == null ? null : configFromDB.getResponseAdd().getJson_types()));
//            vpReplaceTypeResponseValuePicker.setValue(configFromDB.getResponseReplace() == null ? null : (configFromDB.getResponseReplace().getJson_types() == null ? null : configFromDB.getResponseReplace().getJson_types()));
//            vpAppendTypeResponseValuePicker.setValue(configFromDB.getResponseAdd() == null ? null : (configFromDB.getResponseAppend().getJson_types() == null ? null : configFromDB.getResponseAppend().getJson_types()));
        } catch (Exception e) {
            notifications.create(Notifications.NotificationType.WARNING).withCaption("Can't Set Request Transformer: " + e.getMessage()).show();
        }

    }

    private int doTargetSecurity() {
        //Target security
        if (target_securityField.getValue() == null || !request_transformerCheckBox.getValue()) {
            getEditedEntity().setUpstream_header_name(null);
            getEditedEntity().setUpstream_header_value(null);
            return 0;
        }
        String targetSecurity = target_securityField.getValue().toString();
        if (!StringUtils.hasText(targetSecurity)) {
            getEditedEntity().setUpstream_header_name(null);
            getEditedEntity().setUpstream_header_value(null);
            return 0;
        }
        if (targetSecurity.equals("Basic")) {
            String targetUser = target_userField.getRawValue();
            String targetPasswd = target_passwordField.getRawValue();
            int retInt = 0;
            if (!StringUtils.hasText(targetUser) || !StringUtils.hasText(targetPasswd)) {
                retInt = -1;
                notifications.create(Notifications.NotificationType.WARNING).withCaption("Please enter Target user, Target password").show();
            } else {
                getEditedEntity().setUpstream_header_name("Authorization");
                getEditedEntity().setUpstream_header_value("Basic " + Utility.encodeBase64(targetUser + ":" + targetPasswd));
                Utility.addElementToValuesPicker(vpRemoveHeaderRequestValuePicker, "Authorization");
                Utility.addElementToValuesPicker(vpAddHeaderRequestValuePicker, "Authorization:" + getEditedEntity().getUpstream_header_value());
            }
            return retInt;
        }
        if (targetSecurity.equals("API key")) {
            String targetKeyName = target_apiKeyNameField.getRawValue();
            String targetKeyValue = target_apiKeyValueField.getRawValue();
            int retInt = 0;
            if ((!StringUtils.hasText(targetKeyName)) || (!StringUtils.hasText(targetKeyValue))) {
                retInt = -1;
                notifications.create(Notifications.NotificationType.WARNING).withCaption("Please enter Target API key name & value").show();
            } else {
                getEditedEntity().setUpstream_header_name(target_apiKeyNameField.getValue().toString());
                getEditedEntity().setUpstream_header_value(target_apiKeyValueField.getValue().toString());
                retInt = 0;
                Utility.addElementToValuesPicker(vpRemoveHeaderRequestValuePicker, targetKeyName);
                Utility.addElementToValuesPicker(vpAddHeaderRequestValuePicker, targetKeyName + ":" + targetKeyValue);
            }
            return retInt;
        }
        if (targetSecurity.equals("JWT")) {
            String targetKeyName = "Authorization";
            String targetKeyValue = target_jwtValueField.getRawValue();
            if (targetKeyValue == null || targetKeyValue.equals("")) {
                notifications.create(Notifications.NotificationType.WARNING).withCaption("Please enter Target JWT token").show();
                return -1;
            } else {
                getEditedEntity().setUpstream_header_name(targetKeyName);
                getEditedEntity().setUpstream_header_value(targetKeyValue);
                Utility.addElementToValuesPicker(vpRemoveHeaderRequestValuePicker, targetKeyName);
                Utility.addElementToValuesPicker(vpAddHeaderRequestValuePicker, targetKeyName + ":" + targetKeyValue);
            }
            return 0;
        }
        getEditedEntity().setUpstream_header_name(null);
        getEditedEntity().setUpstream_header_value(null);
        return 0;
    }

    @Autowired
    private CheckBox response_transformerCheckBox;
    @Autowired
    private CheckBox corsCheckBox;
    @Autowired
    private CheckBox request_transformerCheckBox;
    @Autowired
    private GroupBoxLayout response_transformer_group;
    @Autowired
    private GroupBoxLayout request_transformer_group;


    @Subscribe("response_transformerCheckBox")
    public void onResponse_transformerCheckBoxValueChange(HasValue.ValueChangeEvent<Boolean> event) {
        response_transformer_group.setVisible(event.getValue());
    }

    @Subscribe("request_transformerCheckBox")
    public void onRequest_transformerCheckBoxValueChange(HasValue.ValueChangeEvent<Boolean> event) {
        request_transformer_group.setVisible(event.getValue());
        target_securityField.setVisible(event.getValue());
        target_securityField.setValue(null);
    }


    @Subscribe("corsCheckBox")
    public void onCorsCheckBoxValueChange(HasValue.ValueChangeEvent<Boolean> event) {
        corsGroupBox.setVisible(event.getValue());
    }

    @Autowired
    private GroupBoxLayout corsGroupBox;
    @Autowired
    private ValuesPicker corsOriginValuePicker;
    @Autowired
    private ValuesPicker corsMethodValuePicker;
    @Autowired
    private ValuesPicker corsExposeHeaderValuePicker;
    @Autowired
    private TextField<Integer> corsMaxAgeField;
    @Autowired
    private CheckBox corsCredentialCheckBox;
    @Autowired
    private ValuesPicker corsAccessControlAllowHeaderValuePicker;

    void setValueForCorsUI() {
        String content = getEditedEntity().getCors();
        try {
            if (StringUtils.hasText(content)) {
                ObjectMapper objectMapper = new ObjectMapper();
                objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
                CorsPlugin plugin = objectMapper.readValue(content, CorsPlugin.class);
                CorsPluginConfig configFromDB = plugin.getConfig();
                corsOriginValuePicker.setValue(configFromDB.getOrigins());
                corsAccessControlAllowHeaderValuePicker.setValue(configFromDB.getHeaders());
                corsExposeHeaderValuePicker.setValue(configFromDB.getExposed_headers());
                corsMaxAgeField.setValue(configFromDB.getMax_age());
                corsCredentialCheckBox.setValue(configFromDB.getCredentials());
            }
        } catch (Exception ex) {
            notifications.create(Notifications.NotificationType.WARNING).withCaption("cannot set UI value for CORS").show();
        }
    }

    void getValueFromCorsUI() {
        try {
            if (corsCheckBox.isChecked()) {
                List<String> origin = Utility.vpToLst(corsOriginValuePicker);
                List<String> headers = Utility.vpToLst(corsAccessControlAllowHeaderValuePicker);
                List<String> exposeHeaders = Utility.vpToLst(corsExposeHeaderValuePicker);
                Integer maxAge = corsMaxAgeField.getValue();
                boolean credential = corsCredentialCheckBox.getValue();

                CorsPluginConfig config = new CorsPluginConfig(new ArrayList<String>(origin), new ArrayList<String>(exposeHeaders), new ArrayList<String>(headers), maxAge, credential);
                CorsPlugin corsPlugin = new CorsPlugin(config);
                ObjectMapper objectMapper = new ObjectMapper();
                objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
                getEditedEntity().setCors(objectMapper.writeValueAsString(corsPlugin));
            } else {
                getEditedEntity().setCors(null);
            }
        } catch (Exception ex) {
            notifications.create(Notifications.NotificationType.WARNING).withCaption("cannot get value for CORS").show();
        }

    }

    // Raw view
    @Autowired
    private ScreenBuilders screenBuilders;

    private void openOtherScreen(KongPluginType type) {
        IdeScreen screen = screenBuilders.screen(this).withScreenClass(IdeScreen.class).withOpenMode(OpenMode.DIALOG).withAfterCloseListener(afterCloseEvent -> {
            IdeScreen otherScreen = afterCloseEvent.getSource();
            if (afterCloseEvent.closedWith(StandardOutcome.COMMIT)) {
                String result = otherScreen.getResult();
//                        notifications.create().withCaption("Result: " + result).show();
                if (type.equals(KongPluginType.REQUEST_TRANSFORMER)) {
                    setRequestTransformerForUI(result);
                    loadRequestTransformerDataFromUI();
                }
                if (type.equals(KongPluginType.RESPONSE_TRANSFORMER)) {
                    setResponseTransformerForUI(result);
                    loadResponseTransformerDataFromUI();
                }
            }
        }).build();
        screen.setContent(getEditedEntity(), type);
        screen.show();
    }

    @Subscribe("btnRequestRawView")
    public void onBtnRequestRawViewClick(Button.ClickEvent event) {
        loadRequestTransformerDataFromUI();
        openOtherScreen(KongPluginType.REQUEST_TRANSFORMER);
    }

    @Subscribe("btnResponseRawView")
    public void onBtnResponseRawViewClick(Button.ClickEvent event) {
        loadResponseTransformerDataFromUI();
        openOtherScreen(KongPluginType.RESPONSE_TRANSFORMER);
    }

    @Subscribe("testEndpointBtn")
    public void onTestEndpointBtnClick(Button.ClickEvent event) {
        ApiCallEdit apiCallEdit = screens.create(ApiCallEdit.class);
        ApiCall apiCall = new ApiCall();
        apiCall.setEndpoint(target_endpointField.getRawValue());
        apiCallEdit.setEntityToEdit(apiCall);
        apiCallEdit.setApiEndpoint(target_endpointField.getRawValue());
        apiCallEdit.show();
    }

}