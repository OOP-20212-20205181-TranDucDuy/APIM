package com.company.apimanager.screen.analyticsservice;

import io.jmix.ui.screen.*;
import com.company.apimanager.entity.AnalyticsService;

@UiController("AnalyticsService.edit")
@UiDescriptor("analytics-service-edit.xml")
@EditedEntityContainer("analyticsServiceDc")
public class AnalyticsServiceEdit extends StandardEditor<AnalyticsService> {
}