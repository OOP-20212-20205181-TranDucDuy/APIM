package com.company.apimanager.screen.user;

import com.company.apimanager.entity.Site;
import io.jmix.core.security.CurrentAuthentication;
import io.jmix.ui.model.CollectionLoader;
import io.jmix.ui.screen.*;
import com.company.apimanager.entity.User;
import org.springframework.beans.factory.annotation.Autowired;

@UiController("UserPlan.browse")
@UiDescriptor("user-plan.xml")
@LookupComponent("usersTable")
public class UserPlan extends StandardLookup<User> {

    @Autowired
    private CollectionLoader<User> usersDl;
    @Autowired
    private CurrentAuthentication currentAuthentication;

    @Subscribe
    public void onInit(InitEvent event) {
        usersDl.setParameter("user", (User) currentAuthentication.getUser());
    }

}