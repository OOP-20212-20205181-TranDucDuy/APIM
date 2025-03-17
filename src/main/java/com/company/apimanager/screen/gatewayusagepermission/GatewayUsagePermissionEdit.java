package com.company.apimanager.screen.gatewayusagepermission;

import com.company.apimanager.entity.GatewayService;
import com.company.apimanager.entity.VirtualArea;
import io.jmix.ui.Notifications;
import io.jmix.ui.component.EntityPicker;
import io.jmix.ui.screen.*;
import com.company.apimanager.entity.GatewayUsagePermission;
import org.springframework.beans.factory.annotation.Autowired;

@UiController("GatewayUsagePermission.edit")
@UiDescriptor("gateway-usage-permission-edit.xml")
@EditedEntityContainer("gatewayUsagePermissionDc")
public class GatewayUsagePermissionEdit extends StandardEditor<GatewayUsagePermission> {
    @Autowired
    private EntityPicker<GatewayService> gatewayField;
    @Autowired
    private Notifications notifications;

    @Subscribe
    public void onBeforeCommitChanges(BeforeCommitChangesEvent event) {
        GatewayService gatewayService = gatewayField.getValue();
        if (gatewayService == null) {
            event.preventCommit();
            return;
        }
        VirtualArea virtualArea = gatewayService.getVirtualArea();
        if (!virtualArea.getIs_active()) {
            notifications.create(Notifications.NotificationType.ERROR)
                    .withCaption("That gateway service in an inactive virtual area").show();
            event.preventCommit();
            return;
        }

        getEditedEntity().setName(getEditedEntity().getGateway().getName());
        getEditedEntity().setProvider_gateway_id(getEditedEntity().getApi_provider().getId().toString() +
                "_" + getEditedEntity().getGateway().getId().toString());
    }

}