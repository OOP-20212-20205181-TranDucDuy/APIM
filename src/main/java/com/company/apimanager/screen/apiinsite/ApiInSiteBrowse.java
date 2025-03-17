package com.company.apimanager.screen.apiinsite;

import com.company.apimanager.app.KongAclDetails;
import com.company.apimanager.app.KongConnector;
import com.company.apimanager.app.Utility;
import com.company.apimanager.app.kong_object.ConsumerJwtCredential;
import com.company.apimanager.entity.*;
import com.company.apimanager.screen.apicall.ApiCallEdit;
import com.company.apimanager.screen.apicallwithconsumer.ApiCallWithConsumerScreen;
import com.company.apimanager.screen.brandlogin.ApiRegister;
import com.company.apimanager.screen.publishedproduct.HandledError;
import io.jmix.core.DataManager;
import io.jmix.ui.Notifications;
import io.jmix.ui.Screens;
import io.jmix.ui.action.Action;
import io.jmix.ui.action.BaseAction;
import io.jmix.ui.component.*;
import io.jmix.ui.model.CollectionContainer;
import io.jmix.ui.screen.*;
import io.jmix.ui.screen.LookupComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

@UiController("ApiInSite.browse")
@UiDescriptor("api-in-site-browse.xml")
@LookupComponent("apiInSitesTable")
public class ApiInSiteBrowse extends StandardLookup<ApiInSite> {
    @Autowired
    private CollectionContainer<ApiInSite> apiInSitesDc;

    private List<ApiInSite> apiInSiteList;
    private Site site;
    private PublishedProduct publishedProduct;

    @Autowired
    private Button turnOffBtn;
    @Autowired
    private GroupTable<ApiInSite> apiInSitesTable;
    @Autowired
    private Notifications notifications;
    @Autowired
    private DataManager dataManager;
    @Autowired
    private Label siteApisLabel;
    @Autowired
    private Screens screens;
    @Autowired
    private Button testBtn;
    @Autowired
    private Button testWithConsumerBtn;
    @Autowired
    private ConsumerJwtCredential consumerJwtCredential;
    @Autowired
    private EntityComboBox consumerField;

    public List<ApiInSite> getApiInSiteList() {
        return apiInSiteList;
    }

    private final Logger log = LoggerFactory.getLogger(ApiInSiteBrowse.class);

    public PublishedProduct getPublishedProduct() {
        return publishedProduct;
    }
    public void setPublishedProduct(PublishedProduct publishedProduct) {
        this.publishedProduct = publishedProduct;
    }

    public void setSite(Site site) {
        this.site = site;
    }

    public void setApiInSiteList(List<ApiInSite> apiInSiteList) {
        this.apiInSiteList = apiInSiteList;
    }

    @Subscribe
    public void onInit(InitEvent event) {
        apiInSitesTable.setItemClickAction(new BaseAction("itemClickAction")
                .withHandler(actionPerformedEvent -> {

                }));
    }

    @Subscribe("apiInSitesTable.create")
    public void onApiInSitesTableCreate(Action.ActionPerformedEvent event) {
        
    }

    @Subscribe("apiInSitesTable.remove")
    public void onApiInSitesTableRemove(Action.ActionPerformedEvent event) {
        
    }

    @Subscribe
    public void onBeforeShow(BeforeShowEvent event) {

        String value = "<b><font size=\"3\">"
                + "The product \"" + publishedProduct.getProduct().getName() +
                "\" 's APIs published in the Catalog of " + site.getName()
                + "</font><b/>";
        siteApisLabel.setHtmlEnabled(true);
        siteApisLabel.setHtmlSanitizerEnabled(true);
        siteApisLabel.setValue(value);

        if (apiInSiteList != null) {
            for (int i = 0; i < apiInSiteList.size(); i++) {
                ApiInSite apiInSite = apiInSiteList.get(i);
                apiInSite.setId(UUID.randomUUID());
                apiInSitesDc.getMutableItems().add(apiInSite);
            }
        }
        turnOffBtn.setEnabled(false);
        testBtn.setEnabled(false);
        testWithConsumerBtn.setEnabled(false);

        //Consumers
        try {
            List<ProductSubscription> productSubscriptionList = dataManager.load(ProductSubscription.class).query("select e from ProductSubscription e where e.published_product = :published_product")
                    .parameter("published_product", this.publishedProduct).list();

            List <Consumer> consumers = new ArrayList<Consumer>();
            for (int i = 0; i < productSubscriptionList.size(); i++) {
                consumers.add(productSubscriptionList.get(i).getConsumer());
            }
            consumerField.setOptionsList(consumers);

        }
        catch (Exception e) {
            notifications.create(Notifications.NotificationType.WARNING).
                    withCaption(e.getMessage())
                    .show();
        }

    }

    @Subscribe("apiInSitesTable")
    public void onApiInSitesTableSelection(Table.SelectionEvent<ApiInSite> event) {
        ApiInSite apiInSite = apiInSitesTable.getSingleSelected();
        if (apiInSite == null) {
            turnOffBtn.setEnabled(false);
            testBtn.setEnabled(false);
            testWithConsumerBtn.setEnabled(false);
        }
        else {
            turnOffBtn.setEnabled(true);
            testBtn.setEnabled(true);
            testWithConsumerBtn.setEnabled(true);
        }
    }

    private String newKongObdId(ApiInSite removedApi) {
        String strRet = "";
        int intLen = apiInSiteList.size();
        for (int i = 0; i < apiInSiteList.size() - 1; i++) {
            if (!apiInSiteList.get(i).getApi_id().equals(removedApi.getApi_id())) {
                strRet = strRet + apiInSiteList.get(i).getApi_id() + ";" +
                        apiInSiteList.get(i).getRoute_id() + ";" + apiInSiteList.get(i).getAcl_id()
                        + " ";
            }
        }
        strRet = strRet + apiInSiteList.get(intLen - 1).getApi_id() + ";" +
                apiInSiteList.get(intLen - 1).getRoute_id() + ";"
                + apiInSiteList.get(intLen - 1).getAcl_id();
        return  strRet;
    }

    private void newKongObdIdAndOAuthInfo(ApiInSite removedApi, StringBuilder objIds,
                                          StringBuilder oauthInfo) {
        String strId = "";
        String strInfo = "";
        int intLen = apiInSiteList.size();
        for (int i = 0; i < apiInSiteList.size() - 1; i++) {
            if (!apiInSiteList.get(i).getApi_id().equals(removedApi.getApi_id())) {
                strId = strId + apiInSiteList.get(i).getApi_id() + ";" +
                        apiInSiteList.get(i).getRoute_id() + ";" + apiInSiteList.get(i).getAcl_id()
                        + " ";
                String strProvisionKey = "";
                if (apiInSiteList.get(i).getOauthProvisionKey() != null) {
                    strProvisionKey = apiInSiteList.get(i).getOauthProvisionKey();
                }
                strInfo = strInfo + strProvisionKey + ";" +
                        apiInSiteList.get(i).getApi_id() + " ";
            }
        }
        if (!apiInSiteList.get(intLen - 1).getApi_id().equals(removedApi.getApi_id())) {
            strId = strId + apiInSiteList.get(intLen - 1).getApi_id() + ";" +
                    apiInSiteList.get(intLen - 1).getRoute_id() + ";"
                    + apiInSiteList.get(intLen - 1).getAcl_id();
            String strProvisionKey = "";
            if (apiInSiteList.get(intLen - 1).getOauthProvisionKey() != null) {
                strProvisionKey = apiInSiteList.get(intLen - 1).getOauthProvisionKey();
            }
            strInfo = strInfo + strProvisionKey + ";" +
                    apiInSiteList.get(intLen - 1).getApi_id();
        }
        objIds.setLength(0);
        objIds.append(strId);
        oauthInfo.setLength(0);
        oauthInfo.append(strInfo);
    }

    @Subscribe("turnOffBtn")
    public void onTurnOffBtnClick(Button.ClickEvent event) {
        ApiInSite apiInSite = apiInSitesTable.getSingleSelected();
        if (apiInSite == null) {
            return;
        }
        if (apiInSitesTable.getItems().size() <= 1) {
            notifications.create(Notifications.NotificationType.WARNING).
                    withCaption("There is only API in the Product, could not turn off ")
                    .show();
            return;
        }
        KongConnector kongConnector = new KongConnector(site.getDefaultManagementEndpoint());
        HandledError handledError = new HandledError();

        KongAclDetails kongAclDetails = new KongAclDetails();
        String thatGroup = site.getOwner().getUsername() + "."
                + site.getName() + "." + publishedProduct.getProduct().getName();
        if (kongConnector.deleteWholeService(apiInSite.getApi_id(), apiInSite.getRoute_id(),
                apiInSite.getAcl_id(), thatGroup, handledError) != 0) {
            notifications.create(Notifications.NotificationType.ERROR).
                    withCaption("Could not remove API route in Gateway: " +
                            handledError.getErrorMsg())
                    .show();
            return;
        }
        boolean saveError = false;
        try {
            StringBuilder strIds = new StringBuilder();
            StringBuilder strInfos = new StringBuilder();
            String mappingInfo = publishedProduct.getKong_mapping_info();

            String[] mappingApiList = mappingInfo.split(" ");
            StringBuilder newMapping = new StringBuilder();
            for (int i = 0; i < mappingApiList.length; i++) {
                String[] ele = mappingApiList[i].split(";");
                if (!Objects.equals(ele[0], apiInSite.getApi_id())) {
                    if (i < mappingApiList.length - 1) {
                        newMapping.append(mappingApiList[i]).append(" ");
                    } else {
                        newMapping.append(mappingApiList[i]);
                    }
                }
            }
            this.newKongObdIdAndOAuthInfo(apiInSite, strIds, strInfos);
            //publishedProduct.setKong_object_ids(this.newKongObdId(apiInSite));
            publishedProduct.setKong_object_ids(strIds.toString());
            publishedProduct.setKong_mapping_info(newMapping.toString());
            publishedProduct.setKong_oauth_info(strInfos.toString());
            dataManager.save(publishedProduct);
        }
        catch (Exception e) {
            saveError = true;
            notifications.create(Notifications.NotificationType.ERROR).
                    withCaption("Could not save published product: " +
                            e.getMessage()).show();
        }
        if (saveError) {
            return;
        }
        notifications.create(Notifications.NotificationType.HUMANIZED).
                withCaption("Removing API from Site successful")
                .show();
        //Determine index of removed

        List<DrupalLink> drupalLinks = dataManager.load(DrupalLink.class)
                .query("select d from DrupalLink d where d.publishedProduct=:published_product and d.kong_api_id=:kong_api_id")
                .parameter("published_product", publishedProduct)
                .parameter("kong_api_id", apiInSite.getApi_id())
                .list();

        if (!drupalLinks.isEmpty()) {
            Site site = publishedProduct.getSite();
            String siteName = site.getName().toLowerCase();
            String username = site.getOwner().getUsername();
            //String basicAuth = Base64.getEncoder().encodeToString((username + "-" + siteName).getBytes());
            //basicAuth = Base64.getEncoder().encodeToString(basicAuth.getBytes());
            for (DrupalLink drupalLink:drupalLinks) {
                String drupalId = drupalLink.getDrupal_api_id();
                drupalLink.setDrupal_api_id("");
                dataManager.save(drupalLink);
                Utility.deleteNode(Site.DRUPAL_ADMIN_USER, Site.DRUPAL_ADMIN_PASSWORD,
                        site.getDrupal_api(), drupalId, new HandledError());
            }
        }

        int intLen = apiInSitesDc.getMutableItems().size();
        int indRemoved = -1;
        for (int i = 0; i < intLen; i++) {
            if (apiInSitesDc.getMutableItems().get(i).getName().equals(apiInSite.getName())) {
                indRemoved = i;
                break;
            }
        }
        if (indRemoved != -1) {
            apiInSitesDc.getMutableItems().remove(indRemoved);
        }
    }

    @Subscribe("testBtn")
    public void onTestBtnClick(Button.ClickEvent event) {
        ApiInSite apiInSite = apiInSitesTable.getSingleSelected();
        if (apiInSite == null) {
            notifications.create(Notifications.NotificationType.WARNING).withCaption("Please select API").show();
            return;
        }

        ApiCallEdit apiCallEdit = screens.create(ApiCallEdit.class);
        ApiCall apiCall = new ApiCall();
        apiCall.setEndpoint(apiInSite.getApi_url());
        apiCallEdit.setEntityToEdit(apiCall);
        apiCallEdit.setApiEndpoint(apiInSite.getApi_url());
        apiCallEdit.show();
    }

    @Subscribe("testWithConsumerBtn")
    public void onTestWithConsumerBtnClick(Button.ClickEvent event) {
        ApiInSite apiInSite = apiInSitesTable.getSingleSelected();
        if (apiInSite == null) {
            notifications.create(Notifications.NotificationType.WARNING).withCaption("Please select API").show();
            return;
        }

        ApiCallWithConsumerScreen apiCallWithConsumerScreen = screens.create(ApiCallWithConsumerScreen.class);
        apiCallWithConsumerScreen.setApiEndpoint(apiInSite.getApi_url());
        apiCallWithConsumerScreen.setApiSite(site);
        apiCallWithConsumerScreen.show();
    }
}