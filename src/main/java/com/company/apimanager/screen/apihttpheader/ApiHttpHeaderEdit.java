package com.company.apimanager.screen.apihttpheader;

import io.jmix.ui.screen.*;
import com.company.apimanager.entity.ApiHttpHeader;

@UiController("ApiHttpHeader.edit")
@UiDescriptor("api-http-header-edit.xml")
@EditedEntityContainer("apiHttpHeaderDc")
public class ApiHttpHeaderEdit extends StandardEditor<ApiHttpHeader> {
}