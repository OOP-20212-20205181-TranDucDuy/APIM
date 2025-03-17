package com.company.apimanager.screen.apisecuritymethod;

import io.jmix.ui.component.Button;
import io.jmix.ui.component.HBoxLayout;
import io.jmix.ui.screen.*;
import com.company.apimanager.entity.ApiSecurityMethod;
import org.springframework.beans.factory.annotation.Autowired;

@UiController("ApiSecurityMethod.browse")
@UiDescriptor("api-security-method-browse.xml")
@LookupComponent("apiSecurityMethodsTable")
public class ApiSecurityMethodBrowse extends StandardLookup<ApiSecurityMethod> {
    @Autowired
    private HBoxLayout lookupActions;
    @Autowired
    private Button createBtn;
    @Autowired
    private Button editBtn;
    @Autowired
    private Button removeBtn;

    @Subscribe
    public void onAfterShow(AfterShowEvent event) {
        if (lookupActions.isVisible()) {
            createBtn.setVisible(false);
            editBtn.setVisible(false);
            removeBtn.setVisible(false);
        }
    }
}