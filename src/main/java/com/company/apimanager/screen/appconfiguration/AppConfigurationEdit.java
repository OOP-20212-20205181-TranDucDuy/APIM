package com.company.apimanager.screen.appconfiguration;

import io.jmix.ui.screen.*;
import com.company.apimanager.entity.AppConfiguration;

@UiController("AppConfiguration.edit")
@UiDescriptor("app-configuration-edit.xml")
@EditedEntityContainer("appConfigurationDc")
public class AppConfigurationEdit extends StandardEditor<AppConfiguration> {
}