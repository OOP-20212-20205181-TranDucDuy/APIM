package com.company.apimanager.screen.portalservice;

import io.jmix.ui.screen.*;
import com.company.apimanager.entity.PortalService;

@UiController("PortalService.browse")
@UiDescriptor("portal-service-browse.xml")
@LookupComponent("portalServicesTable")
public class PortalServiceBrowse extends StandardLookup<PortalService> {
}