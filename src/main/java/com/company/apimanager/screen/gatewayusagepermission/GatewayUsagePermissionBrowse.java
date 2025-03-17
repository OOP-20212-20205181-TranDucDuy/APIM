package com.company.apimanager.screen.gatewayusagepermission;

import com.company.apimanager.entity.User;
import io.jmix.core.security.CurrentAuthentication;
import io.jmix.ui.component.Label;
import io.jmix.ui.model.CollectionLoader;
import io.jmix.ui.screen.*;
import com.company.apimanager.entity.GatewayUsagePermission;
import org.springframework.beans.factory.annotation.Autowired;

@UiController("GatewayUsagePermission.browse")
@UiDescriptor("gateway-usage-permission-browse.xml")
@LookupComponent("gatewayUsagePermissionsTable")
public class GatewayUsagePermissionBrowse extends StandardLookup<GatewayUsagePermission> {
    @Autowired
    private CollectionLoader<GatewayUsagePermission> gatewayUsagePermissionsDl;

    private User user;
    @Autowired
    private CurrentAuthentication currentAuthentication;
    @Autowired
    private Label tileLabel;

    public void setUser(User user) {
        this.user = user;
        gatewayUsagePermissionsDl.setParameter("user", user);
    }

    public User getUser() {
        return user;
    }

    @Subscribe
    public void onBeforeShow(BeforeShowEvent event) {
        String value = "<b><font size=\"3\">"
                + "Gateways assigned to " + user.getUsername()
                + "</font><b/>";
        tileLabel.setHtmlEnabled(true);
        tileLabel.setHtmlSanitizerEnabled(true);
        tileLabel.setValue(value);

    }

    @Install(to = "gatewayUsagePermissionsTable.create", subject = "initializer")
    private void gatewayUsagePermissionsTableCreateInitializer(GatewayUsagePermission gatewayUsagePermission) {
        gatewayUsagePermission.setApi_provider(user);
    }

    @Subscribe
    public void onInit(InitEvent event) {
    }
}