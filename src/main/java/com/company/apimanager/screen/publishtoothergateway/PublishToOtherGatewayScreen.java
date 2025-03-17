package com.company.apimanager.screen.publishtoothergateway;

import com.company.apimanager.entity.GatewayService;
import com.company.apimanager.entity.Product;
import com.company.apimanager.entity.Site;
import com.company.apimanager.entity.SiteGateway;
import com.company.apimanager.screen.brandlogin.ApiRegister;
import io.jmix.core.DataManager;
import io.jmix.ui.Notifications;
import io.jmix.ui.component.Button;
import io.jmix.ui.component.EntityComboBox;
import io.jmix.ui.model.CollectionLoader;
import io.jmix.ui.screen.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@UiController("PublishToOtherGatewayScreen")
@UiDescriptor("publish-to-other-gateway-screen.xml")
public class PublishToOtherGatewayScreen extends Screen {
    private Site site;
    private GatewayService defaultGateway;
    private Product publishProduct;

    @Autowired
    private Notifications notifications;
    @Autowired
    private CollectionLoader<SiteGateway> siteGatewaysDl;
    @Autowired
    private DataManager dataManager;
    @Autowired
    private EntityComboBox<SiteGateway> gatewayComboBox;

    public void setMember(Site site, GatewayService defaultGateway, Product publishProduct) {
        this.site = site;
        this.defaultGateway = defaultGateway;
        this.publishProduct = publishProduct;
        siteGatewaysDl.setParameter("site", site);
        siteGatewaysDl.setParameter("default_gateway", defaultGateway);
    }

    public void setSite(Site site) {
        this.site = site;
    }

    public void setDefaultGateway(GatewayService defaultGateway) {
        this.defaultGateway = defaultGateway;
    }

    @Subscribe("closeBtn")
    public void onCloseBtnClick(Button.ClickEvent event) {
        close(StandardOutcome.CLOSE);
    }

    @Subscribe("publishAndCloseBtn")
    public void onPublishAndCloseBtnClick(Button.ClickEvent event) {
        GatewayIntefrace gatewayIntefrace = new GatewayIntefrace(site);
        GatewayService gatewayService = gatewayComboBox.getValue().getGateway();

        List<ApiRegister> apiRegisters = dataManager.load(ApiRegister.class).query("select e from ApiRegister e where e.product = :product")
                .parameter("product",publishProduct).list();
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

        close(StandardOutcome.CLOSE);
    }
}