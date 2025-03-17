package com.company.apimanager.screen.portalservice;

import io.jmix.ui.screen.*;
import com.company.apimanager.entity.PortalService;

@UiController("PortalService.edit")
@UiDescriptor("portal-service-edit.xml")
@EditedEntityContainer("portalServiceDc")
public class PortalServiceEdit extends StandardEditor<PortalService> {
}