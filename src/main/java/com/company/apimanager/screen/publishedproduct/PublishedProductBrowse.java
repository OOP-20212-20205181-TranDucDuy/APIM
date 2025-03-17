package com.company.apimanager.screen.publishedproduct;

import com.company.apimanager.app.*;

import com.company.apimanager.app.kong_action.Cors;
import com.company.apimanager.app.kong_action.RequestTransformer;
import com.company.apimanager.entity.*;
import com.company.apimanager.screen.apiinsite.ApiInSiteBrowse;
import com.company.apimanager.screen.brandlogin.ApiRegister;
import com.company.apimanager.screen.login.LoginScreen;
import com.company.apimanager.screen.publishedrestapi.PublishedRestApiBrowse;
import com.company.apimanager.screen.publishtoothergateway.GatewayIntefrace;
import com.company.apimanager.screen.publishtoothergateway.PublishToOtherGatewayScreen;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.jmix.core.DataManager;
import io.jmix.core.SaveContext;
import io.jmix.core.querycondition.PropertyCondition;
import io.jmix.core.security.CurrentAuthentication;
import io.jmix.ui.Dialogs;
import io.jmix.ui.Notifications;
import io.jmix.ui.RemoveOperation;
import io.jmix.ui.Screens;
import io.jmix.ui.action.Action;
import io.jmix.ui.action.DialogAction;
import io.jmix.ui.action.list.RemoveAction;
import io.jmix.ui.component.GroupTable;
import io.jmix.ui.component.HBoxLayout;
import io.jmix.ui.component.Table;
import io.jmix.ui.model.CollectionLoader;
import io.jmix.ui.model.CollectionPropertyContainer;
import io.jmix.ui.model.DataContext;
import io.jmix.ui.screen.*;
import liquibase.pro.packaged.P;
import org.apache.catalina.users.SparseUserDatabase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.*;
import javax.inject.Named;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.eclipse.persistence.eis.EISException.TIMEOUT;

@UiController("PublishedProduct.browse")
@UiDescriptor("published-product-browse.xml")
@LookupComponent("publishedProductsTable")
public class PublishedProductBrowse extends StandardLookup<PublishedProduct> {

    @Autowired
    private io.jmix.ui.component.Button deprecateBtn;
    @Autowired
    private io.jmix.ui.component.Button retireBtn;
    @Autowired
    private io.jmix.ui.component.Button publishBtn;
    @Autowired
    private io.jmix.ui.component.Button createBtn;
    @Autowired
    private DataManager dataManager;
    @Autowired
    private io.jmix.ui.component.Button archiveBtn;
    @Autowired
    private io.jmix.ui.component.Button updateBtn;
    @Autowired
    private Screens screens;
    @Autowired
    private io.jmix.ui.component.Button manageApiBtn;
    @Autowired
    private io.jmix.ui.component.Button manageApiBrowserBtn;
    @Autowired
    private CurrentAuthentication currentAuthentication;
    @Autowired
    private io.jmix.ui.component.Button publishOtherBtn;

    private final Logger log = LoggerFactory.getLogger(LoginScreen.class);

    @Autowired
    private Utility utility;
    @Autowired
    private io.jmix.ui.component.Button updatePortalProductBtn;
    @Autowired
    private io.jmix.ui.component.Button updatePortalProductNodeBtn;
    @Autowired
    private HBoxLayout lookupActions;
    @Autowired
    private HBoxLayout editActions;

//    private final Logger log = LoggerFactory.getLogger(PublishedProductBrowse.class);

    @Install(to = "publishedProductsTable.create", subject = "initializer")
    private void publishedProductsTableCreateInitializer(PublishedProduct publishedProduct) {
        publishedProduct.setProduct(product);
        publishedProduct.setSite(site);
    }

    @Autowired
    private io.jmix.ui.component.Button unStagingBtn;

    private Product product;
    private String productState;
    private String archivedState;
    private Site site;

    @Autowired
    private CollectionLoader<PublishedProduct> publishedProductsDl;
    @Autowired
    private io.jmix.ui.component.Label productLabel;
    @Autowired
    private GroupTable<PublishedProduct> publishedProductsTable;
    @Autowired
    private Notifications notifications;
    @Autowired
    protected Dialogs dialogs;

    private boolean kongRemoveOK;
    @Named("publishedProductsTable.remove")
    private RemoveAction<PublishedProduct> publishedProductsTableRemove;

    /*
    @Subscribe("publishedProductsTable.create")
    public void onPublishedProductsTableCreate(Action.ActionPerformedEvent event) {

    }

     */

    @Subscribe("publishedProductsTable.edit")
    public void onPublishedProductsTableEdit(Action.ActionPerformedEvent event) {

    }

    @Subscribe("publishedProductsTable.remove")
    public void onPublishedProductsTableRemove(Action.ActionPerformedEvent event) {

    }

    public void setProduct(Product product) {
        this.product = product;
        publishedProductsDl.setParameter("product", product);
        publishedProductsDl.setParameter("site", null);
    }

    public void setSite(Site site) {
        this.site = site;
        publishedProductsDl.setParameter("product", null);
        publishedProductsDl.setParameter("site", site);
    }

    public void setArchivedState(String archivedState) {
        this.archivedState = archivedState;
        if (archivedState != null) {
            publishedProductsDl.setParameter("archive_state", archivedState);
        }
    }

    public Product getProduct() {
        return product;
    }

    public void setProductState(String productState) {
        this.productState = productState;
        publishedProductsDl.setParameter("state", productState);
    }

    @Subscribe("updatePortalProductBtn")
    public void onUpdatePortalProductBtnClick(io.jmix.ui.component.Button.ClickEvent event) {
        try {
            List<PublishedProduct> portalProducts = new ArrayList<PublishedProduct>();
            List<PublishedProduct> publishedProducts = publishedProductsTable.getItems().getItems().stream().toList();
            boolean isEmpty = false;
            if (publishedProducts == null) {
                isEmpty = true;
            }
            else {
                if (publishedProducts.isEmpty()) {
                    isEmpty = true;
                }
            }
            if (isEmpty) {
                notifications.create(Notifications.NotificationType.WARNING)
                        .withCaption("No product!")
                        .show();
            }
            else {
                for (int i = 0; i < publishedProducts.size(); i++) {
                    if (publishedProducts.get(i).getState().equals("PUBLISHED")) {
                        portalProducts.add(publishedProducts.get(i));
                    }
                }
                if (!portalProducts.isEmpty()) {
                    DrupalConnector drupalConnector = new DrupalConnector(this.site.getDrupal_api(),
                            Site.DRUPAL_ADMIN_USER, Site.DRUPAL_ADMIN_PASSWORD);
                    CallDrupalApiError callDrupalApiError = new CallDrupalApiError();
                    if (drupalConnector.updateProductPage(portalProducts, callDrupalApiError) != HttpURLConnection.HTTP_OK) {
                        notifications.create(Notifications.NotificationType.ERROR)
                                .withCaption(callDrupalApiError.getErrorMsg())
                                .show();
                    }
                    else {
                        notifications.create(Notifications.NotificationType.HUMANIZED)
                                .withCaption("Update OK")
                                .show();
                    }
                }
            }
        }
        catch (Exception e) {

        }
    }

    @Subscribe("deprecateBtn")
    public void onDeprecateBtnClick(io.jmix.ui.component.Button.ClickEvent event) {
        dialogs.createOptionDialog()
                .withCaption("Confirmation")
                .withMessage("Are you sure to deprecate this product?")
                .withActions(
                        new DialogAction(DialogAction.Type.OK)
                                .withHandler(e ->
                                        this.handleDeprecateProduct()
                                ),
                        new DialogAction(DialogAction.Type.CANCEL))
                .show();
    }

    private void handleDeprecateProduct() {
        try {
            PublishedProduct publishedProduct = publishedProductsTable.getSingleSelected();
            assert publishedProduct != null;

            Site site = publishedProduct.getSite();
            String portalDrupalUrl = site.getDrupal_api();
            Product product = publishedProduct.getProduct();
            String siteName = site.getName().toLowerCase();
            String username = site.getOwner().getUsername();
            String basicAuth = Base64.getEncoder().encodeToString((username + "-" + siteName).getBytes());
            //basicAuth = Base64.getEncoder().encodeToString(basicAuth.getBytes());

            //basicAuth = "martinn@01";

            int drupalProductId;
            try {
                drupalProductId = publishedProduct.getDrupal_product_id();
            }
            catch (Exception e) {
                drupalProductId = 0;
            }

            HandledError handledError = new HandledError();
            String listJsonApi = "";
            boolean err = false;

            List<DrupalLink> drupalLinks = dataManager.load(DrupalLink.class)
                    .query("SELECT d from DrupalLink d where d.publishedProduct = :publishProduct")
                    .parameter("publishProduct", publishedProduct)
                    .list();

            //basicAuth = "martinn@01";

            if (drupalProductId > 0) {
                int deleteRet = Utility.deleteNode(Site.DRUPAL_ADMIN_USER, Site.DRUPAL_ADMIN_PASSWORD,
                        portalDrupalUrl, String.valueOf(drupalProductId), handledError);
                if ((deleteRet !=204) && (deleteRet != 404)){ //Khi xóa không thành công, và báo lỗi không phải không tồn tại node
                    notifications.create(Notifications.NotificationType.WARNING)
                            .withCaption("Could not remove product node in the Portal: " + handledError.getErrorMsg())
                            .show();
                    return;
                }
            }

            boolean gotError = false;
            for (DrupalLink drupalLink : drupalLinks) {
                if (drupalLink.getDrupal_api_id() == null) {
                    continue;
                }
                if (!drupalLink.getDrupal_api_id().isEmpty()) {
                    int deleteRet = Utility.deleteNode(Site.DRUPAL_ADMIN_USER, Site.DRUPAL_ADMIN_PASSWORD,
                            portalDrupalUrl, drupalLink.getDrupal_api_id(),handledError);
                    if ((deleteRet !=204) && (deleteRet != 404)){ //Khi xóa không thành công, và báo lỗi không phải không tồn tại node
                        notifications.create(Notifications.NotificationType.WARNING)
                                .withCaption("Could not remove API node of " +  drupalLink.getRestApi().getName()
                                        + " in the Portal: " + handledError.getErrorMsg())
                                .show();
                        gotError = true;
                        break;
                    }
                    else {
                        //drupalLink.setDrupal_api_id(null);
                        //dataManager.save(drupalLink);
                        dataManager.remove(drupalLink);
                    }
                }
            }
            if (gotError) {
                return;
            }

            publishedProduct.setState("DEPRECATED");
            publishedProduct.setDrupal_product_id(0);
            dataManager.save(publishedProduct);
            publishedProductsDl.load();
            setComponents();

            /*List<ApiRegister> apiRegisters = dataManager.load(ApiRegister.class)
                    .query("select a from ApiRegister a where a.product = :product")
                    .parameter("product", product)
                    .list();


            for (ApiRegister apiRegister : apiRegisters) {
                int apiDrupalId = 0;
                if (apiRegister.getDrupal_api_id() == null || apiRegister.getDrupal_api_id() == 0) {
                    continue;
                } else {
                    apiDrupalId = apiRegister.getDrupal_api_id();
                    gotError = !Utility.deleteNode(basicAuth, portalDrupalUrl, String.valueOf(apiDrupalId), handledError);
                }

                if (gotError) {
                    log.error("Error when delete api: " + handledError.getErrorMsg());
                    break;
                }


            }

            if (gotError) {
                notifications.create(Notifications.NotificationType.WARNING)
                        .withCaption("Error when deprecate api")
                        .show();
            }

             */
        } catch (Exception e) {
            notifications.create(Notifications.NotificationType.WARNING)
                    .withCaption(e.getMessage()).show();
        }
    }

    @Subscribe
    public void onBeforeShow(BeforeShowEvent event) {
        String value = "";
        if (product != null) {
            value = "<b><font size=\"3\">"
                    + "Staged catalogs of " + product.getName()
                    + "</font><b/>";
            publishedProductsTable.removeColumn(publishedProductsTable.getColumn("product"));
            publishedProductsTable.removeColumn(publishedProductsTable.getColumn("state"));
            deprecateBtn.setVisible(false);
            retireBtn.setVisible(false);
            publishBtn.setVisible(false);
            publishOtherBtn.setVisible(false);
            archiveBtn.setVisible(false);
            updateBtn.setVisible(false);
            manageApiBtn.setVisible(false);
            updatePortalProductBtn.setVisible(false);
            updatePortalProductNodeBtn.setVisible(false);
            editActions.setVisible(false);
        } else {
            if (site != null) {
                value = "<b><font size=\"3\">"
                        + "Products in " + site.getName()
                        + " catalog </font><b/>";
                publishedProductsTable.removeColumn(publishedProductsTable.getColumn("site"));
                createBtn.setVisible(false);
                deprecateBtn.setEnabled(false);
                retireBtn.setEnabled(false);
                publishBtn.setEnabled(false);
                publishOtherBtn.setEnabled(false);
                archiveBtn.setEnabled(false);
                updateBtn.setEnabled(false);
                manageApiBtn.setEnabled(false);
                manageApiBrowserBtn.setEnabled(false);
                updatePortalProductNodeBtn.setEnabled(false);

                User currUser = (User) currentAuthentication.getUser();
                String providerPlan = "";
                if (currUser.getProviderPlan() == null) {
                    providerPlan = "Pilot";
                } else {
                    providerPlan = currUser.getProviderPlan();
                }
                if (!providerPlan.equals("Enterprise")) {
                    publishOtherBtn.setVisible(false);
                    publishBtn.setCaption("Publish");
                }
            }
        }
        productLabel.setHtmlEnabled(true);
        productLabel.setHtmlSanitizerEnabled(true);
        productLabel.setValue(value);

        unStagingBtn.setEnabled(false);
    }

    @Subscribe(id = "publishedProductsDl", target = Target.DATA_LOADER)
    public void onPublishedProductsDlPreLoad(CollectionLoader.PreLoadEvent<PublishedProduct> event) {
        publishedProductsDl.setParameter("state", productState);
        if (archivedState != null) {
            publishedProductsDl.setParameter("archive_state", archivedState);
        }
    }

    @Subscribe("publishedProductsTable")
    public void onPublishedProductsTableSelection(Table.SelectionEvent<PublishedProduct> event) {
        setComponents();
    }

    private void setComponents() {
        try {
            /*
            java.util.List<PublishedProduct> productList = new ArrayList<PublishedProduct>();
            productList.addAll(event.getSelected());
            String productState = productList.get(0).getState();
             */

            PublishedProduct publishedProduct = publishedProductsTable.getSingleSelected();
            if (publishedProduct == null) {
                unStagingBtn.setEnabled(false);
                publishBtn.setEnabled(false);
                deprecateBtn.setEnabled(false);
                retireBtn.setEnabled(false);
                archiveBtn.setEnabled(false);
                updateBtn.setEnabled(false);
            } else {
                String productState = publishedProduct.getState();
                Site site = publishedProduct.getSite();
                switch (productState) {
                    case "STAGED":
                        unStagingBtn.setEnabled(true);
                        publishBtn.setEnabled(true);
                        publishOtherBtn.setEnabled(true);
                        deprecateBtn.setEnabled(false);
                        retireBtn.setEnabled(false);
                        archiveBtn.setEnabled(false);
                        updateBtn.setEnabled(false);
                        manageApiBtn.setEnabled(false);
                        manageApiBrowserBtn.setEnabled(false);

                        updatePortalProductNodeBtn.setEnabled(false);
                        break;
                    case "PUBLISHED":
                        unStagingBtn.setEnabled(false);
                        publishBtn.setEnabled(false);
                        publishOtherBtn.setEnabled(false);
                        deprecateBtn.setEnabled(true);
                        retireBtn.setEnabled(false);
                        archiveBtn.setEnabled(false);
                        if (site.getProduction_mode()) {
                            updateBtn.setEnabled(false);
                        } else {
                            updateBtn.setEnabled(true);
                        }
                        manageApiBtn.setEnabled(true);
                        manageApiBrowserBtn.setEnabled(true);

                        updatePortalProductNodeBtn.setEnabled(true);
                        break;
                    case "DEPRECATED":
                        unStagingBtn.setEnabled(false);
                        publishBtn.setEnabled(false);
                        publishOtherBtn.setEnabled(false);
                        deprecateBtn.setEnabled(false);
                        retireBtn.setEnabled(true);
                        archiveBtn.setEnabled(false);
                        updateBtn.setEnabled(false);
                        manageApiBtn.setEnabled(true);
                        manageApiBrowserBtn.setEnabled(true);

                        updatePortalProductNodeBtn.setEnabled(false);
                        break;
                    case "RETIRED":
                        unStagingBtn.setEnabled(true);
                        publishBtn.setEnabled(false);
                        publishOtherBtn.setEnabled(false);
                        deprecateBtn.setEnabled(false);
                        retireBtn.setEnabled(false);
                        archiveBtn.setEnabled(true);
                        updateBtn.setEnabled(false);
                        manageApiBtn.setEnabled(false);
                        manageApiBrowserBtn.setEnabled(false);

                        updatePortalProductNodeBtn.setEnabled(false);
                        break;
                    default:
                        unStagingBtn.setEnabled(false);
                        publishBtn.setEnabled(false);
                        publishOtherBtn.setEnabled(false);
                        deprecateBtn.setEnabled(false);
                        retireBtn.setEnabled(false);
                        archiveBtn.setEnabled(false);
                        updateBtn.setEnabled(false);
                        manageApiBtn.setEnabled(false);
                        manageApiBrowserBtn.setEnabled(false);

                        updatePortalProductNodeBtn.setEnabled(false);
                }
            }

        } catch (Exception e) {

        }
    }

    private void rePublishToPortal(PublishedProduct publishedProduct) {
        HandledError handledError = new HandledError();
        String listJsonApi = "";
        String portalDrupalUrl = site.getDrupal_api();
        int drupalProductId = publishedProduct.getDrupal_product_id() == null ? 0 : publishedProduct.getDrupal_product_id();
        boolean err = false;

        List<DrupalLink> drupalLinks = dataManager.load(DrupalLink.class)
                .query("SELECT d from DrupalLink d where d.publishedProduct = :publishProduct")
                .parameter("publishProduct", publishedProduct)
                .list();
        String siteName = site.getName().toLowerCase();
        String username = site.getOwner().getUsername();

        String basicAuth = Base64.getEncoder().encodeToString((username + "-" + siteName).getBytes());
        //basicAuth = Base64.getEncoder().encodeToString(basicAuth.getBytes());

        //basicAuth = "martinn@01";

        boolean apiPuslish = true;
        for (DrupalLink drupalLink : drupalLinks) {
            String infoApi;
            RestApi restApi = drupalLink.getRestApi();

            String apiDrupalId;
            if (drupalLink.getDrupal_api_id() == null || drupalLink.getDrupal_api_id().isBlank()) {
                infoApi = utility.createApiNode(site, Site.DRUPAL_ADMIN_USER, Site.DRUPAL_ADMIN_PASSWORD,
                        restApi, portalDrupalUrl, drupalLink, handledError);
            } else {
                apiDrupalId = drupalLink.getDrupal_api_id();
                infoApi = utility.getApiNode(Site.DRUPAL_ADMIN_USER, Site.DRUPAL_ADMIN_PASSWORD, portalDrupalUrl, apiDrupalId, handledError);
            }

            if (infoApi.isEmpty()) {
                notifications.create(Notifications.NotificationType.ERROR)
                        .withCaption("Error when create or update api: " + restApi.getName()
                        + handledError.getErrorMsg())
                        .show();
                apiPuslish = false;
                break;
            }

            if (listJsonApi.isEmpty()) {
                listJsonApi = listJsonApi + infoApi;
            } else {
                listJsonApi = listJsonApi + "," + infoApi;
            }
        }
        if (!apiPuslish) {
            return;
        }

        if (!listJsonApi.isEmpty() && drupalProductId != 0) {
            err = Utility.updateProductNode(Site.DRUPAL_ADMIN_USER, Site.DRUPAL_ADMIN_PASSWORD, portalDrupalUrl, listJsonApi, (String.valueOf(drupalProductId)), publishedProduct.getProduct(), handledError);
        } else if (!listJsonApi.isEmpty()) {
            err = utility.createProductNode(Site.DRUPAL_ADMIN_USER, Site.DRUPAL_ADMIN_PASSWORD,
                    portalDrupalUrl, listJsonApi, publishedProduct, handledError);
        }

        if (err) {
            notifications.create(Notifications.NotificationType.ERROR)
                    .withCaption("Error when create or update product: "
                            + handledError.getErrorMsg())
                    .show();
        }
        else {
            notifications.create(Notifications.NotificationType.HUMANIZED)
                    .withCaption("Update successful!")
                    .show();
        }
    }

    @Subscribe("updatePortalProductNodeBtn")
    public void onUpdatePortalProductNodeBtnClick(io.jmix.ui.component.Button.ClickEvent event) {
        PublishedProduct publishedProduct = publishedProductsTable.getSingleSelected();
        if (publishedProduct == null) {
            return;
        }
        if (publishedProduct.getState().equals("PUBLISHED")) {
            this.rePublishToPortal(publishedProduct);
        }
        else {

        }
    }

    @Subscribe("publishBtn")
    public void onPublishBtnClick(io.jmix.ui.component.Button.ClickEvent event) {
        dialogs.createOptionDialog()
                .withCaption("Confirmation")
                .withMessage("Are you sure to publish this product?")
                .withActions(
                        new DialogAction(DialogAction.Type.OK)
                                .withHandler(e ->
                                        {
                                            try {
                                                this.handlePublishProduct();
                                            } catch (IOException ex) {
                                                throw new RuntimeException(ex);
                                            }
                                        }
                                ),
                        new DialogAction(DialogAction.Type.CANCEL))
                .show();
    }

    private void handlePublishProduct() throws IOException {
        PublishedProduct publishedProduct = publishedProductsTable.getSingleSelected();
        if (publishedProduct == null) {
            return;
        }

        Product productToPublish = publishedProduct.getProduct();
        List<ApiRegister> apiRegisters = dataManager.load(ApiRegister.class).query("select e from ApiRegister e where e.product = :product")
                .parameter("product", productToPublish).list();
        if (apiRegisters == null) {
            notifications.create(Notifications.NotificationType.WARNING).
                    withCaption("No API in the product")
                    .show();
            return;
        }
        if (apiRegisters.size() == 0) {
            notifications.create(Notifications.NotificationType.WARNING).
                    withCaption("No API in the product")
                    .show();
            return;
        }
        boolean kongServiceCreated = true;
        HandledError handledError = new HandledError();
        GatewayIntefrace gatewayIntefrace = new GatewayIntefrace(site);
        StringBuilder stringBuilderIds = new StringBuilder();
        StringBuilder stringBuilderInfo = new StringBuilder();
        StringBuilder kongMappingInfo = new StringBuilder();

        String gatewayManagementEndpoint = site.getDefaultManagementEndpoint();
        if (gatewayManagementEndpoint == null) {
            notifications.create(Notifications.NotificationType.ERROR).
                    withCaption("Could not find the default gateway of the site")
                    .show();
            return;
        }

        String listJsonApi = "";
        String portalDrupalUrl = site.getDrupal_api();
        int drupalProductId = publishedProduct.getDrupal_product_id() == null ? 0 : publishedProduct.getDrupal_product_id();
        boolean err = false;

        List<DrupalLink> drupalLinks = dataManager.load(DrupalLink.class)
                .query("SELECT d from DrupalLink d where d.publishedProduct = :publishProduct")
                .parameter("publishProduct", publishedProduct)
                .list();
        String siteName = site.getName().toLowerCase();
        String username =
                site.getOwner().getUsername();

        String basicAuth = Base64.getEncoder().encodeToString((username + "-" + siteName).getBytes());
        //basicAuth = Base64.getEncoder().encodeToString(basicAuth.getBytes());

        //basicAuth = "martinn@01";

        if (!drupalLinks.isEmpty()) {
            dataManager.remove(drupalLinks);
        }

        boolean publishToPortal = true;

        if (!apiRegisters.isEmpty()) {
            for (ApiRegister apiRegister:apiRegisters) {
                DrupalLink drupalLink = dataManager.create(DrupalLink.class);
                drupalLink.setRestApi(apiRegister.getApi());
                drupalLink.setPublishedProduct(publishedProduct);
                dataManager.save(drupalLink);

                String infoApi;
                infoApi = utility.createApiNode(site, Site.DRUPAL_ADMIN_USER, Site.DRUPAL_ADMIN_PASSWORD,
                        apiRegister.getApi(), portalDrupalUrl, drupalLink, handledError);

                if (infoApi.isEmpty()) {
                    log.error("Error when create or update api");
                    log.error(handledError.getErrorMsg());
                    publishToPortal = false;
                    continue;
                }

                if (listJsonApi.isEmpty()) {
                    listJsonApi = listJsonApi + infoApi;
                } else {
                    listJsonApi = listJsonApi + "," + infoApi;
                }
            }
        }

        /*
        for (DrupalLink drupalLink : drupalLinks) {
            String infoApi;
            RestApi restApi = drupalLink.getRestApi();

            String apiDrupalId = drupalLink.getDrupal_api_id();
            if (apiDrupalId == null) {
                infoApi = utility.createApiNode(site, Site.DRUPAL_ADMIN_USER, Site.DRUPAL_ADMIN_PASSWORD, restApi, portalDrupalUrl, drupalLink, handledError);
            }
            else {
                if (apiDrupalId.isEmpty()) {
                    infoApi = utility.createApiNode(site, Site.DRUPAL_ADMIN_USER, Site.DRUPAL_ADMIN_PASSWORD, restApi, portalDrupalUrl, drupalLink, handledError);
                }
                else {
                    apiDrupalId = drupalLink.getDrupal_api_id();
                    infoApi = utility.getApiNode(Site.DRUPAL_ADMIN_USER, Site.DRUPAL_ADMIN_PASSWORD, portalDrupalUrl, apiDrupalId, handledError);
                }
            }

            if (infoApi.isEmpty()) {
                log.error("Error when create or update api");
                log.error(handledError.getErrorMsg());
                publishToPortal = false;
                continue;
            }

            if (listJsonApi.isEmpty()) {
                listJsonApi = listJsonApi + infoApi;
            } else {
                listJsonApi = listJsonApi + "," + infoApi;
            }
        }

         */

        if (!listJsonApi.isEmpty() && drupalProductId != 0) {
            err = Utility.updateProductNode(Site.DRUPAL_ADMIN_USER, Site.DRUPAL_ADMIN_PASSWORD, portalDrupalUrl,
                    listJsonApi, (String.valueOf(drupalProductId)), publishedProduct.getProduct(), handledError);
        } else if (!listJsonApi.isEmpty()) {
            err = utility.createProductNode(Site.DRUPAL_ADMIN_USER, Site.DRUPAL_ADMIN_PASSWORD,
                    portalDrupalUrl, listJsonApi, publishedProduct, handledError);
        }

        if (err) {
            log.error("Error when update or create product");
            log.error(handledError.getErrorMsg());
            publishToPortal = false;
        }

        /*if (gatewayIntefrace.publishProduct(productToPublish, site.getGateway(), apiRegisters,
                stringBuilderIds, handledError) != 0) {
         */
        Date startTime = new Date();
        if (gatewayIntefrace.publishProductToGateway(productToPublish, gatewayManagementEndpoint,
                apiRegisters, stringBuilderIds, stringBuilderInfo, kongMappingInfo, handledError) != 0) {
            notifications.create(Notifications.NotificationType.ERROR).
                    withCaption(handledError.getErrorMsg())
                    .show();
            return;
        }
        Date endTime = new Date();
        long durable = Math.abs(startTime.getTime() - endTime.getTime());
        SimpleDateFormat formatDate = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat formatTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String content = "FIS_APIM_001|APIM|||||||" + formatTime.format(startTime) + "|"
                + formatTime.format(endTime) + "|" + durable + "|200|||PUBLISHED_API|||||||||||";
        utility.writeFile(formatDate.format(startTime).toString() + ".txt", content + "\n");
        publishedProduct.setState("PUBLISHED");
        publishedProduct.setKong_object_ids(stringBuilderIds.toString());
        publishedProduct.setKong_oauth_info(stringBuilderInfo.toString());
        publishedProduct.setKong_mapping_info(kongMappingInfo.toString());
        dataManager.save(publishedProduct);
        publishedProductsDl.load();
        setComponents();
        Map<String, String> listApi = Arrays.stream(kongMappingInfo.toString().split(" ")).collect(Collectors.toMap(
                tuple -> (tuple.split(";")[1]),
                tuple -> (tuple.split(";")[0]))
        );

        for (DrupalLink drupalLink : drupalLinks) {
            if (listApi.containsKey(drupalLink.getRestApi().getId().toString())) {
                drupalLink.setKong_api_id(listApi.get(drupalLink.getRestApi().getId().toString()));
                dataManager.save(drupalLink);
            }
        }

        // Publish to Portal
        String managementEndpoint = null;
        String invocationEndpoint = null;
        if (site.getSelf_hosted_gateway_used()) {
            SelfHostedGateway selfHostedGateway = site.getSelf_hosted_gateway();
            if (selfHostedGateway != null) {
                invocationEndpoint = selfHostedGateway.getInvocation_endpoint();
                managementEndpoint = selfHostedGateway.getManagement_endpoint();
            }
        } else {
            GatewayService gatewayService = site.getGateway();
            if (gatewayService != null) {
                invocationEndpoint = gatewayService.getInvocation_endpoint();
                managementEndpoint = gatewayService.getManagement_endpoint();
            }
        }
        KongServiceObjIds kongServiceObjIds = new KongServiceObjIds();
        HandledError getIdsError = new HandledError();
        this.getKongObjId(publishedProduct, kongServiceObjIds, getIdsError);
        if (!getIdsError.getErrorMsg().equals("")) {
            notifications.create(Notifications.NotificationType.ERROR).
                    withCaption("Could not get API info from API Manager:" +
                            getIdsError.getErrorMsg()).show();
            return;
        }
        if (managementEndpoint == null) {
            notifications.create(Notifications.NotificationType.ERROR).
                    withCaption("Could not get management endpoint of the gateway");
            return;
        }

        String[] servicesIdList = kongServiceObjIds.getServicesIdList();
        String[] provisionKeyList = kongServiceObjIds.getProvisionKeyList();
        String[] routeIdList = kongServiceObjIds.getRoutesIdList();

        KongServiceDetails[] kongServiceDetailsList = new KongServiceDetails[servicesIdList.length];
        KongRouteDetails[] routeDetails = new KongRouteDetails[routeIdList.length];
        KongConnector kongConnector = new KongConnector(managementEndpoint);
        SaveContext saveContext = new SaveContext().setDiscardSaved(true);
//        dataManager.remove();
        HandledError handledError2 = new HandledError();
        String consentPageUrl0 = utility.getAppConfigValue("consentPageUrl");

        List<PublishedRestApi> removeList = dataManager.load(PublishedRestApi.class)
                .query("select e from PublishedRestApi e where  e.product = :product")
                .parameter("product", publishedProduct)
                .list();
        for (int i = 0; i < servicesIdList.length; i++) {
            kongServiceDetailsList[i] = kongConnector.getServicebyName(servicesIdList[i], handledError2);
            routeDetails[i] = kongConnector.getRoutebyName(routeIdList[i], handledError2);
            String routePath = routeDetails[i].getPaths()[0];
            if ((kongServiceDetailsList[i] == null)) {
                break;
            }
            PublishedRestApi restApi = dataManager.load(PublishedRestApi.class)
                    .condition(PropertyCondition.equal("kongServiceId", UUID
                            .fromString(kongServiceDetailsList[i].getId())))
                    .optional()
                    .orElse(dataManager.create(PublishedRestApi.class));
            removeList.remove(restApi);
            restApi.setPath(invocationEndpoint + routePath);
            log.info("Path: " + routePath);
            System.out.println("Path: " + routePath);
            restApi.setName(kongServiceDetailsList[i].getName());
            restApi.setConsent_url(consentPageUrl0 + "?product=" + publishedProduct.getId().toString()
                    + "&service=" + kongServiceDetailsList[i].getId() +
                    "&site=" + site.getId().toString() + "&api=" + restApi.getPath());
            restApi.setOauthProvisionKey(provisionKeyList[i]);
            restApi.setPlugin(kongConnector.getRawServicePlugins(servicesIdList[i], handledError2));
            restApi.setKongServiceId(UUID
                    .fromString(kongServiceDetailsList[i].getId()));
            restApi.setHost(kongServiceDetailsList[i].getHost());
            restApi.setProduct(publishedProduct);
            saveContext.saving(restApi);
        }
        dataManager.save(saveContext);
        dataManager.remove(removeList);

        ///

        if (publishToPortal) {
            notifications.create(Notifications.NotificationType.HUMANIZED).
                    withCaption("Publish successful")
                    .show();
        }
        else {
            notifications.create(Notifications.NotificationType.WARNING).
                    withCaption("Publish successful, but information on Portal not updated")
                    .show();
        }
    }

    @Subscribe("publishOtherBtn")
    public void onPublishOtherBtnClick(io.jmix.ui.component.Button.ClickEvent event) {
        PublishedProduct publishedProduct = publishedProductsTable.getSingleSelected();
        if (publishedProduct == null) {
            return;
        }
        Product productToPublish = publishedProduct.getProduct();
        PublishToOtherGatewayScreen publishToOtherGatewayScreen = screens.create(PublishToOtherGatewayScreen.class);
        //publishToOtherGatewayScreen.setMember(site, site.getGateway(), productToPublish);
        publishToOtherGatewayScreen.show();
    }

    private int updateSingleApi(KongConnector kongConnector,
                                RestApi restApi, HandledError handledError) {
        String serviceName = restApi.getOwner().getUsername()
                + "." + site.getName() + "." + restApi.getName();
        KongNewServiceInfo kongNewServiceInfo = new KongNewServiceInfo(serviceName,
                restApi.getTarget_endpoint());
        KongServiceDetails existKongServiceDetails = kongConnector.getServicebyName(serviceName
                , handledError);
        if (existKongServiceDetails == null) {
            handledError.setErrorMsg("Service not found");
            return -1;
        }
        String host = "";
        String path = "";
        int intRet = 0;

        try {
            if (restApi.getHost_group_path() != null) {    //Use balanced host group (upstream target)
                path = restApi.getHost_group_path();
                host = serviceName + "_upstream";
            } else {
                URI uri = new URI(restApi.getTarget_endpoint());
                host = uri.getHost();
                path = uri.getPath();
                if (path == null) {
                    path = "/";
                }
                if (path.isEmpty()) {
                    path = "/";
                }
            }

            //Create upstream & target
            boolean createUpstream = true;
            if (restApi.getHost_group_path() != null) {    //Use balanced host group (upstream target)
                //Create upstream & targets
                String upstreamName = serviceName + "_upstream";
                KongUpstreamDetails kongUpstreamDetails = new KongUpstreamDetails();
                if (kongConnector.createOrUpdateUpStream(upstreamName, kongUpstreamDetails, handledError) != 0) {
                    handledError.setErrorMsg("Could not create upstream on the API Gateway: " + handledError.getErrorMsg());
                    createUpstream = false;
                } else {
                    if (restApi.getBalancedHosts() == null) {
                        handledError.setErrorMsg("Could not create upstream on the API Gateway: No backend host");
                        createUpstream = false;
                    } else {
                        if (restApi.getBalancedHosts().size() == 0) {
                            handledError.setErrorMsg("Could not create upstream on the API Gateway: No backend host");
                            createUpstream = false;
                        } else {
                            for (int i = 0; i < restApi.getBalancedHosts().size(); i++) {
                                KongTargetDetails kongTargetDetails = new KongTargetDetails();
                                if (kongConnector.createOrUpdateUpstreamTarget(upstreamName,
                                        restApi.getBalancedHosts().get(i).getHostname(),
                                        restApi.getBalancedHosts().get(i).getHost_port(),
                                        restApi.getBalancedHosts().get(i).getWeight(),
                                        kongTargetDetails, handledError) != 0) {
                                    handledError.setErrorMsg("Could not create target of upstream on the API Gateway: "
                                            + handledError.getErrorMsg());
                                    return -1;
                                }
                            }
                        }
                    }
                }

            }
        } catch (Exception e) {
            handledError.setErrorMsg("URI of service is wrong");
            intRet = -2;
        }
        if (intRet != 0) {
            return intRet;
        }
        existKongServiceDetails.setHost(host);
        existKongServiceDetails.setPath(path);
        intRet = kongConnector.updateService(existKongServiceDetails, handledError);

        if (intRet != 200) {
            return intRet;
        }

        KongServicePlugin kongServicePlugin = kongConnector.getServicePluginByType(serviceName, "request-transformer", handledError);
        if (StringUtils.hasText(restApi.getRequestTransformer())) {
            StringBuilder pluginId = new StringBuilder(kongServicePlugin == null ? "" : kongServicePlugin.getKongId());
            RequestTransformer requestTransformerAction = new RequestTransformer(kongConnector.getKongManagementUrl());
            int ret = requestTransformerAction.createOrUpdate(serviceName, restApi.getRequestTransformer(), pluginId, handledError);
            if (ret != 0) {
                handledError.setErrorMsg("Could not update Request Transformer: " + handledError.getErrorMsg());
                return -1;
            }
        } else {
            if (kongServicePlugin != null) {
                kongConnector.deletePluginWithType(serviceName, "request-transformer", handledError);
            }
        }

        KongServicePlugin kongResponseTransServicePlugin = kongConnector.getServicePluginByType(serviceName, "response-transformer", handledError);
        if (kongResponseTransServicePlugin != null) {
            if (StringUtils.hasText(restApi.getResponseTransformer())) {
                KongTransformationPlugin plugin = new KongTransformationPlugin(kongConnector.getKongManagementUrl());
                intRet = plugin.updateResponseTransformerPlugin(serviceName, kongResponseTransServicePlugin.getKongId(),
                        handledError, restApi.getResponseTransformer());
                if (intRet != 200) {
                    handledError.setErrorMsg("Could not update Response Transformer Plugin: "
                            + handledError.getErrorMsg());
                    return -1;
                }
            } else {
                kongConnector.deletePluginWithType(serviceName, "response-transformer", handledError);
            }
        } else {
            if (StringUtils.hasText(restApi.getResponseTransformer())) {
                StringBuilder newPluginId = new StringBuilder();
                KongTransformationPlugin plugin = new KongTransformationPlugin(kongConnector.getKongManagementUrl());
                intRet = plugin.createResponseTransformerPlugin(serviceName, newPluginId,
                        handledError, restApi.getResponseTransformer());
                if (intRet != 201) {
                    handledError.setErrorMsg("Could not Update Response Transformer Plugin: "
                            + handledError.getErrorMsg());
                    return -1;
                }
            }
        }
        //---schema validator
        kongServicePlugin = kongConnector.getServicePluginByType(serviceName, "schema-validator", handledError);

        if (restApi.getEnable_validation_schema() != null && restApi.getEnable_validation_schema()) {
            intRet = kongConnector.createOrUpdateValidationSchemaPlugin(serviceName, restApi, handledError);
            if (intRet != 0) {
                handledError.setErrorMsg("Could not update validate schema plugin: "
                        + handledError.getErrorMsg());
            } else {
                intRet = 200;
            }
        } else if (kongServicePlugin != null) {
            intRet = kongConnector.deleteServicePlugin(serviceName, kongServicePlugin.getKongId(), handledError);
            if (intRet == 204) {
                intRet = 200;
            }
        }
        // --------------------

        // --------------CORS--------------
        KongServicePlugin kongCorsServicePlugin = kongConnector.getServicePluginByType(serviceName, "cors", handledError);
        if (StringUtils.hasText(restApi.getCors())) {
            Cors plugin = new Cors(kongConnector.getKongManagementUrl());
            StringBuilder pluginId = new StringBuilder(kongCorsServicePlugin == null ? "" : kongCorsServicePlugin.getKongId());
            intRet = plugin.createOrUpdate(serviceName, restApi.getCors(), pluginId,
                    handledError);
            if (intRet != 0) {
                return -1;
            }
        } else {
            intRet = kongConnector.deletePluginWithType(serviceName, "cors", handledError);
            if (intRet != 204) {
                handledError.setErrorMsg("Could not update CORS Plugin: "
                        + handledError.getErrorMsg());
                return -1;
            }
        }
        // ----------------------------------------

        if ((intRet == 201) || (intRet == 0) || (intRet == 204)) {
            return 200;
        }
        return intRet;
    }

    private int findServiceinGateway(String serviceName, KongServiceDetails[] kongServiceDetailsList) {
        int ind = -1;
        for (int i = 0; i < kongServiceDetailsList.length; i++) {
            if (serviceName.equals(kongServiceDetailsList[i].getName())) {
                ind = i;
                break;
            }
        }
        return ind;
    }

    private boolean findGwServiceAgainstApiLis(KongServiceDetails kongServiceDetails,
                                               List<ApiRegister> apiRegisterList) {
        boolean found = false;
        for (int i = 0; i < apiRegisterList.size(); i++) {
            RestApi restApi = apiRegisterList.get(i).getApi();
            String serviceName = restApi.getOwner().getUsername()
                    + "." + site.getName() + "." + restApi.getName();
            if (kongServiceDetails.getName().equals(serviceName)) {
                found = true;
                break;
            }
        }
        return found;
    }

    private int FindServiceDrupalLink(List<DrupalLink> drupalLinks, RestApi restApi) {
        int n = -1;

        for (int i= 0; i < drupalLinks.size(); i++) {
            RestApi drupalRestApi = drupalLinks.get(i).getRestApi();
            if (drupalRestApi.getId().equals(restApi.getId())) {
                n = i;
                break;
            }
        }
        return n;
    }

    private int updateGatewayServices(HandledError updateError) {

        //String gwMgtEndpoint = site.getGateway().getManagement_endpoint();
        String gwMgtEndpoint = site.getDefaultManagementEndpoint();
        String portalDrupalUrl = site.getDrupal_api();
        if (gwMgtEndpoint == null) {
            updateError.setErrorMsg("Could not get default gateway ");
            return -5;
        }
        KongConnector kongConnector = new KongConnector(gwMgtEndpoint);
        PublishedProduct publishedProduct = publishedProductsTable.getSingleSelected();
        if (publishedProduct == null) {
            return -1;
        }

        int retInt = 0;
        Product product = publishedProduct.getProduct();
        List<ApiRegister> apiRegisters = dataManager.load(ApiRegister.class).query("select e from ApiRegister e where e.product = :product")
                .parameter("product", product).list();
        if (apiRegisters == null) {
            updateError.setErrorMsg("No API in the product");
            return -1;
        }

        if (apiRegisters.size() == 0) {
            updateError.setErrorMsg("No API in the product");
            return -2;
        }
        HandledError handledError = new HandledError();

        //Get service, route, acl id
        KongServiceObjIds kongServiceObjIds = new KongServiceObjIds();
        HandledError getIdsError = new HandledError();
        this.getKongObjId(publishedProduct, kongServiceObjIds, getIdsError);
        if (!getIdsError.getErrorMsg().equals("")) {
            updateError.setErrorMsg("Could not get object id: " + getIdsError.getErrorMsg());
            return -3;
        }
        //Check API in gateway
        boolean checkApiGotError = false;
        String[] servicesIdList = kongServiceObjIds.getServicesIdList();
        String[] routesIdList = kongServiceObjIds.getRoutesIdList();
        String[] aclsIdList = kongServiceObjIds.getAclsIdList();
        String[] provisionKeyList = kongServiceObjIds.getProvisionKeyList();

        KongServiceDetails[] kongServiceDetailsList = new KongServiceDetails[servicesIdList.length];
        for (int i = 0; i < servicesIdList.length; i++) {
            kongServiceDetailsList[i] = kongConnector.getServicebyName(servicesIdList[i]
                    , handledError);

            if (kongServiceDetailsList[i] == null) {
                checkApiGotError = true;
                break;
            }
        }
        if (checkApiGotError) {
            updateError.setErrorMsg("Check API in gateway failed: " + handledError.getErrorMsg());
            return -4;
        }

        boolean gotError = false;
        String kongObjsId = "";
        String kongOAuthInfo = "";
        String kongMappingInfo = "";


        int drupalProductId = publishedProduct.getDrupal_product_id() == null ? 0 : publishedProduct.getDrupal_product_id();
        String listJsonApi = "";
        List<DrupalLink> drupalLinks = dataManager.load(DrupalLink.class)
                .query("SELECT d from DrupalLink d where d.publishedProduct = :publishProduct")
                .parameter("publishProduct", publishedProduct)
                .list();
        String siteName = site.getName().toLowerCase();
        String username = site.getOwner().getUsername();
        String basicAuth = Base64.getEncoder().encodeToString((username + "-" + siteName).getBytes());
        basicAuth = Base64.getEncoder().encodeToString(basicAuth.getBytes());

        /*
        for (DrupalLink drupalLink : drupalLinks) {
            String apiDrupalId, infoApi;
            if (drupalLink.getDrupal_api_id() == null || drupalLink.getDrupal_api_id().isBlank()) {
                infoApi = utility.createApiNode(site, Site.DRUPAL_ADMIN_USER, Site.DRUPAL_ADMIN_PASSWORD, drupalLink.getRestApi(), portalDrupalUrl, drupalLink, handledError);
            } else {
                apiDrupalId = drupalLink.getDrupal_api_id();
                infoApi = utility.getApiNode(Site.DRUPAL_ADMIN_USER, Site.DRUPAL_ADMIN_PASSWORD, portalDrupalUrl, apiDrupalId, handledError);
                utility.updateApiNode(site, Site.DRUPAL_ADMIN_USER, Site.DRUPAL_ADMIN_PASSWORD, drupalLink.getRestApi(), portalDrupalUrl, drupalLink, handledError);
            }

            if (infoApi.isEmpty()) {
                log.error("Error when create api node");
                log.error(handledError.getErrorMsg());
                continue;
            }

            if (listJsonApi.isEmpty()) {
                listJsonApi = listJsonApi + infoApi;
            } else {
                listJsonApi = listJsonApi + "," + infoApi;
            }
        }

         */

        List<DrupalLink> drupalUpdate = new ArrayList<DrupalLink>();
        List<DrupalLink> drupalNew = new ArrayList<DrupalLink>();
        List<DrupalLink> drupalDelete = new ArrayList<DrupalLink>();

        for (int i = 0; i < apiRegisters.size(); i++) {
            RestApi restApi = apiRegisters.get(i).getApi();
            String serviceName = restApi.getOwner().getUsername()
                    + "." + site.getName() + "." + restApi.getName();

            //Find
            int intInd = findServiceinGateway(serviceName, kongServiceDetailsList);
            if (intInd >= 0) {
                //Found: Update
                int intUpdate = updateSingleApi(kongConnector, restApi, handledError);
                if (intUpdate != 200) {
                    gotError = true;
                    log.error("Error when update single api: " + handledError.getErrorMsg());
                    break;
                }
                kongObjsId = kongObjsId + servicesIdList[intInd] + ";" +
                        routesIdList[intInd] + ";" + aclsIdList[intInd] + " ";
                kongOAuthInfo = kongOAuthInfo + provisionKeyList[intInd] + ";" + servicesIdList[intInd]
                        + " ";
                kongMappingInfo = kongMappingInfo + servicesIdList[intInd] + ";" + restApi.getId().toString() + " ";

                //Find drupal link
                int ind = this.FindServiceDrupalLink(drupalLinks,restApi);
                if (ind < 0) {
                    //Create Drupal link
                    DrupalLink newDrupalLink = dataManager.create(DrupalLink.class);
                    newDrupalLink.setRestApi(restApi);
                    newDrupalLink.setPublishedProduct(publishedProduct);
                    dataManager.save(newDrupalLink);
                    drupalUpdate.add(newDrupalLink);
                }
                else {
                    drupalUpdate.add(drupalLinks.get(ind));
                }
            } else {
                //Create
                StringBuilder apiObjId = new StringBuilder();
                StringBuilder oauthInfo = new StringBuilder();
                StringBuilder mappingInfo = new StringBuilder();
                GatewayIntefrace gatewayIntefrace = new GatewayIntefrace(publishedProduct.getSite());
                gatewayIntefrace.setSite(site);
                if (gatewayIntefrace.publishSingleApi(kongConnector, publishedProduct.getProduct(),
                        restApi, apiObjId, oauthInfo, mappingInfo, handledError) != 0) {
                    gotError = true;
                    log.error("Error when updateBtn publish new single api: " + handledError.getErrorMsg());
                    break;
                } else {
                    kongObjsId = kongObjsId + apiObjId.toString() + " ";
                    kongOAuthInfo = kongOAuthInfo + oauthInfo.toString() + " ";
                    kongMappingInfo = kongMappingInfo + mappingInfo.toString() + " ";
                }

                //Create Drupal link
                int ind = this.FindServiceDrupalLink(drupalLinks,restApi);
                if (ind < 0) {
                    //Create Drupal link
                    DrupalLink newDrupalLink = dataManager.create(DrupalLink.class);
                    newDrupalLink.setRestApi(restApi);
                    newDrupalLink.setPublishedProduct(publishedProduct);
                    dataManager.save(newDrupalLink);
                    drupalNew.add(newDrupalLink);
                }
                else {
                    drupalNew.add(drupalLinks.get(ind));
                }
            }
        }

        //Detect API removed from product
        boolean deleteError = false;
        for (int i = 0; i < kongServiceDetailsList.length; i++) {
            if (!findGwServiceAgainstApiLis(kongServiceDetailsList[i], apiRegisters)) {
                //Remove
                String thatGroup = site.getOwner().getUsername() + "."
                        + site.getName() + "." + publishedProduct.getProduct().getName();
                if (kongConnector.deleteWholeService(servicesIdList[i], routesIdList[i],
                        aclsIdList[i], thatGroup, handledError) != 0) {
                    updateError.setErrorMsg("Could not delete the service: " + servicesIdList[i]
                            + handledError.getErrorMsg());
                    deleteError = true;
                    break;
                }
            }
        }
        if (deleteError) {
            return -5;
        }

        //Detect Drupal removed
        for (DrupalLink drupalLink : drupalLinks) {
            boolean found =false;
            for (ApiRegister apiRegister : apiRegisters) {
                if (drupalLink.getRestApi().equals(apiRegister.getApi())) {
                    found = true;
                    break;
                }
            }
            if (!found) { //Removed
                drupalDelete.add(drupalLink);
            }
        }

        //Update Drupal site
        for (DrupalLink updatedLink : drupalUpdate) {
            String apiDrupalId, infoApi;
            if (updatedLink.getDrupal_api_id() == null) {
                infoApi = utility.createApiNode(site, Site.DRUPAL_ADMIN_USER, Site.DRUPAL_ADMIN_PASSWORD,
                        updatedLink.getRestApi(), portalDrupalUrl, updatedLink, handledError);
            } else {
                apiDrupalId = updatedLink.getDrupal_api_id();
                infoApi = utility.getApiNode(Site.DRUPAL_ADMIN_USER, Site.DRUPAL_ADMIN_PASSWORD, portalDrupalUrl,
                        apiDrupalId, handledError);
                utility.updateApiNode(site, Site.DRUPAL_ADMIN_USER, Site.DRUPAL_ADMIN_PASSWORD,
                        updatedLink.getRestApi(), portalDrupalUrl, updatedLink, handledError);
            }

            if (infoApi.isEmpty()) {
                log.error("Error when create api node");
                log.error(handledError.getErrorMsg());
                continue;
            }

            if (listJsonApi.isEmpty()) {
                listJsonApi = listJsonApi + infoApi;
            } else {
                listJsonApi = listJsonApi + "," + infoApi;
            }
        }

        for (DrupalLink createdLink : drupalNew) {
            String apiDrupalId, infoApi;
            infoApi = utility.createApiNode(site, Site.DRUPAL_ADMIN_USER, Site.DRUPAL_ADMIN_PASSWORD,
                    createdLink.getRestApi(), portalDrupalUrl, createdLink, handledError);
            if (infoApi.isEmpty()) {
                log.error("Error when create api node");
                log.error(handledError.getErrorMsg());
                continue;
            }

            if (listJsonApi.isEmpty()) {
                listJsonApi = listJsonApi + infoApi;
            } else {
                listJsonApi = listJsonApi + "," + infoApi;
            }
        }

        for (DrupalLink deletedLink : drupalDelete) {
            Utility.deleteNode(Site.DRUPAL_ADMIN_USER, Site.DRUPAL_ADMIN_PASSWORD, portalDrupalUrl,
                    deletedLink.getDrupal_api_id(), handledError);
            dataManager.remove(deletedLink);
        }

        if (!listJsonApi.isEmpty() && drupalProductId != 0) {
            gotError = Utility.updateProductNode(Site.DRUPAL_ADMIN_USER, Site.DRUPAL_ADMIN_PASSWORD, portalDrupalUrl, listJsonApi, (String.valueOf(drupalProductId)), publishedProduct.getProduct(), handledError);
        }

        if (gotError) {
            updateError.setErrorMsg(handledError.getErrorMsg());
            updateError.setHttpResponseCode(handledError.getHttpResponseCode());
            return -1;
        }

        //Set object Ids in DB
        int intLen = kongObjsId.length();
        if (intLen < 3) {
            updateError.setErrorMsg("Kong object id in wrong format");
            return -6;
        }
        if (kongOAuthInfo.length() < 3) {
            updateError.setErrorMsg("Kong object info in wrong format");
            return -6;
        }
        String latsChar = kongObjsId.substring(intLen - 2, intLen - 1);
        if (latsChar.equals(" ")) {
            kongObjsId.substring(0, intLen - 1);
        }

        latsChar = kongOAuthInfo.substring(kongOAuthInfo.length() - 2, kongOAuthInfo.length() - 1);
        if (latsChar.equals(" ")) {
            kongOAuthInfo.substring(0, kongOAuthInfo.length() - 1);
        }

        latsChar = kongMappingInfo.substring(kongMappingInfo.length() - 2, kongMappingInfo.length() - 1);
        if (latsChar.equals(" ")) {
            kongMappingInfo.substring(0, kongMappingInfo.length() - 1);
        }

        try {
            publishedProduct.setKong_object_ids(kongObjsId);
            publishedProduct.setKong_oauth_info(kongOAuthInfo);
            publishedProduct.setKong_mapping_info(kongMappingInfo);
            dataManager.save(publishedProduct);
            Map<String, String> listApi = Arrays.stream(kongMappingInfo.split(" ")).collect(Collectors.toMap(
                    tuple -> (tuple.split(";")[1]),
                    tuple -> (tuple.split(";")[0]))
            );

            for (DrupalLink drupalLink : drupalLinks) {
                if (listApi.containsKey(drupalLink.getRestApi().getId().toString())) {
                    drupalLink.setKong_api_id(listApi.get(drupalLink.getRestApi().getId().toString()));
                    dataManager.save(drupalLink);
                }
            }

            updateError.setErrorMsg("");
        } catch (Exception e) {
            updateError.setErrorMsg(e.getMessage());
            retInt = -7;
        }
        return retInt;
    }

    private void  getKongObjId(PublishedProduct publishedProduct,
                              KongServiceObjIds kongServiceObjIds, HandledError handledError) {
        String kongObjIds = publishedProduct.getKong_object_ids();
        if (kongObjIds == null) {
            handledError.setErrorMsg("No API information");
            return;
        }

        String kongOAuthInfo = publishedProduct.getKong_oauth_info();
        String mappingInfo = publishedProduct.getKong_mapping_info();
        String[] kongApis = kongObjIds.split(" ");

        String[] routeIdList, servicesIdList, aclIdList;
        String[] provisionKeyList = null;
        if (mappingInfo == null) {
            handledError.setErrorMsg("No API information");
            return;
        }
        String[] mappingIds = mappingInfo.split(" ");
        if (kongApis.length <= 0) {
            handledError.setErrorMsg("No API information");
            return;
        }
        String[] kongOAuths = null;
        if (kongOAuthInfo != null) {
            kongOAuths = kongOAuthInfo.split(" ");
            provisionKeyList = new String[kongOAuths.length];
        }

        routeIdList = new String[kongApis.length];
        aclIdList = new String[kongApis.length];
        servicesIdList = new String[kongApis.length];
        String[] restApiIdList = new String[mappingIds.length];

        if (kongOAuths != null) {
            if (kongApis.length != kongOAuths.length) {
                handledError.setErrorMsg("Kong Ids and OAuth info not consolidated");
                return;
            }
        }

        for (int i = 0; i < mappingIds.length; i++) {
            String idList[] = mappingIds[i].split(";");
            if (idList.length != 2) {
                handledError.setErrorMsg("Mapping info uncorrect");
                return;
            }
            restApiIdList[i] = idList[1];
        }

        boolean detectIdGotError = false;
        for (int i = 0; i < kongApis.length; i++) {
            String[] idList = kongApis[i].split(";");
            if (idList.length != 3) {
                detectIdGotError = true;
                break;
            } else {
                servicesIdList[i] = idList[0];
                routeIdList[i] = idList[1];
                aclIdList[i] = idList[2];
            }
            if ((kongOAuths != null) && (provisionKeyList != null)) {
                String[] infoList = kongOAuths[i].split(";");
                if (infoList.length != 2) {
                    detectIdGotError = true;
                    break;
                } else {
                    provisionKeyList[i] = infoList[0];
                }
            }
        }

        if (detectIdGotError) {
            handledError.setErrorMsg("API information in wrong format");
            return;
        }
        handledError.setErrorMsg("");
        kongServiceObjIds.setServicesIdList(servicesIdList);
        kongServiceObjIds.setRoutesIdList(routeIdList);
        kongServiceObjIds.setAclsIdList(aclIdList);
        kongServiceObjIds.setRestApiList(restApiIdList);
        if (kongOAuths != null) {
            kongServiceObjIds.setProvisionKeyList(provisionKeyList);
        }
    }

    @Autowired
    private DataContext dataContext;

    @Subscribe("manageApiBtn")
    public void onManageApiBtnClick(io.jmix.ui.component.Button.ClickEvent event) {
        PublishedProduct publishedProduct = publishedProductsTable.getSingleSelected();

        if (publishedProduct == null) {
            return;
        }
        if ((!publishedProduct.getState().equals("PUBLISHED")) && (!publishedProduct.getState().equals("DEPRECATED"))) {
            return;
        }

        String invocationEndpoint = null;
        String managementEndpoint = null;
        if (site.getSelf_hosted_gateway_used()) {
            SelfHostedGateway selfHostedGateway = site.getSelf_hosted_gateway();
            if (selfHostedGateway != null) {
                invocationEndpoint = selfHostedGateway.getInvocation_endpoint();
                managementEndpoint = selfHostedGateway.getManagement_endpoint();
            }
        } else {
            GatewayService gatewayService = site.getGateway();
            if (gatewayService != null) {
                invocationEndpoint = gatewayService.getInvocation_endpoint();
                managementEndpoint = gatewayService.getManagement_endpoint();
            }
        }
        if (invocationEndpoint != null) {
            if (invocationEndpoint.length() > 1) {
                String endChar = invocationEndpoint.substring(invocationEndpoint.length() - 1);
                if (invocationEndpoint.substring(invocationEndpoint.length() - 1).equals("/")) {
                    invocationEndpoint = invocationEndpoint.substring(0, invocationEndpoint.length() - 2);
                }
            } else {
                invocationEndpoint = "";
            }
        } else {
            invocationEndpoint = "";
        }

        KongServiceObjIds kongServiceObjIds = new KongServiceObjIds();
        HandledError getIdsError = new HandledError();
        this.getKongObjId(publishedProduct, kongServiceObjIds, getIdsError);
        if (!getIdsError.getErrorMsg().equals("")) {
            notifications.create(Notifications.NotificationType.ERROR).
                    withCaption("Could not get API info from API Manager:" +
                            getIdsError.getErrorMsg()).show();
            return;
        }
        if (managementEndpoint == null) {
            notifications.create(Notifications.NotificationType.ERROR).
                    withCaption("Could not get management endpoint of the gateway");
            return;
        }

        String[] servicesIdList = kongServiceObjIds.getServicesIdList();
        String[] routeIdList = kongServiceObjIds.getRoutesIdList();
        String[] aclIdList = kongServiceObjIds.getAclsIdList();
        String[] provisionKeyList = kongServiceObjIds.getProvisionKeyList();
        String[] restApiIdList = kongServiceObjIds.getRestApiList();

        KongConnector kongConnector = new KongConnector(managementEndpoint);
        HandledError handledError = new HandledError();
        boolean checkApiGotError = false;
        KongServiceDetails[] kongServiceDetailsList = new KongServiceDetails[servicesIdList.length];
        KongRouteDetails[] routeDetails = new KongRouteDetails[routeIdList.length];
        //KongServicePlugin[] plugins = new KongServicePlugin[servicesIdList.length];

//        SaveContext saveContext = new SaveContext().setDiscardSaved(true);
//        String consentPageUrl0 = utility.getAppConfigValue("consentPageUrl");
        for (int i = 0; i < servicesIdList.length; i++) {
            kongServiceDetailsList[i] = kongConnector.getServicebyName(servicesIdList[i], handledError);
            routeDetails[i] = kongConnector.getRoutebyName(routeIdList[i], handledError);
            if ((kongServiceDetailsList[i] == null) || (routeDetails[i] == null)) {
                checkApiGotError = true;
                break;
            }
//            PublishedRestApi restApi = dataManager.load(PublishedRestApi.class)
//                    .condition(PropertyCondition.equal("kongServiceId", UUID
//                                    .fromString(kongServiceDetailsList[i].getId())))
//                    .optional()
//                    .orElse(dataManager.create(PublishedRestApi.class));
//
//            restApi.setName(kongServiceDetailsList[i].getName());
//
//
//            restApi.setConsent_url(consentPageUrl0 + "?product=" + publishedProduct.getId().toString()
//                    + "&service=" + kongServiceDetailsList[i].getId() +
//                    "&site=" + site.getId().toString() + "&api=" + restApi.getPath());
//            restApi.setOauthProvisionKey(provisionKeyList[i]);
//
//            restApi.setPlugin(kongConnector.getRawServicePlugins(servicesIdList[i], handledError));
//            restApi.setKongServiceId(UUID
//                    .fromString(kongServiceDetailsList[i].getId()));
//            restApi.setHost(kongServiceDetailsList[i].getHost());
//            restApi.setProduct(publishedProduct);
//            saveContext.saving(restApi);

            /*
            plugins[i] = kongConnector.getServicePluginByType
                    (servicesIdList[i], "oauth2", handledError);
             */
        }
//        dataManager.save(saveContext);
        if (checkApiGotError) {
            notifications.create(Notifications.NotificationType.ERROR).
                    withCaption("Could not get API info from Gateway:" +
                            handledError.getErrorMsg())
                    .show();
            return;
        }
        List<ApiInSite> apiInSiteList = new ArrayList<ApiInSite>();
        for (int i = 0; i < kongServiceDetailsList.length; i++) {
            ApiInSite apiInSite = new ApiInSite();
            apiInSite.setRestApiId(restApiIdList[i]);
            apiInSite.setApi_id(kongServiceDetailsList[i].getId());
            apiInSite.setName(kongServiceDetailsList[i].getName());
            if ( kongServiceDetailsList[i].getPath() != null) {
                apiInSite.setHost(kongServiceDetailsList[i].getHost() + kongServiceDetailsList[i].getPath());
            }
            else {
                apiInSite.setHost(kongServiceDetailsList[i].getHost());
            }
            apiInSite.setSite_id(site.getId().toString());


            //apiInSite.setPath(kongServiceDetailsList[i].getPath());

            String routePath = routeDetails[i].getPaths()[0];
            String apiEndpointDomainNameAlias = utility.getAppConfigValue("apiEndpointDomainNameAlias");
            String apiProtocol = utility.getAppConfigValue("apiEndpointAliasProtocol");
            log.info("Alias domain: " + apiEndpointDomainNameAlias);
            log.info("Alias protocol: " + apiProtocol);

            if ((!apiEndpointDomainNameAlias.toLowerCase().equals("")) && (!apiEndpointDomainNameAlias.toLowerCase().equals("null"))) {
                //String aliasDomainName = "apim.fptcloud.com";
                String[] components = routePath.split("/");
                boolean aliasDomainUsed = false;
                if (components != null) {
                    if (components.length > 3) {
                        aliasDomainUsed = true;
                        String newRoutePath = "";
                        for (int iC = 3; iC < components.length; iC++) {
                            newRoutePath = newRoutePath + "/" + components[iC];
                        }
                        apiInSite.setApi_url(apiProtocol + components[1] + "-" + components[2] + "."
                                + apiEndpointDomainNameAlias + newRoutePath);
                        int a = 0;
                    }
                }
                if (!aliasDomainUsed) {
                    apiInSite.setApi_url(invocationEndpoint + routePath);
                }
            } else {
                apiInSite.setApi_url(invocationEndpoint + routePath);
            }

            if (provisionKeyList != null) {
                if (provisionKeyList[i] != null) {
                    if (!provisionKeyList[i].isEmpty()) {
                        String consentPageUrl = utility.getAppConfigValue("consentPageUrl");
                        apiInSite.setConsent_url(consentPageUrl + "?product=" + publishedProduct.getId().toString()
                                + "&service=" + kongServiceDetailsList[i].getId() +
                                "&site=" + site.getId().toString() + "&api=" + apiInSite.getApi_url());
                        apiInSite.setOauthProvisionKey(provisionKeyList[i]);
                    }
                }
            }

            apiInSite.setRoute_id(routeIdList[i]);
            apiInSite.setAcl_id(aclIdList[i]);
            apiInSiteList.add(apiInSite);
        }

        ApiInSiteBrowse apiInSiteBrowse = screens.create(ApiInSiteBrowse.class);
        apiInSiteBrowse.setApiInSiteList(apiInSiteList);
        apiInSiteBrowse.setSite(site);
        apiInSiteBrowse.setPublishedProduct(publishedProduct);
        apiInSiteBrowse.show();
    }

    @Subscribe("manageApiBrowserBtn")
    public void onManageApiBrowserBtnClick(io.jmix.ui.component.Button.ClickEvent event) {
        PublishedProduct publishedProduct = publishedProductsTable.getSingleSelected();

        String invocationEndpoint = null;
        String managementEndpoint = null;
        if (site.getSelf_hosted_gateway_used()) {
            SelfHostedGateway selfHostedGateway = site.getSelf_hosted_gateway();
            if (selfHostedGateway != null) {
                invocationEndpoint = selfHostedGateway.getInvocation_endpoint();
                managementEndpoint = selfHostedGateway.getManagement_endpoint();
            }
        } else {
            GatewayService gatewayService = site.getGateway();
            if (gatewayService != null) {
                invocationEndpoint = gatewayService.getInvocation_endpoint();
                managementEndpoint = gatewayService.getManagement_endpoint();
            }
        }
        if (invocationEndpoint != null) {
            if (invocationEndpoint.length() > 1) {
                String endChar = invocationEndpoint.substring(invocationEndpoint.length() - 1);
                if (invocationEndpoint.substring(invocationEndpoint.length() - 1).equals("/")) {
                    invocationEndpoint = invocationEndpoint.substring(0, invocationEndpoint.length() - 2);
                }
            } else {
                invocationEndpoint = "";
            }
        } else {
            invocationEndpoint = "";
        }


        KongServiceObjIds kongServiceObjIds = new KongServiceObjIds();
        HandledError getIdsError = new HandledError();
        this.getKongObjId(publishedProduct, kongServiceObjIds, getIdsError);
        if (!getIdsError.getErrorMsg().equals("")) {
            notifications.create(Notifications.NotificationType.ERROR).
                    withCaption("Could not get API info from API Manager:" +
                            getIdsError.getErrorMsg()).show();
            return;
        }
        if (managementEndpoint == null) {
            notifications.create(Notifications.NotificationType.ERROR).
                    withCaption("Could not get management endpoint of the gateway");
            return;
        }

        String[] servicesIdList = kongServiceObjIds.getServicesIdList();
        String[] provisionKeyList = kongServiceObjIds.getProvisionKeyList();
        KongServiceDetails[] kongServiceDetailsList = new KongServiceDetails[servicesIdList.length];
        KongConnector kongConnector = new KongConnector(managementEndpoint);
        SaveContext saveContext = new SaveContext().setDiscardSaved(true);
//        dataManager.remove();
        HandledError handledError = new HandledError();
        String consentPageUrl0 = utility.getAppConfigValue("consentPageUrl");

        List<PublishedRestApi> removeList = dataManager.load(PublishedRestApi.class)
                .query("select e from PublishedRestApi e where  e.product = :product")
                .parameter("product", publishedProduct)
                .list();
        for (int i = 0; i < servicesIdList.length; i++) {
            kongServiceDetailsList[i] = kongConnector.getServicebyName(servicesIdList[i], handledError);
//            routeDetails[i] = kongConnector.getRoutebyName(routeIdList[i], handledError);
            if ((kongServiceDetailsList[i] == null)) {
                break;
            }
            PublishedRestApi restApi = dataManager.load(PublishedRestApi.class)
                    .condition(PropertyCondition.equal("kongServiceId", UUID
                            .fromString(kongServiceDetailsList[i].getId())))
                    .optional()
                    .orElse(dataManager.create(PublishedRestApi.class));
            removeList.remove(restApi);
            restApi.setName(kongServiceDetailsList[i].getName());


            restApi.setConsent_url(consentPageUrl0 + "?product=" + publishedProduct.getId().toString()
                    + "&service=" + kongServiceDetailsList[i].getId() +
                    "&site=" + site.getId().toString() + "&api=" + restApi.getPath());
            restApi.setOauthProvisionKey(provisionKeyList[i]);
            restApi.setPath(kongServiceDetailsList[i].getPath());
            restApi.setPlugin(kongConnector.getRawServicePlugins(servicesIdList[i], handledError));
            restApi.setKongServiceId(UUID
                    .fromString(kongServiceDetailsList[i].getId()));
            restApi.setHost(kongServiceDetailsList[i].getHost());
            restApi.setProduct(publishedProduct);
            saveContext.saving(restApi);
        }
        dataManager.save(saveContext);
        dataManager.remove(removeList);
        PublishedRestApiBrowse publishedRestApiBrowse = screens.create(PublishedRestApiBrowse.class);
        publishedRestApiBrowse.setInitParameter(publishedProductsTable.getSingleSelected());
        publishedRestApiBrowse.show();
    }


    @Subscribe("retireBtn")
    public void onRetireBtnClick(io.jmix.ui.component.Button.ClickEvent event) {
        dialogs.createOptionDialog()
                .withCaption("Confirmation")
                .withMessage("Are you sure to retire this product?")
                .withActions(
                        new DialogAction(DialogAction.Type.OK)
                                .withHandler(e ->
                                        this.handeRetireProduct()
                                ),
                        new DialogAction(DialogAction.Type.CANCEL))
                .show();
    }

    private void handeRetireProduct() {
        boolean kongGWAction = true;

        PublishedProduct publishedProduct = publishedProductsTable.getSingleSelected();
        KongConnector kongConnector = null;

        try {
            /*publishedProductsTableRemove.setConfirmationMessage("Are you sure to unpublish "
                    + product.getName() + " from the site named " + publishedProduct.getSite().getName());

             */
            Product product = publishedProduct.getProduct();
            Site site = publishedProduct.getSite();
            String thatGroup = site.getOwner().getUsername() + "."
                    + site.getName() + "." + product.getName();

            String managementEndpoint = null;
            if (site.getSelf_hosted_gateway_used()) {
                SelfHostedGateway selfHostedGateway = site.getSelf_hosted_gateway();
                if (selfHostedGateway != null) {
                    managementEndpoint = selfHostedGateway.getManagement_endpoint();
                }
            } else {
                GatewayService gatewayService = site.getGateway();
                if (gatewayService != null) {
                    managementEndpoint = gatewayService.getManagement_endpoint();
                }
            }
            if (managementEndpoint == null) {
                notifications.create(Notifications.NotificationType.ERROR).
                        withCaption("Could not get management endpoint of the gateway");
                return;
            }
            kongConnector = new KongConnector(managementEndpoint);

            String servicesId = "";

            String kongObjIds = publishedProduct.getKong_object_ids();
            if (kongObjIds == null) {
                throw new RuntimeException("Kong object info nof found!");
            }
            String[] kongApis = kongObjIds.split(" ");
            String[] routeIdList, servicesIdList, aclIdList;
            if (kongApis.length <= 0) {
                //No Kong service/route/acl info; Do nothing;
            } else {
                servicesIdList = new String[kongApis.length];
                routeIdList = new String[kongApis.length];
                aclIdList = new String[kongApis.length];
                servicesIdList = new String[kongApis.length];
                for (int i = 0; i < kongApis.length; i++) {
                    String[] idList = kongApis[i].split(";");
                    if (idList.length != 3) {
                        throw new RuntimeException("Kong object info in wrong format!");
                    } else {
                        servicesIdList[i] = idList[0];
                        routeIdList[i] = idList[1];
                        aclIdList[i] = idList[2];
                    }
                }
                HandledError handledError = new HandledError();
                boolean gotError = false;
                String errMsg = "";
                for (int i = 0; i < aclIdList.length; i++) {
                    if (kongConnector.deleteWholeService(servicesIdList[i], routeIdList[i],
                            aclIdList[i], thatGroup, handledError) != 0) {
                        gotError = true;
                        break;
                    }
                }
                if (gotError) {
                    notifications.create(Notifications.NotificationType.ERROR).
                            withCaption(handledError.getErrorMsg()).show();
                    kongGWAction = false;
                }
            }
        } catch (Exception e) {
            notifications.create(Notifications.NotificationType.ERROR).withCaption(e.getMessage()).show();
            kongGWAction = false;
        }
        if (kongGWAction) {
            try {
                this.removeSubscription(publishedProduct, kongConnector);
                publishedProduct.setState("RETIRED");
                publishedProduct.setKong_object_ids("");
                dataManager.save(publishedProduct);
                publishedProductsDl.load();
                setComponents();
            } catch (Exception e) {
                notifications.create(Notifications.NotificationType.ERROR).withCaption(e.getMessage()).show();
            }
        }
    }

    private void removeSubscription(PublishedProduct publishedProduct, KongConnector kongConnector) {
        if (kongConnector == null) {
            return;
        }
        List<ProductSubscription> subscriptionList =
                dataManager.load(ProductSubscription.class)
                        .query("select e from ProductSubscription e " +
                                "where e.published_product = :product")
                        .parameter("product", publishedProduct).list();
        if (!subscriptionList.isEmpty()) {
            for (int i = 0; i < subscriptionList.size(); i++) {
                Consumer consumer = subscriptionList.get(i).getConsumer();
                String kongConsumerName = consumer.getProvider().getUsername().toLowerCase()
                        + "." + site.getName().toLowerCase()
                        + "." + consumer.getName().toLowerCase();
                HandledError handledError = new HandledError();
                if (kongConnector.unJoinConsumerFromGroup(kongConsumerName, subscriptionList.get(i).getKong_consumer_group_id(),
                        handledError) != 204) {
                    notifications.create(Notifications.NotificationType.ERROR).withCaption("Error when unsubscribe the consumer "
                                    + kongConsumerName + ": " + handledError.getErrorMsg())
                            .show();
                    break;
                }
            }

            dataManager.remove(subscriptionList);
        }
    }

    @Subscribe("archiveBtn")
    public void onArchiveBtnClick(io.jmix.ui.component.Button.ClickEvent event) {
        dialogs.createOptionDialog()
                .withCaption("Confirmation")
                .withMessage("Are you sure to archive this product?")
                .withActions(
                        new DialogAction(DialogAction.Type.OK)
                                .withHandler(e ->
                                        this.handleArchiveProduct()
                                ),
                        new DialogAction(DialogAction.Type.CANCEL))
                .show();
    }

    private void handleArchiveProduct() {
        try {
            PublishedProduct publishedProduct = publishedProductsTable.getSingleSelected();
            publishedProduct.setState("ARCHIVED");
            dataManager.save(publishedProduct);
            publishedProductsDl.load();
            setComponents();
        } catch (Exception e) {

        }
    }

    @Subscribe("unStagingBtn")
    public void onUnStagingBtnClick(io.jmix.ui.component.Button.ClickEvent event) {

//        PublishedProduct publishedProduct = publishedProductsTable.getSingleSelected();
//        assert publishedProduct != null;
//        dialogs.createOptionDialog()
//                .withCaption("Confirmation")
//                .withMessage("Are you sure to remove that " +
//                    "product from the Site named " + publishedProduct.getSite().getName())
//                .withActions(
//                        new DialogAction(DialogAction.Type.OK)
//                                .withHandler(e ->
//                                        {
//                                            try {
//                                                this.handleRemoveProduct(publishedProduct);
//                                            } catch (Exception ex) {
//                                                log.error("Error when unstaging product");
//                                                log.error(ex.getMessage());
//                                                notifications.create(Notifications.NotificationType.ERROR).withCaption(ex.getMessage());
//                                            }
//                                        }
//                                ),
//                        new DialogAction(DialogAction.Type.CANCEL))
//                .show();
        try {
            PublishedProduct publishedProduct = publishedProductsTable.getSingleSelected();
            assert publishedProduct != null;
            List<DrupalLink> drupalLinks = dataManager.load(DrupalLink.class)
                    .query("select d from DrupalLink d where d.publishedProduct=:publishedProduct")
                    .parameter("publishedProduct", publishedProduct)
                    .list();
            publishedProductsTableRemove.setConfirmationMessage("Are you sure to remove that " +
                    "product from the Site named " + publishedProduct.getSite().getName());

            dataManager.remove(drupalLinks);
            publishedProductsTableRemove.execute();
        } catch (Exception e) {
            log.error("Error when unstaging product");
            log.error(e.getMessage());
            notifications.create(Notifications.NotificationType.ERROR).withCaption(e.getMessage());
        }
    }
    public void handleRemoveProduct(PublishedProduct publishedProduct) {
            List<DrupalLink> drupalLinks = dataManager.load(DrupalLink.class)
                    .query("select d from DrupalLink d where d.publishedProduct=:publishedProduct")
                    .parameter("publishedProduct", publishedProduct)
                    .list();

            dataManager.remove(drupalLinks);
            dataManager.remove(publishedProduct);
            publishedProductsDl.load();
    }

    @Subscribe("updateBtn")
    public void onUpdateBtnClick(io.jmix.ui.component.Button.ClickEvent event) {
        PublishedProduct publishedProduct = null;
        boolean gotError = false;
        try {
            publishedProduct = publishedProductsTable.getSingleSelected();
            if (!publishedProduct.getState().equals("PUBLISHED")) {
                gotError = true;
            } else {
                if (publishedProduct.getSite().getProduction_mode()) {
                    gotError = true;
                }
            }
        } catch (Exception e) {
            gotError = true;
            publishedProduct = null;
        }
        if ((gotError) || (publishedProduct == null)) {
            return;
        }
        HandledError handledError = new HandledError();
        if (updateGatewayServices(handledError) != 0) {
            notifications.create(Notifications.NotificationType.ERROR)
                    .withCaption(handledError.getErrorMsg()).show();
        } else {
            notifications.create(Notifications.NotificationType.HUMANIZED)
                    .withCaption("Update successful").show();
        }
    }

    @Subscribe("closeBtn")
    public void onCloseBtnClick(io.jmix.ui.component.Button.ClickEvent event) {
        closeWithDefaultAction();
    }
}