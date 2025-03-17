package com.company.apimanager.screen.roleassignmententity;

import io.jmix.ui.screen.*;
import io.jmix.securitydata.entity.RoleAssignmentEntity;

@UiController("sec_RoleAssignmentEntity.edit")
@UiDescriptor("role-assignment-entity-edit.xml")
@EditedEntityContainer("roleAssignmentEntityDc")
public class RoleAssignmentEntityEdit extends StandardEditor<RoleAssignmentEntity> {
    @Subscribe
    public void onBeforeCommitChanges(BeforeCommitChangesEvent event) {
        getEditedEntity().setRoleCode("api_provider_owner");
    }
}