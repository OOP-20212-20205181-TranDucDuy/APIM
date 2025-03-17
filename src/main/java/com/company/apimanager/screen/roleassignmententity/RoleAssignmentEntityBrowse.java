package com.company.apimanager.screen.roleassignmententity;

import com.company.apimanager.entity.User;
import io.jmix.core.security.CurrentAuthentication;
import io.jmix.ui.model.CollectionLoader;
import io.jmix.ui.screen.*;
import io.jmix.securitydata.entity.RoleAssignmentEntity;
import org.springframework.beans.factory.annotation.Autowired;

@UiController("sec_RoleAssignmentEntity.browse")
@UiDescriptor("role-assignment-entity-browse.xml")
@LookupComponent("roleAssignmentEntitiesTable")
public class RoleAssignmentEntityBrowse extends StandardLookup<RoleAssignmentEntity> {
    @Autowired
    private CurrentAuthentication currentAuthentication;
    @Autowired
    private CollectionLoader<RoleAssignmentEntity> roleAssignmentEntitiesDl1;

    @Subscribe
    public void onInit(InitEvent event) {
        User curr_user = (User) currentAuthentication.getUser();
        roleAssignmentEntitiesDl1.setParameter("f_code", "api_provider_owner");
    }
}