package com.company.apimanager.screen.portalusagepermission;

import com.company.apimanager.entity.PortalService;
import io.jmix.ui.Notifications;
import io.jmix.ui.component.EntityPicker;
import io.jmix.ui.screen.*;
import com.company.apimanager.entity.PortalUsagePermission;
import org.springframework.beans.factory.annotation.Autowired;

@UiController("PortalUsagePermission.edit")
@UiDescriptor("portal-usage-permission-edit.xml")
@EditedEntityContainer("portalUsagePermissionDc")
public class PortalUsagePermissionEdit extends StandardEditor<PortalUsagePermission> {
    @Autowired
    private EntityPicker<PortalService> portalField;
    @Autowired
    private Notifications notifications;

    @Subscribe
    public void onBeforeCommitChanges(BeforeCommitChangesEvent event) {
        PortalService portalService = portalField.getValue();
        if (portalService == null) {
            event.preventCommit();
        }
        if (!portalService.getVirtualArea().getIs_active()) {
            notifications.create(Notifications.NotificationType.ERROR)
                    .withCaption("That portal service in an inactive virtual area").show();
            event.preventCommit();
            return;
        }

        getEditedEntity().setName(getEditedEntity().getPortal().getName());
        getEditedEntity().setProvider_portal_id(getEditedEntity().getApi_provider().getId().toString() +
                "_" + getEditedEntity().getPortal().getId().toString());
    }
}