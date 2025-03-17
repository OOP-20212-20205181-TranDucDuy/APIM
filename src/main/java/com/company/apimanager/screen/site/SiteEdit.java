package com.company.apimanager.screen.site;

import com.company.apimanager.app.*;
import com.company.apimanager.app.kong_object.IpRestrictionSecurityPlugin;
import com.company.apimanager.app.kong_object.IpRestrictionSecurityPluginConfig;
import com.company.apimanager.app.kong_object.RequestTransformerTransformationsPlugin;
import com.company.apimanager.app.kong_object.RequestTransformerTransformationsPluginConfig;
import com.company.apimanager.app.kong_object.helper.*;
import com.company.apimanager.entity.*;
import com.company.apimanager.screen.login.LoginScreen;
import com.company.apimanager.screen.publishedproduct.HandledError;
import com.company.apimanager.screen.sitegateway.SiteGatewayBrowse;
import com.company.apimanager.screen.testiframe.TestIframeScreen;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jmix.core.DataManager;
import io.jmix.core.EntityStates;
import io.jmix.core.JmixModulesAwareBeanSelector;
import io.jmix.core.security.CurrentAuthentication;
import io.jmix.ui.Notifications;
import io.jmix.ui.Screens;
import io.jmix.ui.component.*;
import io.jmix.ui.model.CollectionLoader;
import io.jmix.ui.screen.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;


import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Map;

@UiController("Site.edit")
@UiDescriptor("site-edit.xml")
@EditedEntityContainer("siteDc")
public class SiteEdit extends StandardEditor<Site> {
    @Autowired
    private Notifications notifications;
    @Autowired
    private CollectionLoader<GatewayService> gatewayServicesDl;
    @Autowired
    private CollectionLoader<PortalService> portalServicesDl;
    @Autowired
    private EntityComboBox<GatewayService> gatewaySelector;
    @Autowired
    private EntityComboBox<PortalService> portalSelector;
    @Autowired
    private DataManager dataManager;
    @Autowired
    private CurrentAuthentication currentAuthentication;
    @Autowired
    private EntityStates entityStates;
    @Autowired
    private TextField<String> nameField;
    @Autowired
    private JmixModulesAwareBeanSelector jmixModulesAwareBeanSelector;
    @Autowired
    private EntityPicker<VirtualArea> virtual_areaField;
    @Autowired
    private Screens screens;
    @Autowired
    RabbitTemplate rabbitTemplate;

    private User siteOwner;

    //    @Autowired
//    private Button additionalGatewayBtn;
//    @Autowired
//    private TextField<String> oauth_providerField;
    @Autowired
    private TextField<String> titleField;
    @Autowired
    private CheckBox production_modeField;
    //    @Autowired
//    private TextField<String> portal_adminField;
    @Autowired
    private CollectionLoader<PortalService> portalServicesDl1;
    @Autowired
    private EntityComboBox<PortalService> portalSelector1;
    @Autowired
    private CollectionLoader<GatewayService> gatewayServicesDl1;
    @Autowired
    private EntityComboBox<GatewayService> gatewaySelector1;
    @Autowired
    private CollectionLoader<SelfHostedGateway> selfHostedGatewayServicesDl;
    @Autowired
    private CheckBox self_hosted_gateway_usedField;
    @Autowired
    private TextField<String> drupalApiField;

    @Autowired
    private TextField<String> managementEndpointField;

    @Autowired
    private EntityComboBox<SelfHostedGateway> selfHostedGatewaySelector;
    @Autowired
    private Utility utility;
    @Autowired
    private TextField<String> logoField;
    @Autowired
    private TextField<String> ldapCredentialField;
    @Autowired
    private TextField<String> ldapPrincipalField;
    @Autowired
    private TextField<String> ldapUrlField;
    @Autowired
    private CheckBox oauth_consent_page;

    @Autowired
    private ConfigurationProperties config;
    private final Logger log = LoggerFactory.getLogger(SiteEdit.class);

    private boolean isNewEntity;
    @Autowired
    private FileUploadField imageField;
    @Autowired
    private Link uploadedImageField;
    @Autowired
    private TextField<String> portalAboutPageContentField;
    @Autowired
    private TextField<String> portalHomePageContentField;
    @Autowired
    private TextField<String> portalHomePageTitleField;

    @Subscribe
    public void onInitEntity(InitEntityEvent<Site> event) {
        isNewEntity = true;
    }

    @Subscribe("virtual_areaField")
    public void onVirtual_areaFieldValueChange(HasValue.ValueChangeEvent<VirtualArea> event) {
        //notifications.create(Notifications.NotificationType.HUMANIZED).withCaption("changed").show();
        if (event.getValue() == null) {
            gatewaySelector.setValue(null);
            portalSelector.setValue(null);
        } else {
            gatewaySelector.setValue(null);
            gatewayServicesDl.setParameter("area", event.getValue());
            if (siteOwner != null) {
                //gatewayServicesDl.setParameter("user", siteOwner);
            } else {
                gatewayServicesDl.setParameter("user", (User) currentAuthentication.getUser());
            }
            gatewayServicesDl.load();

            portalSelector.setValue(null);
            portalServicesDl.setParameter("area", event.getValue());
            if (siteOwner != null) {
                gatewayServicesDl1.setParameter("area", event.getValue());
                gatewayServicesDl1.load();
                portalServicesDl1.setParameter("area", event.getValue());
                portalServicesDl1.load();
            } else {
                portalServicesDl.setParameter("user", (User) currentAuthentication.getUser());
            }
            portalServicesDl.load();
        }
    }

    public User getSiteOwner() {
        return siteOwner;
    }

    /*
    public void setSiteOwner(User siteOwner) {

        this.siteOwner = siteOwner;
        gatewayServicesDl.setParameter("user", siteOwner);
        portalServicesDl.setParameter("user", siteOwner);
    }
     */

    @Subscribe
    public void onInit(InitEvent event) {
        //gatewayServicesDl.setParameter("user", (User) currentAuthentication.getUser());
        //portalServicesDl.setParameter("user", (User) currentAuthentication.getUser());
    }

    @Subscribe
    public void onBeforeShow(BeforeShowEvent event) {
        if (entityStates.isNew(getEditedEntity())) {
            siteOwner = getEditedEntity().getOwner();
            drupalApiField.setVisible(false);
            managementEndpointField.setVisible(false);
            uploadedImageField.setVisible(false);

            if (siteOwner != null) { //goi tu Topo Admin

                getEditedEntity().setName("Public");
                getEditedEntity().setTitle("Public site");
                getEditedEntity().setProduction_mode(true);
                getEditedEntity().setOauth_provider("");
                getEditedEntity().setPortal_admin("admin");

//                oauth_providerField.setEditable(false);
                nameField.setEditable(false);
                titleField.setEditable(false);
                production_modeField.setEditable(false);

                portalSelector1.setVisible(true);
                gatewaySelector1.setVisible(true);
                gatewaySelector.setVisible(false);
                portalSelector.setVisible(false);

                selfHostedGatewaySelector.setVisible(false);
                self_hosted_gateway_usedField.setVisible(false);
                self_hosted_gateway_usedField.setVisible(false);
                imageField.setVisible(false);

            } else {
                gatewayServicesDl.setParameter("user", (User) currentAuthentication.getUser());
                portalServicesDl.setParameter("user", (User) currentAuthentication.getUser());
                self_hosted_gateway_usedField.setValue(false);
                selfHostedGatewayServicesDl.setParameter("user", (User) currentAuthentication.getUser());
                selfHostedGatewaySelector.setVisible(false);
                gatewaySelector.setVisible(true);
                gatewaySelector1.setVisible(false);
                portalSelector1.setVisible(false);
                portalSelector.setVisible(true);

                portalAboutPageContentField.setVisible(false);
                portalHomePageContentField.setVisible(false);
                portalHomePageTitleField.setVisible(false);
                uploadedImageField.setVisible(false);
                imageField.setVisible(false);

                if (!CompilerDirective.onCloud) {
                    selfHostedGatewaySelector.setVisible(false);
                    self_hosted_gateway_usedField.setVisible(false);
                    self_hosted_gateway_usedField.setValue(false);
                } else {
                    String usingSelfHostedGateway = utility.getAppConfigValue("usingSelfHostedGateway");
                    selfHostedGatewaySelector.setVisible(false);
                    self_hosted_gateway_usedField.setVisible(false);
                    self_hosted_gateway_usedField.setValue(false);
                    if (usingSelfHostedGateway != null) {
                        if (usingSelfHostedGateway.toLowerCase().equals("true")) {
                            self_hosted_gateway_usedField.setVisible(true);
                        }
                    }
                }
            }
//            additionalGatewayBtn.setVisible(false);
        } else {
            virtual_areaField.setEnabled(false);
            gatewaySelector.setEnabled(false);
            portalSelector.setEnabled(false);
//            portal_adminField.setEnabled(false);
            gatewaySelector1.setVisible(false);
            portalSelector1.setVisible(false);
            production_modeField.setEnabled(false);

            portalAboutPageContentField.setVisible(true);
            portalHomePageContentField.setVisible(true);
            portalHomePageTitleField.setVisible(true);
            uploadedImageField.setVisible(true);
            imageField.setVisible(true);

            siteOwner = getEditedEntity().getOwner();
            if (siteOwner == null){
                siteOwner = (User) currentAuthentication.getUser();
            }
            if (!siteOwner.getPricingPlan().getDeveloper_portal()){
                drupalApiField.setVisible(false);
            }
            /*uploadedImageField.setUrl(getEditedEntity().getDrupal_api() +
                    getEditedEntity().getPortal_about_page_image_url());

             */

            if (!CompilerDirective.onCloud) {
                selfHostedGatewaySelector.setVisible(false);
                self_hosted_gateway_usedField.setVisible(false);

            } else {
                if (getEditedEntity().getSelf_hosted_gateway_used()) {
                    selfHostedGatewaySelector.setVisible(true);
                    selfHostedGatewaySelector.setEnabled(false);
                    selfHostedGatewaySelector.setValue(getEditedEntity().getSelf_hosted_gateway());
                    gatewaySelector.setVisible(false);
                } else {
                    selfHostedGatewaySelector.setVisible(false);
                    gatewaySelector.setVisible(true);
                }
                self_hosted_gateway_usedField.setEnabled(false);
                //additionalGatewayBtn.setVisible(false);
            }
        }
    }

    @Subscribe("self_hosted_gateway_usedField")
    public void onSelf_hosted_gateway_usedFieldValueChange(HasValue.ValueChangeEvent<Boolean> event) {
        if (self_hosted_gateway_usedField.isChecked()) {
            selfHostedGatewaySelector.setVisible(true);
            gatewaySelector.setVisible(false);
        } else {
            selfHostedGatewaySelector.setVisible(false);
            gatewaySelector.setVisible(true);
        }
    }

    private int createDrupalSite(HandledError handledError) {
        int retInt = 0;
        boolean noPortalSite = false;
        if (getEditedEntity().getDrupal_api() == null) {
            noPortalSite = true;
        } else {
            if (getEditedEntity().getDrupal_api().isEmpty()) {
                noPortalSite = true;
            }
        }
        if (noPortalSite) {
            // Gửi yêu cầu tạo Site
//            User currUser = (User) currentAuthentication.getUser();
//            Map<String, Object> map = new HashMap<>();
//            map.put("user", currUser);
//            map.put("site", getEditedEntity());
//            rabbitTemplate.convertAndSend(config.siteQueueName, map);



            User currUser = (User) currentAuthentication.getUser();
            String url = utility.createDrupalSite(currUser.getUsername(), getEditedEntity().getName(),
                    getEditedEntity().getPortal().getManagement_endpoint(), handledError);
            if (!url.isEmpty()) {
                getEditedEntity().setDrupal_api(url);
                retInt = 0;
            } else {
                retInt = -1;
            }
            //getEditedEntity().setDrupal_api("http://localhost:81/drupal2");
        }
        return retInt;
    }

    @Subscribe
    public void onBeforeCommitChanges(BeforeCommitChangesEvent event) {
        String name = getEditedEntity().getName();

        if (Utility.findSpecialChar(name) != 0) {
            notifications.create(Notifications.NotificationType.WARNING).withCaption("Name must not contain special characters")
                    .show();
            event.preventCommit();
            return;
        }
        if ((gatewaySelector.getValue() == null) ||
                (portalSelector.getValue() == null)
                || (getEditedEntity().getVirtual_area() == null)) {
            notifications.create(Notifications.NotificationType.WARNING).
                    withCaption("Work zone or Gateway or Portal is NULL").show();
            event.preventCommit();
            return;
        }

        getEditedEntity().setGateway(gatewaySelector.getValue());
        getEditedEntity().setPortal(portalSelector.getValue());
        getEditedEntity().setSelf_hosted_gateway_used(false);
        getEditedEntity().setProduction_mode(production_modeField.getValue());
        getEditedEntity().setSelf_hosted_gateway(null);
        User currUser = (User) currentAuthentication.getUser();
        getEditedEntity().setOwner(currUser);
        getEditedEntity().setOwner_sitename(currUser.getUsername() + "_" + getEditedEntity().getName());
        getEditedEntity().setPortal_admin("portal_admin");
        HandledError handledError = new HandledError();
        if (entityStates.isNew(getEditedEntity())) {
            if (createDrupalSite(handledError) != 0) {
                notifications.create(Notifications.NotificationType.WARNING).
                        withCaption("Could not create Portal site: " + handledError.getErrorMsg()).show();
                event.preventCommit();
            }
        }
        else {
            if (getEditedEntity().getDrupal_api() != null) {
                DrupalConnector drupalConnector = new DrupalConnector(getEditedEntity().getDrupal_api(),
                        Site.DRUPAL_ADMIN_USER, Site.DRUPAL_ADMIN_PASSWORD);
                CallDrupalApiError callDrupalApiError = new CallDrupalApiError();
                if (this.updateAboutPage(drupalConnector, callDrupalApiError) != 0) {
                    notifications.create(Notifications.NotificationType.ERROR).
                            withCaption("Update About page failed: " + callDrupalApiError.getErrorMsg()
                            ).show();
                    event.preventCommit();
                }
                else {
                    if (this.updateHomePage(drupalConnector, callDrupalApiError) != 0) {
                        notifications.create(Notifications.NotificationType.ERROR).
                                withCaption("Update Home page failed: " + callDrupalApiError.getErrorMsg()
                                ).show();
                        event.preventCommit();
                    }
                }
            }
        }
    }

//    @Subscribe("additionalGatewayBtn")
//    public void onAdditionalGatewayBtnClick(Button.ClickEvent event) {
//        if (!entityStates.isNew(getEditedEntity())) {
//            SiteGatewayBrowse siteGatewayBrowse = screens.create(SiteGatewayBrowse.class);
//            siteGatewayBrowse.setSite(getEditedEntity());
//            siteGatewayBrowse.show();
//        }
//    }



    private boolean isImageEmpty() {
        if (imageField.getValue() == null) {
            return true;
        }
        if (imageField.getValue().length == 0) {
            return true;
        }
        return false;
    }

    private int updateImage(DrupalConnector drupalConnector, CallDrupalApiError error) {
        DrupalUploadedFile uploadedFile = new DrupalUploadedFile();

        int retInt = 0;
        try {
            if (!this.isImageEmpty()) {
                if (drupalConnector.uploadFile(imageField.getValue(), imageField.getFileName(), uploadedFile
                        , error) != HttpURLConnection.HTTP_OK) {
                    error.setErrorMsg("update image failed: " + error.getErrorMsg());
                    retInt = -1;
                }
                else {
                    getEditedEntity().setPortal_about_page_image_id(uploadedFile.getfId());
                    getEditedEntity().setPortal_about_page_image_url(uploadedFile.getUrl());

                }
            }
        }
        catch (Exception e) {
            retInt = -2;

        }
        return retInt;
    }

    private int updateAboutPage(DrupalConnector drupalConnector, CallDrupalApiError error) {
        int retInt = 0;

        if (updateImage(drupalConnector, error) != 0) {
            return -1;
        }
        if (drupalConnector.updateAboutPage(getEditedEntity().getPortal_about_page_content()
                ,getEditedEntity().getPortal_about_page_image_id(), error) != HttpURLConnection.HTTP_OK) {
            retInt = -2;

        }

        return retInt;
    }

    private int updateHomePage(DrupalConnector drupalConnector, CallDrupalApiError error) {
        int retInt = 0;

        if (drupalConnector.updateHomePage(getEditedEntity().getPortal_home_page_content()
                , error) != HttpURLConnection.HTTP_OK) {
            retInt = -1;
        }


        return retInt;
    }

    @Subscribe
    public void onAfterShow(AfterShowEvent event) {
        if (isNewEntity) {
        } else {
//            if (StringUtils.hasText(getEditedEntity().getIpLimitation())) {
//                setVisibleIpLimit(true);
//                enableIpLimitation.setValue(true);
//            }
            gatewaySelector.setValue(getEditedEntity().getGateway());
            portalSelector.setValue(getEditedEntity().getPortal());
            nameField.setEditable(false);
        }
        oauth_consent_page.setValue(false);
        ldapCredentialField.setVisible(false);
        ldapPrincipalField.setVisible(false);
        ldapUrlField.setVisible(false);
        logoField.setVisible(false);
    }

    public String getPortConfig() {
        try {
            AppConfiguration config = dataManager.loadValue(
                            "select a from AppConfiguration a where a.name = :name", AppConfiguration.class)
                    .store("main")
                    .parameter("name", "portal_port")
                    .one();
            return config.getConfigvalue();
        } catch (Exception e) {
            log.error(e.getMessage());
            return "";
        }
    }

    @Subscribe("oauth_consent_page")
    public void onOauth_consent_pageValueChange(HasValue.ValueChangeEvent<Boolean> event) {
        if (event.getValue()) {
            logoField.setVisible(true);
            ldapCredentialField.setVisible(true);
            ldapPrincipalField.setVisible(true);
            ldapUrlField.setVisible(true);
            logoField.setVisible(true);
        } else {
            logoField.setVisible(false);
            ldapCredentialField.setVisible(false);
            ldapPrincipalField.setVisible(false);
            ldapUrlField.setVisible(false);
        }
    }
}