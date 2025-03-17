package com.company.apimanager.screen.analyticsservice;

import io.jmix.ui.component.Button;
import io.jmix.ui.component.ButtonsPanel;
import io.jmix.ui.component.HBoxLayout;
import io.jmix.ui.screen.*;
import com.company.apimanager.entity.AnalyticsService;
import org.springframework.beans.factory.annotation.Autowired;

@UiController("AnalyticsService.browse")
@UiDescriptor("analytics-service-browse.xml")
@LookupComponent("analyticsServicesTable")
public class AnalyticsServiceBrowse extends StandardLookup<AnalyticsService> {
}