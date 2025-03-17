package com.company.apimanager.screen.homepage;

import com.company.apimanager.entity.Product;
import com.company.apimanager.entity.RestApi;
import com.company.apimanager.entity.User;
import com.company.apimanager.screen.analyticsservice.AnalyticsServiceBrowse;
import com.company.apimanager.screen.apiregister.ApiRegisterBrowse;
import com.company.apimanager.screen.gatewaytype.GatewayTypeBrowse;
import com.company.apimanager.screen.product.ProductBrowse;
import com.company.apimanager.screen.report.ReportScreen;
import com.company.apimanager.screen.restapi.RestApiBrowse;
import com.company.apimanager.screen.site.SiteBrowse;
import com.company.apimanager.screen.user.UserBrowse;
import io.jmix.core.security.CurrentAuthentication;
import io.jmix.ui.Screens;
import io.jmix.ui.action.Action;
import io.jmix.ui.component.Button;
import io.jmix.ui.component.CssLayout;
import io.jmix.ui.component.LinkButton;
import io.jmix.ui.screen.Screen;
import io.jmix.ui.screen.Subscribe;
import io.jmix.ui.screen.UiController;
import io.jmix.ui.screen.UiDescriptor;
import org.springframework.beans.factory.annotation.Autowired;

@UiController("Homepage")
@UiDescriptor("homepage.xml")
public class Homepage extends Screen {
    @Autowired
    private Screens screens;
    @Autowired
    private CurrentAuthentication currentAuthentication;
    @Autowired
    private LinkButton productButton;
    @Autowired
    private LinkButton catalogButton;
    @Autowired
    private LinkButton gatewayButton;
    @Autowired
    private LinkButton analyticsButton;
    @Autowired
    private LinkButton userButton;
    @Autowired
    private LinkButton restApiButton;
    @Autowired
    private CssLayout productCard;
    @Autowired
    private CssLayout catalogCard;
    @Autowired
    private CssLayout analyticsCard;
    @Autowired
    private CssLayout restApiCard;
    @Autowired
    private CssLayout gatewayCard;
    @Autowired
    private CssLayout userCard;

    @Subscribe("productButton")
    public void onProductButton(Button.ClickEvent event) {
            ProductBrowse productBrowser = screens.create(ProductBrowse.class);
            productBrowser.show();
    }

    @Subscribe("catalogButton")
    public void onCatalogButton(Button.ClickEvent event) {
        SiteBrowse siteBrowse = screens.create(SiteBrowse.class);
        siteBrowse.show();
    }

    @Subscribe("gatewayButton")
    public void onGatewayButton(Button.ClickEvent event) {
        GatewayTypeBrowse gatewayTypeBrowse = screens.create(GatewayTypeBrowse.class);
        gatewayTypeBrowse.show();
    }

    @Subscribe("analyticsButton")
    public void onAnalyticsButton(Button.ClickEvent event) {
        ReportScreen reportScreen = screens.create(ReportScreen.class);
        reportScreen.show();
    }

    @Subscribe("userButton")
    public void onUserButton(Button.ClickEvent event) {
        UserBrowse userBrowse = screens.create(UserBrowse.class);
        userBrowse.show();
    }

    @Subscribe("restApiButton")
    public void onRestApiButton(Button.ClickEvent event) {
        RestApiBrowse restApiBrowse = screens.create(RestApiBrowse.class);
        restApiBrowse.show();
    }

    @Subscribe
    public void onInit(InitEvent event) {
        User currUser = (User) currentAuthentication.getUser();
        if (currentAuthentication.isSet()) {
            if (currUser.getAuthorities().stream()
                    .anyMatch(authority -> authority.getAuthority().equals("topo_admin") ||
                            authority.getAuthority().equals("topology-administrator-code"))) {
                productCard.setVisible(false);
                catalogCard.setVisible(false);
                gatewayCard.setVisible(false);
                analyticsCard.setVisible(false);
                userCard.setVisible(false);
                restApiCard.setVisible(false);

            }
            else if (currentAuthentication.getUser().getAuthorities().stream()
                    .anyMatch(authority -> authority.getAuthority().equals("api_provider_owner")
                            || authority.getAuthority().equals("apim-provider-owner-code"))) {
                userCard.setVisible(false);
                gatewayCard.setVisible(false);
            }
        }
    }
}
