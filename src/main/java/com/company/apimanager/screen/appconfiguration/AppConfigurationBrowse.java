package com.company.apimanager.screen.appconfiguration;

import io.jmix.ui.screen.*;
import com.company.apimanager.entity.AppConfiguration;

@UiController("AppConfiguration.browse")
@UiDescriptor("app-configuration-browse.xml")
@LookupComponent("appConfigurationsTable")
public class AppConfigurationBrowse extends StandardLookup<AppConfiguration> {
}