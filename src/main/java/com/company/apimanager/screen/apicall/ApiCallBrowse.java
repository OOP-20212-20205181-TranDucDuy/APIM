package com.company.apimanager.screen.apicall;

import io.jmix.ui.screen.*;
import com.company.apimanager.entity.ApiCall;

@UiController("ApiCall.browse")
@UiDescriptor("api-call-browse.xml")
@LookupComponent("apiCallsTable")
public class ApiCallBrowse extends StandardLookup<ApiCall> {
}