package com.company.apimanager.screen.apisecuritymethod;

import io.jmix.ui.screen.*;
import com.company.apimanager.entity.ApiSecurityMethod;

@UiController("ApiSecurityMethod.edit")
@UiDescriptor("api-security-method-edit.xml")
@EditedEntityContainer("apiSecurityMethodDc")
public class ApiSecurityMethodEdit extends StandardEditor<ApiSecurityMethod> {
}