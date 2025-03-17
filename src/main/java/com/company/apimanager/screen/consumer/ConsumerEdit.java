package com.company.apimanager.screen.consumer;

import com.company.apimanager.app.*;
import com.company.apimanager.app.kong_object.KongConsumerGroup;
import com.company.apimanager.app.kong_object.KongConsumerGroupRatelimit;
import com.company.apimanager.screen.publishedproduct.HandledError;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.jmix.core.DataManager;
import io.jmix.core.EntityStates;
import com.company.apimanager.entity.*;
import io.jmix.core.security.CurrentAuthentication;
import io.jmix.ui.Notifications;
import io.jmix.ui.component.*;
import io.jmix.ui.screen.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Objects;

@UiController("Consumer.edit")
@UiDescriptor("consumer-edit.xml")
@EditedEntityContainer("consumerDc")
public class ConsumerEdit extends StandardEditor<Consumer> {
    @Autowired
    private CurrentAuthentication currentAuthentication;
    @Autowired
    private Notifications notifications;
    @Autowired
    private EntityStates entityStates;
    @Autowired
    private TextField<String> nameField;
    @Autowired
    private TextField<Long> max_per_hourField;
    @Autowired
    private TextField<String> api_keyField;
    @Autowired
    private CheckBox api_keyUsedField;
    @Autowired
    private TextField<Long> max_per_secondField;
    @Autowired
    private TextField<Long> max_per_minuteField;
    @Autowired
    private PasswordField passwordField;
    @Autowired
    private PasswordField confirmPasswordField;

    private String providerPlan;
    @Autowired
    private EntityPicker<Site> siteField;
    @Autowired
    private DataManager dataManager;
    @Autowired
    private TextField<String> oauthClientIdField;
    @Autowired
    private TextField<String> oauthClientNameField;
    @Autowired
    private TextField<String> oauthClientSecretField;
    @Autowired
    private TextField<String> oauthRedirectUriField;
    @Autowired
    private TextField basicAuthenUser;

    @Autowired
    private ConfigurationProperties config;
    @Autowired
    private TextField<String> titleField;

    @Subscribe
    public void onInit(InitEvent event) {
        User currUser = (User) currentAuthentication.getUser();
        providerPlan = currUser.getProviderPlan();
        if (providerPlan == null) {
            providerPlan = "Pilot";
        }
    }

    @Subscribe
    public void onAfterShow(AfterShowEvent event) {
        //Pricing plan
        User currUser = (User) currentAuthentication.getUser();
        PricingPlan currPlan = currUser.getPricingPlan();
        if (currPlan == null) {
            notifications.create(Notifications.NotificationType.WARNING).
                    withCaption("You have no pricing plan. Please contact Cloud administrator").show();
            nameField.setEditable(false);
            siteField.setEnabled(false);
            passwordField.setEnabled(false);
            confirmPasswordField.setEnabled(false);
            api_keyField.setEnabled(false);
            basicAuthenUser.setEnabled(false);
            max_per_secondField.setEnabled(false);
            max_per_minuteField.setEnabled(false);
            max_per_hourField.setEnabled(false);
            titleField.setEnabled(false);
            return;
        }


        if (!entityStates.isNew(getEditedEntity())) {
            nameField.setEditable(false);
            siteField.setEnabled(false);
            //api_keyUsedField.setEnabled(false);
            basicAuthenUser.setVisible(true);
            api_keyUsedField.setVisible(false);
            api_keyField.setEditable(false);
            passwordField.setValue(getEditedEntity().getPassword());
            confirmPasswordField.setValue(getEditedEntity().getPassword());

            basicAuthenUser.setValue(currentAuthentication.getUser().getUsername().toLowerCase() +
                    "." + getEditedEntity().getSite().getName().toLowerCase() + "."
                    + getEditedEntity().getName().toLowerCase());

            if (getEditedEntity().getKong_oauth_id() == null) {
                oauthClientIdField.setVisible(false);
                oauthClientNameField.setVisible(false);
                oauthClientSecretField.setVisible(false);
                oauthRedirectUriField.setVisible(false);
            }
            else {
                oauthClientIdField.setVisible(true);
                oauthClientNameField.setVisible(true);
                oauthClientSecretField.setVisible(true);
                oauthRedirectUriField.setVisible(true);
            }
        }
        else {
            oauthClientIdField.setVisible(false);
            oauthClientNameField.setVisible(false);
            oauthClientSecretField.setVisible(false);
            oauthRedirectUriField.setVisible(false);
            basicAuthenUser.setVisible(false);

            if (!providerPlan.equals("Enterprise")) {
                siteField.setVisible(false);
                if (providerPlan.equals("Pilot")) {
                    max_per_secondField.setEditable(false);
                    max_per_secondField.setValue((long) 1);

                    max_per_minuteField.setEditable(false);
                    max_per_minuteField.setValue((long) 2);

                    max_per_hourField.setEditable(false);
                    max_per_hourField.setValue((long) 12);
                }
                else {
                    max_per_secondField.setValue((long) 1);
                    max_per_minuteField.setValue((long) 30);
                    max_per_hourField.setValue((long) 1000);
                }
            }
            else {
                max_per_secondField.setValue((long) 1);
                max_per_minuteField.setValue((long) 30);
                max_per_hourField.setValue((long) 1000);
            }
            api_keyField.setVisible(false);
        }
        passwordField.setValue("");
        confirmPasswordField.setValue("");
    }

    private Site getDefaultSite(HandledError handledError) {
        Site rsSite = null;
        try {
            List<Site> siteList = dataManager.load(Site.class).query("select e from Site e where e.owner = :owner")
                    .parameter("owner", (User) currentAuthentication.getUser()).list();
            if (siteList == null) {
                rsSite = null;
                handledError.setErrorMsg("Could not get default site");
            }
            else {
                if (siteList.size() == 0) {
                    rsSite = null;
                    handledError.setErrorMsg("Could not get default site");
                }
                else {
                    rsSite = siteList.get(0);
                }
            }
        }
        catch (Exception e) {
            rsSite = null;
            handledError.setErrorMsg(e.getMessage());
        }
        return rsSite;
    }


    @Subscribe("max_per_minuteField")
    public void onMax_per_minuteFieldValueChange(HasValue.ValueChangeEvent<Long> event) {
        User currUser = (User) currentAuthentication.getUser();
        PricingPlan pricingPlan = currUser.getPricingPlan();
        if (pricingPlan == null) {
            notifications.create(Notifications.NotificationType.WARNING).
                    withCaption("You have no pricing plan").show();
            max_per_minuteField.setEnabled(false);
            return;
        }

        long rateLimitPlan = (long) pricingPlan.getRate_limit();

        if ((max_per_minuteField.getValue() == null)) {
            max_per_minuteField.setValue((long) 30);
            return;
        }

        long maxPerMinute = max_per_minuteField.getValue();
        if (maxPerMinute <= 0) {
            notifications.create(Notifications.NotificationType.WARNING).
                    withCaption("Rate less than or equal zero. Set to default").show();
            max_per_minuteField.setValue((long) 30);
        }
        if (maxPerMinute > rateLimitPlan * 60) {
            notifications.create(Notifications.NotificationType.WARNING).
                    withCaption("Rate too much. Set to max value").show();
            max_per_minuteField.setValue(rateLimitPlan * 60);
        }
    }

    @Subscribe("max_per_secondField")
    public void onMax_per_secondFieldValueChange(HasValue.ValueChangeEvent<Long> event) {

        User currUser = (User) currentAuthentication.getUser();
        PricingPlan pricingPlan = currUser.getPricingPlan();
        if (pricingPlan == null) {
            notifications.create(Notifications.NotificationType.WARNING).
                    withCaption("You have no pricing plan").show();
            max_per_secondField.setEnabled(false);
            return;
        }

        if ((max_per_secondField.getValue() == null)) {
            max_per_secondField.setValue((long) 1);
            return;
        }

        long maxPerSecond = max_per_secondField.getValue();
        if (maxPerSecond <= 0) {
            notifications.create(Notifications.NotificationType.WARNING).
                    withCaption("Rate less than or equal Zero. Set to default").show();
            max_per_secondField.setValue((long) 1);
            return;
        }

        long rateLimitPlan = (long) pricingPlan.getRate_limit();
        if (maxPerSecond > rateLimitPlan) {
            notifications.create(Notifications.NotificationType.WARNING).

                    withCaption("Rate greater than your pricing plan. Set to your plan").show();

            max_per_secondField.setValue(rateLimitPlan);
        }
    }

    @Subscribe("max_per_hourField")
    public void onMax_per_hourFieldValueChange(HasValue.ValueChangeEvent<Long> event) {

        User currUser = (User) currentAuthentication.getUser();
        PricingPlan pricingPlan = currUser.getPricingPlan();
        if (pricingPlan == null) {
            notifications.create(Notifications.NotificationType.WARNING).
                    withCaption("You have no pricing plan").show();
            max_per_hourField.setEnabled(false);
            return;
        }

        long rateLimitPlan = (long) pricingPlan.getRate_limit();

        if ((max_per_hourField.getValue() == null)) {
            max_per_hourField.setValue((long) 1000);
            return;
        }

        long maxPerHour = max_per_hourField.getValue();
        if (maxPerHour <= 0) {
            notifications.create(Notifications.NotificationType.WARNING).
                    withCaption("Rate less than or equal Zero. Set to default").show();
            max_per_hourField.setValue((long) 1000);
            return;
        }

        if (maxPerHour > rateLimitPlan * 3600) {
            notifications.create(Notifications.NotificationType.WARNING).
                    withCaption("Rate too much. Set to max value").show();
            maxPerHour = config.rateMaxPerHour;
            max_per_hourField.setValue(rateLimitPlan * 3600);
        }
    }

    private long getMaxPerMinute() {
        long rateRs = 0;
        try {
            rateRs = max_per_minuteField.getValue();
        }
        catch (Exception e) {
            rateRs = 0;
        }
        return rateRs;
    }

    @Subscribe
    public void onBeforeCommitChanges(BeforeCommitChangesEvent event) {
        if (Utility.findSpecialChar(getEditedEntity().getName()) != 0) {
            notifications.create(Notifications.NotificationType.WARNING).
                    withCaption("Name must not contain special characters").show();
            event.preventCommit();
            return;
        }

        if (!Objects.equals(passwordField.getValue(), confirmPasswordField.getValue())) {
            notifications.create(Notifications.NotificationType.WARNING)
                    .withCaption("Password do not match").show();
            event.preventCommit();
            return;
        }
        long maxPerSecond, maxPerMinute, maxperHour;
        String apiKey, password;
        if( (max_per_hourField.getValue() == null) && (max_per_minuteField.getValue() == null)
                && (max_per_secondField.getValue() == null)) {
            notifications.create(Notifications.NotificationType.WARNING).
                    withCaption("Please enter rate limiting").show();
            event.preventCommit();
            return;
        }
        if (max_per_hourField.getValue() == null) {
            maxperHour = 1000;
            max_per_hourField.setValue((long) 1000);
        }
        else {
            maxperHour = max_per_hourField.getValue();
            if (maxperHour <= 0) {
                max_per_hourField.setValue((long) 1000);
            }
        }
        if (max_per_minuteField.getValue() == null) {
            maxPerMinute = 30;
            max_per_minuteField.setValue((long) 30);
        }
        else {
            maxPerMinute = max_per_minuteField.getValue();
            if (maxPerMinute <= 0) {
                max_per_minuteField.setValue((long) 30);
            }
        }
        if (max_per_secondField.getValue() == null) {
            maxPerSecond = 1;
            max_per_secondField.setValue((long) 1);
        }
        else {
            maxPerSecond = max_per_secondField.getValue();
            if (maxPerSecond <= 0) {
                max_per_secondField.setValue((long) 1);
            }
        }

        /*
        if (api_keyField.getValue() == null) {
            apiKey = "";
        } else {
            apiKey = api_keyField.getValue();
        }
        */

        if (passwordField.getValue() == null) {
            event.preventCommit();
            return;
        }
        password = passwordField.getValue();
//        if (password.isEmpty()) {
//            event.preventCommit();
//            return;
//        }

        if ((maxPerSecond > maxPerMinute) || (maxPerSecond > maxperHour) || (maxPerMinute > maxperHour)) {
                notifications.create(Notifications.NotificationType.ERROR).
                        withCaption("Rate per hour must be greater than per minute greater than per second").show();
            event.preventCommit();
            return;
        }

        HandledError siteHandledError = new HandledError();

        Site defaultSite = null;
        if (!providerPlan.equals("Enterprise")) {
            defaultSite = this.getDefaultSite(siteHandledError);
            if (defaultSite == null) {
                notifications.create(Notifications.NotificationType.WARNING)
                        .withCaption("There is no Public site for the Provider").show();
                event.preventCommit();
                return;
            }
        }

        try {
            PasswordEncoder encoder = EncodePassword.setPasswordEncoder();
            KongRateLimitInfo kongRateLimitInfo = new KongRateLimitInfo(maxPerSecond, maxPerMinute, maxperHour);
            HandledError handledError = new HandledError();
            Site publishSite;
            GatewayService gatewayService;
            if (providerPlan.equals("Enterprise")) {
                publishSite = getEditedEntity().getSite();
            }
            else {
                publishSite = defaultSite;
            }
            //gatewayService = publishSite.getGateway();
            KongConnector kongConnector = new KongConnector(publishSite.getDefaultManagementEndpoint());
            if (!entityStates.isNew(getEditedEntity())) {
                getEditedEntity().getKong_rate_limit_id();
                String kongConsumerName = getEditedEntity().getProvider_site_consumer_name();
                int retInt = kongConnector.updateRateLimit(kongConsumerName,
                        getEditedEntity().getKong_rate_limit_id(), kongRateLimitInfo, handledError);
                if (retInt != 200) {
                    notifications.create(Notifications.NotificationType.ERROR).
                            withCaption(handledError.getErrorMsg()).show();
                    event.preventCommit();
                }
                else {

                    if (!password.isBlank()) {
                        getEditedEntity().setPassword(encoder.encode(password));
                        int retIntPass = kongConnector.updateCredentialConsumer(kongConsumerName, getEditedEntity().getBase_credential_id(),
                                kongConsumerName, password, handledError);
                        if (retIntPass != 200) {
                            notifications.create(Notifications.NotificationType.ERROR).
                                    withCaption("Cloud not update password on Gateway: " + handledError.getErrorMsg()).show();
                            event.preventCommit();
                        }
                    }
                }

                /*
                if (!apiKey.isEmpty()) {
                    getEditedEntity().setApi_key(apiKey);
                    kongConnector.updateApiKeyConsumer(kongConsumerName, getEditedEntity().getKey_credential_id(), apiKey, handledError);
                }
                 */
            }
            else {
                getEditedEntity().setProvider((User) currentAuthentication.getUser());
                String kongConsumerName = getEditedEntity().getProvider().getUsername().toLowerCase()
                        + "." + publishSite.getName().toLowerCase() + "." + getEditedEntity().getName().toLowerCase();
                getEditedEntity().setProvider_site_consumer_name(kongConsumerName);

                KongConsumerDetails kongConsumerDetails = new KongConsumerDetails(kongConsumerName);
                HandledError consumerHandledError = new HandledError();
                int createConsumerInt = kongConnector.createConsumer(kongConsumerName, kongConsumerDetails,
                        consumerHandledError);

                if ((createConsumerInt != 201) && (createConsumerInt != 409)) {
                    notifications.create(Notifications.NotificationType.ERROR).
                            withCaption(consumerHandledError.getErrorMsg()).show();
                    event.preventCommit();
                    //201: tạo thành công, 409: đã có consumer từ trước
                }
                else {
                    if (createConsumerInt == 409)  { // đã có consumer
                        List<Consumer> consumerList = dataManager.load(Consumer.class).query
                                        ("select e from Consumer e where e.name = :name and e.provider = :user and e.site = :site")
                                .parameter("user", (User) currentAuthentication.getUser())
                                .parameter("name", getEditedEntity().getName())
                                .parameter("site", publishSite).list();
                        boolean consumerFound = true;
                        if (consumerList == null) {
                            consumerFound = false;
                        }
                        else {
                            if (consumerList.size() == 0) {
                                consumerFound = false;
                            }
                        }
                        if (consumerFound) {
                            notifications.create(Notifications.NotificationType.ERROR).
                                    withCaption("The consumer with that name already exist").show();
                            event.preventCommit();
                            return;
                        }
                    }

                    if (kongConnector.getConsumerByName(kongConsumerName, kongConsumerDetails, handledError) != 200) {
                        notifications.create(Notifications.NotificationType.ERROR).
                                withCaption("Could not get existing consumer: " + handledError.getErrorMsg()).show();
                        event.preventCommit();
                        return;
                    }
                    KongRateLimitDetails kongRateLimitDetails = new KongRateLimitDetails();
                    boolean createBasic = true;

                    if (password.isBlank()) {
                        notifications.create(Notifications.NotificationType.ERROR).
                                withCaption("Password cannot empty").show();
                        event.preventCommit();
                        return;
                    }

                    getEditedEntity().setPassword(encoder.encode(password));
                    StringBuilder idStr = new StringBuilder();
                    int createBasicRet = kongConnector.createOrUpdateBasicCredentialForConsumer
                            (kongConsumerName, kongConsumerName, password, idStr, handledError);
                    if (createBasicRet != 0) {
                        notifications.create(Notifications.NotificationType.ERROR).
                                withCaption("Could not create Basic credential: " + kongConsumerName
                                        + ": " + handledError.getErrorMsg()).show();
                        createBasic = false;
                    }
                    else {
                        getEditedEntity().setBaseCredentialId(idStr.toString());
                    }

                    if (createBasic) {
                        boolean createKey = true;
                        if (api_keyUsedField.getValue()) {
                            String consumerApiKey = kongConsumerName + "_" + kongConsumerDetails.getId();
                            getEditedEntity().setApi_key(consumerApiKey);
                            /*String keyResult = kongConnector.createApiKeyForConsumer(kongConsumerName, consumerApiKey,
                                    handledError);

                             */
                            StringBuilder keyId = new StringBuilder();
                            int createKeyRet = kongConnector.createOrUpdateApiKeyForConsumer(kongConsumerName, consumerApiKey,
                                                keyId, handledError);
                            if (createKeyRet != 0) {
                                notifications.create(Notifications.NotificationType.ERROR).
                                        withCaption("Create Key: " +  handledError.getErrorMsg()).show();
                                createKey = false;
                            } else {
                                getEditedEntity().setKeyCredentialId(keyId.toString());
                            }
                        }
                        if (createKey) {
                            /*
                            int retInt = kongConnector.createRateLimit(kongConsumerName, kongRateLimitInfo,
                                    kongRateLimitDetails, handledError);
                             */
                            int rateLimitRet = kongConnector.createOrUpdateRateLimit(kongConsumerName, kongRateLimitInfo,
                                    kongRateLimitDetails, handledError);
                            if (rateLimitRet != 0) {
                                notifications.create(Notifications.NotificationType.ERROR).
                                        withCaption("Create Rate limit: " + handledError.getErrorMsg()).show();
                                event.preventCommit();
                            } else {
                                getEditedEntity().setKong_consumer_id(kongConsumerDetails.getId());
                                getEditedEntity().setKong_rate_limit_id(kongRateLimitDetails.getId());
                                getEditedEntity().setSite(publishSite);
                            }
                        }
                        else {
                            event.preventCommit();
                        }
                    }
                    else {
                        event.preventCommit();
                    }


                    // Create consumer_group, add rate limit on that group
                    // Only when consumer is new, this work must be done.

                    //Onpremise edition do not need consumer group
                    /*
                    KongConsumerGroupController consumerGroupController = new KongConsumerGroupController((publishSite.getDefaultManagementEndpoint()));
                    String groupName = getEditedEntity().getProvider().getUsername();
                    HandledError getConsumerGroupHandledError = new HandledError();
                    KongConsumerGroup group = consumerGroupController.getConsumerGroupByName(groupName, getConsumerGroupHandledError);
                    if (getConsumerGroupHandledError.getHttpResponseCode()!=404 && getConsumerGroupHandledError.getHttpResponseCode()!=200){ //404 group not exist
                        notifications.create(Notifications.NotificationType.ERROR).withCaption("Get Consumer Group: "+ getConsumerGroupHandledError.getErrorMsg()).show();
                        event.preventCommit();
                    }
                        // so create new group;
                    if (getConsumerGroupHandledError.getHttpResponseCode() == 404){ //group not exist => create new and set limit
                        HandledError createConsumerGroupHandledError = new HandledError();
                        group = consumerGroupController.createConsumerGroup(groupName, createConsumerGroupHandledError);
                        if (group==null){
                            notifications.create(Notifications.NotificationType.ERROR).withCaption("Create Consumer Group: "+ createConsumerGroupHandledError.getErrorMsg()).show();
                            event.preventCommit();
                        }

                        KongConsumerGroupRatelimit groupRatelimit = new KongConsumerGroupRatelimit(getEditedEntity().getProvider().getPricingPlan().getRate_limit());
                        HandledError setLimitOnGroupHandledError = new HandledError();
                        int setRateLimitResult = consumerGroupController.setRateLimitOnGroup(group, groupRatelimit, setLimitOnGroupHandledError);
                        if (setRateLimitResult!=201){
                            notifications.create(Notifications.NotificationType.ERROR).withCaption("Add Limit: "+ setLimitOnGroupHandledError.getErrorMsg()).show();
                            event.preventCommit();
                        }
                    }

                    if (getConsumerGroupHandledError.getHttpResponseCode() == 200){ //group existed => update ratelimit
                        KongConsumerGroupRatelimit groupRatelimit = new KongConsumerGroupRatelimit(getEditedEntity().getProvider().getPricingPlan().getRate_limit());
                        HandledError updateLimitOnGroupHandledError = new HandledError();
                        int updateRateLimitResult = consumerGroupController.updateRateLimitOnGroup(group, groupRatelimit, updateLimitOnGroupHandledError);
                        if (updateRateLimitResult!=200){
                            notifications.create(Notifications.NotificationType.ERROR).withCaption("Add Limit: "+ updateLimitOnGroupHandledError.getErrorMsg()).show();
                            event.preventCommit();
                        }
                    }

                    // add consumer to this group
                    HandledError addConsumerToGroupHandledError = new HandledError();
                    int addResult = consumerGroupController.addConsumerToGroup(group, kongConsumerDetails, addConsumerToGroupHandledError);
                    if (addResult!=201){
                        notifications.create(Notifications.NotificationType.ERROR).withCaption(addResult + " Add Consumer to Consumer Group: " + addConsumerToGroupHandledError.getErrorMsg()).show();
                        event.preventCommit();
                    }

                     */
                }
            }
        }
        catch (Exception e) {
            notifications.create(Notifications.NotificationType.ERROR).withCaption(e.getMessage()).show();
            event.preventCommit();
        }
    }

}