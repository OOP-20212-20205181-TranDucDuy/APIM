package com.company.apimanager.security;

import com.company.apimanager.entity.*;
import com.company.apimanager.screen.brandlogin.ApiRegister;
import com.company.apimanager.screen.roleassignmententity.RoleAssignmentEntityBrowse;
import io.jmix.data.impl.jpql.antlr2.JPA2Parser;
import io.jmix.security.model.EntityAttributePolicyAction;
import io.jmix.security.model.EntityPolicyAction;
import io.jmix.security.role.annotation.EntityAttributePolicy;
import io.jmix.security.role.annotation.EntityPolicy;
import io.jmix.security.role.annotation.ResourceRole;
import io.jmix.security.role.assignment.RoleAssignment;
import io.jmix.securitydata.entity.RoleAssignmentEntity;
import io.jmix.securityui.role.UiMinimalRole;
import io.jmix.securityui.role.annotation.MenuPolicy;
import io.jmix.securityui.role.annotation.ScreenPolicy;

import java.security.Provider;

@ResourceRole(name = "Topology Administrator", code = "topology-administrator-code", scope = "UI")
public interface TopologyAdministratorRole extends UiMinimalRole {
    @MenuPolicy(menuIds = {"GatewayAnalyticsAssociation", "Provider.browse", "AppConfiguration.browse", "VirtualArea.browse", "VirtualArea.browse", "GettingstartedScreen", "AboutScreen",  "SelfHostedGateway.browse", "PricingPlan.browse", "Homepage"})

    @ScreenPolicy(screenIds = {"AnalyticsService.browse", "AnalyticsService.edit", "AppConfiguration.browse", "AppConfiguration.edit", "GettingstartedScreen", "ArchivedProductsScreen", "GatewayAnalyticsAssociation", "AboutScreen",
            "ApiSecurityMethod.browse", "ApiSecurityMethod.edit", "AppConfiguration.browse", "AppConfiguration.edit",
            "GatewayService.browse", "GatewayService.edit", "GatewayType.browse", "GatewayType.edit",
            "GatewayUsagePermission.browse", "GatewayUsagePermission.edit", "GatewayType.browse", "GatewayType.edit",
            "PortalService.browse", "PortalService.edit", "PortalUsagePermission.browse", "PortalUsagePermission.edit",
            "ProductSubscription.browse", "ProductSubscription.edit",
            "User.browse", "User.edit",
            "IdeScreen",
            "SelfHostedGateway.browse", "SelfHostedGateway.edit",
            "Product.browse", "Product.edit", "ProductSubscription.browse", "ProductSubscription.edit",
            "PublishedProduct.browse", "PublishedProduct.edit", "VirtualArea.browse", "VirtualArea.edit", "Site.browse", "Site.edit", "SiteGateway.browse", "SiteGateway.edit",
            "VisibilyType.browse", "VisibilyType.edit", "Provider.browse", "Provider.edit", "PricingPlan.browse", "PricingPlan.edit", "Homepage", "ApiReportScreen", "StatusReportScreen"})
    void screens();

    @EntityAttributePolicy(entityClass = AnalyticsService.class, attributes = "*", action = EntityAttributePolicyAction.MODIFY)
    @EntityPolicy(entityClass = AnalyticsService.class, actions = {EntityPolicyAction.ALL})
    void AnalyticsService();

    @EntityAttributePolicy(entityClass = AppConfiguration.class, attributes = "*", action = EntityAttributePolicyAction.MODIFY)
    @EntityPolicy(entityClass = AppConfiguration.class, actions = {EntityPolicyAction.ALL})
    void AppConfiguration();

    @EntityAttributePolicy(entityClass = GatewayService.class, attributes = "*", action = EntityAttributePolicyAction.MODIFY)
    @EntityPolicy(entityClass = GatewayService.class, actions = {EntityPolicyAction.ALL})
    void gatewayService();

    @EntityAttributePolicy(entityClass = GatewayType.class, attributes = "*", action = EntityAttributePolicyAction.VIEW)
    @EntityPolicy(entityClass = GatewayType.class, actions = {EntityPolicyAction.READ})
    void gatewayType();

    @EntityAttributePolicy(entityClass = GatewayUsagePermission.class, attributes = "*", action = EntityAttributePolicyAction.MODIFY)
    @EntityPolicy(entityClass = GatewayUsagePermission.class, actions = {EntityPolicyAction.ALL})
    void gatewayUsagePermission();

    @EntityAttributePolicy(entityClass = PortalService.class, attributes = "*", action = EntityAttributePolicyAction.MODIFY)
    @EntityPolicy(entityClass = PortalService.class, actions = {EntityPolicyAction.ALL})
    void portalService();

    @EntityAttributePolicy(entityClass = PortalUsagePermission.class, attributes = "*", action = EntityAttributePolicyAction.MODIFY)
    @EntityPolicy(entityClass = PortalUsagePermission.class, actions = {EntityPolicyAction.ALL})
    void portalUsagePermission();

//    @EntityAttributePolicy(entityClass = RoleAssignmentEntityBrowse.class, attributes = "*", action = EntityAttributePolicyAction.MODIFY)
//    @EntityPolicy(entityClass = RoleAssignmentEntityBrowse.class, actions = {EntityPolicyAction.ALL})
//    void RoleAssignmentEntityBrowse();

    //////////////////

    @EntityAttributePolicy(entityClass = VirtualArea.class, attributes = "*", action = EntityAttributePolicyAction.MODIFY)
    @EntityPolicy(entityClass = VirtualArea.class, actions = {EntityPolicyAction.ALL})
    void VirtualArea();


    @EntityAttributePolicy(entityClass = ProductSubscription.class, attributes = "*", action = EntityAttributePolicyAction.MODIFY)
    @EntityPolicy(entityClass = ProductSubscription.class, actions = {EntityPolicyAction.ALL})
    void ProductSubscription();

    @EntityAttributePolicy(entityClass = PublishedProduct.class, attributes = "*", action = EntityAttributePolicyAction.MODIFY)
    @EntityPolicy(entityClass = PublishedProduct.class, actions = {EntityPolicyAction.ALL})
    void PublishedProduct();

    @EntityAttributePolicy(entityClass = RestApi.class, attributes = "*", action = EntityAttributePolicyAction.MODIFY)
    @EntityPolicy(entityClass = RestApi.class, actions = {EntityPolicyAction.ALL})
    void RestApi();

    @EntityAttributePolicy(entityClass = Site.class, attributes = "*", action = EntityAttributePolicyAction.MODIFY)
    @EntityPolicy(entityClass = Site.class, actions = {EntityPolicyAction.ALL})
    void Site();

    @EntityAttributePolicy(entityClass = PricingPlan.class, attributes = "*", action = EntityAttributePolicyAction.MODIFY)
    @EntityPolicy(entityClass = PricingPlan.class, actions = {EntityPolicyAction.ALL})
    void PricingPlan();

    @EntityAttributePolicy(entityClass = SiteGateway.class, attributes = "*", action = EntityAttributePolicyAction.MODIFY)
    @EntityPolicy(entityClass = SiteGateway.class, actions = {EntityPolicyAction.ALL})
    void gateway();

    @EntityAttributePolicy(entityClass = User.class, attributes = "*", action = EntityAttributePolicyAction.MODIFY)
    @EntityPolicy(entityClass = User.class, actions = {EntityPolicyAction.ALL})
    void user();

    @EntityAttributePolicy(entityClass = VisibilyType.class, attributes = "*", action = EntityAttributePolicyAction.MODIFY)
    @EntityPolicy(entityClass = VisibilyType.class, actions = {EntityPolicyAction.ALL})
    void visibily_type();

    @EntityAttributePolicy(entityClass = RoleAssignmentEntity.class, attributes = "*", action = EntityAttributePolicyAction.MODIFY)
    @EntityPolicy(entityClass = RoleAssignmentEntity.class, actions = EntityPolicyAction.ALL)
    void roleAssignmentEntity();

    @EntityAttributePolicy(entityClass = SelfHostedGateway.class, attributes = "*", action = EntityAttributePolicyAction.MODIFY)
    @EntityPolicy(entityClass = SelfHostedGateway.class, actions = {EntityPolicyAction.ALL})
    void selfHostedGateway();
}