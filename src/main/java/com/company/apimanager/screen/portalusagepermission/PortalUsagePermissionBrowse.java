package com.company.apimanager.screen.portalusagepermission;

import com.company.apimanager.entity.User;
import io.jmix.core.security.CurrentAuthentication;
import io.jmix.ui.component.Label;
import io.jmix.ui.model.CollectionLoader;
import io.jmix.ui.screen.*;
import com.company.apimanager.entity.PortalUsagePermission;
import org.springframework.beans.factory.annotation.Autowired;

@UiController("PortalUsagePermission.browse")
@UiDescriptor("portal-usage-permission-browse.xml")
@LookupComponent("portalUsagePermissionsTable")
public class PortalUsagePermissionBrowse extends StandardLookup<PortalUsagePermission> {

    private User user;
    @Autowired
    private CurrentAuthentication currentAuthentication;
    @Autowired
    private CollectionLoader<PortalUsagePermission> portalUsagePermissionsDl;
    @Autowired
    private Label tileLabel;

    public void setUser(User user) {
        this.user = user;
        portalUsagePermissionsDl.setParameter("user", user);
    }

    public User getUser() {
        return user;
    }

    @Install(to = "portalUsagePermissionsTable.create", subject = "initializer")
    private void portalUsagePermissionsTableCreateInitializer(PortalUsagePermission portalUsagePermission) {
        portalUsagePermission.setApi_provider(user);
    }

    @Subscribe
    public void onBeforeShow(BeforeShowEvent event) {
        String value = "<b><font size=\"3\">"
                + "Portals assigned to " + user.getUsername()
                + "</font><b/>";
        tileLabel.setHtmlEnabled(true);
        tileLabel.setHtmlSanitizerEnabled(true);
        tileLabel.setValue(value);
    }
}