package com.company.apimanager.screen.user;

import com.company.apimanager.app.Utility;
import io.jmix.core.EntityStates;
import io.jmix.ui.Notifications;
import io.jmix.ui.component.TextField;
import io.jmix.ui.screen.*;
import com.company.apimanager.entity.User;
import org.springframework.beans.factory.annotation.Autowired;

@UiController("Provider.edit")
@UiDescriptor("provider-edit.xml")
@EditedEntityContainer("userDc")
public class ProviderEdit extends StandardEditor<User> {
    @Autowired
    private Utility utility;
    @Autowired
    private Notifications notifications;
    @Autowired
    private EntityStates entityStates;
    @Autowired
    private TextField<String> nameField;

    @Subscribe
    public void onAfterShow(AfterShowEvent event) {
        if (!entityStates.isNew(getEditedEntity())) {
            nameField.setEditable(false);
        }
    }

    @Subscribe
    public void onAfterCommitChanges(AfterCommitChangesEvent event) {
        //utility.CreateRoleAssignment(getEditedEntity().getUsername(), "api_provider_owner", "resource");
    }

    @Subscribe
    public void onAfterClose(AfterCloseEvent event) {
        utility.CreateRoleAssignment(getEditedEntity().getUsername(), "api_provider_owner", "resource");
    }

    @Subscribe
    public void onBeforeClose(BeforeCloseEvent event) {
        notifications.create().withCaption("Input required").show();
        event.preventWindowClose();
    }
}