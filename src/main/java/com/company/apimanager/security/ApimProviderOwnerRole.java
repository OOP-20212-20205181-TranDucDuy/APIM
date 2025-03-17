package com.company.apimanager.security;

import com.company.apimanager.entity.*;
import com.company.apimanager.screen.brandlogin.ApiRegister;
import io.jmix.security.model.EntityAttributePolicyAction;
import io.jmix.security.model.EntityPolicyAction;
import io.jmix.security.role.annotation.EntityAttributePolicy;
import io.jmix.security.role.annotation.EntityPolicy;
import io.jmix.security.role.annotation.ResourceRole;
import io.jmix.securityui.role.UiMinimalRole;
import io.jmix.securityui.role.annotation.MenuPolicy;
import io.jmix.securityui.role.annotation.ScreenPolicy;


@ResourceRole(name = "apim-provider-owner", code = "apim-provider-owner-code", scope = "UI")
public interface ApimProviderOwnerRole extends UiMinimalRole {
    @MenuPolicy(menuIds = {"ImportApisScreen", "Product.browse", "Consumer.browse", "RestApi.browse", "Site.browse", "ArchivedProductsScreen", "GettingstartedScreen", "SelfHostedGateway.browse", "AboutScreen", "UserPlan.browse", "ReportScreen", "LoggingScreen", "Homepage", "ApiReportScreen"})

    @ScreenPolicy(screenIds = {"ApiCallWithConsumerScreen", "ApiCall.edit", "ApiHttpHeader.edit", "ImportApisScreen", "ApiCallWithConsumer.edit","ApiCall.edit", "ApiInSite.browse", "ApiInSite.edit", "ApiRegister.browse", "ApiRegister.edit", "GettingstartedScreen", "ArchivedProductsScreen", "AboutScreen", "JwtClient.edit",
            "ApiSecurityMethod.browse", "ApiSecurityMethod.edit", "AppConfiguration.browse", "AppConfiguration.edit",
            "Consumer.browse", "Consumer.edit", "BalancedHost.browse", "BalancedHost.edit",
            "Consumer.browse", "Consumer.edit", "BalancedHost.browse", "BalancedHost.edit",
            "OAuthClient.edit", "Plan_.edit",
            "IdeScreen",
            "VirtualArea.browse", "VirtualArea.edit",
            "PortalService.browse", "PortalService.edit", "ProductSubscription.browse", "ProductSubscription.edit",
            "Product.browse", "Product.edit", "ProductSubscription.browse", "ProductSubscription.edit",
            "SelfHostedGateway.browse", "SelfHostedGateway.edit",
            "PublishedProduct.browse", "PublishedProduct.edit", "PublishedRestApi.browse", "PublishedRestApi.edit", "RestApi.browse", "RestApi.edit", "Site.browse", "Site.edit", "SiteGateway.browse", "SiteGateway.edit", "VisibilyType.browse", "VisibilyType.edit",
            "UserPlan.browse", "ReportScreen", "LoggingScreen", "Homepage", "ApiReportScreen", "StatusReportScreen"
    })
    void screens();

    @EntityAttributePolicy(entityClass = ApiCall.class, attributes = "*", action = EntityAttributePolicyAction.MODIFY)
    @EntityPolicy(entityClass = ApiCall.class, actions = {EntityPolicyAction.ALL})
    void ApiCall();

    @EntityAttributePolicy(entityClass = ApiHttpHeader.class, attributes = "*", action = EntityAttributePolicyAction.MODIFY)
    @EntityPolicy(entityClass = ApiHttpHeader.class, actions = {EntityPolicyAction.ALL})
    void ApiHttpHeader();

    @EntityAttributePolicy(entityClass = DtoRestApi.class, attributes = "*", action = EntityAttributePolicyAction.MODIFY)
    @EntityPolicy(entityClass = DtoRestApi.class, actions = {EntityPolicyAction.ALL})
    void DtoRestApi();

    @EntityAttributePolicy(entityClass = ApiInSite.class, attributes = "*", action = EntityAttributePolicyAction.MODIFY)
    @EntityPolicy(entityClass = ApiInSite.class, actions = {EntityPolicyAction.ALL})
    void ApiInSite();

    @EntityAttributePolicy(entityClass = ApiRegister.class, attributes = "*", action = EntityAttributePolicyAction.MODIFY)
    @EntityPolicy(entityClass = ApiRegister.class, actions = {EntityPolicyAction.ALL})
    void ApiRegister();

    @EntityAttributePolicy(entityClass = ApiSecurityMethod.class, attributes = "*", action = EntityAttributePolicyAction.MODIFY)
    @EntityPolicy(entityClass = ApiSecurityMethod.class, actions = {EntityPolicyAction.ALL})
    void apiSecurityMethod();

    @EntityAttributePolicy(entityClass = AppConfiguration.class, attributes = "*", action = EntityAttributePolicyAction.MODIFY)
    @EntityPolicy(entityClass = AppConfiguration.class, actions = {EntityPolicyAction.ALL})
    void AppConfiguration();

//    @EntityAttributePolicy(entityClass = Catalog.class, attributes = "*", action = EntityAttributePolicyAction.MODIFY)
//    @EntityPolicy(entityClass = Catalog.class, actions = {EntityPolicyAction.ALL})
//    void catalog();

    @EntityAttributePolicy(entityClass = Consumer.class, attributes = "*", action = EntityAttributePolicyAction.MODIFY)
    @EntityPolicy(entityClass = Consumer.class, actions = {EntityPolicyAction.ALL})
    void consumer();

    //    @EntityAttributePolicy(entityClass = DrupalLink.class, attributes = "*", action = EntityAttributePolicyAction.MODIFY)
    @EntityPolicy(entityClass = DrupalLink.class, actions = {EntityPolicyAction.ALL})
    void drupal();

    //    @EntityAttributePolicy(entityClass = GatewayService.class, attributes = "*", action = EntityAttributePolicyAction.MODIFY)
    @EntityPolicy(entityClass = GatewayService.class, actions = {EntityPolicyAction.ALL})
    void gatewayService();

    @EntityAttributePolicy(entityClass = GatewayType.class, attributes = "*", action = EntityAttributePolicyAction.VIEW)
    @EntityPolicy(entityClass = GatewayType.class, actions = {EntityPolicyAction.READ})
    void gatewayType();

    @EntityAttributePolicy(entityClass = GatewayUsagePermission.class, attributes = "*", action = EntityAttributePolicyAction.VIEW)
    @EntityPolicy(entityClass = GatewayUsagePermission.class, actions = {EntityPolicyAction.READ})
    void gatewayUsagePermission();

    @EntityAttributePolicy(entityClass = OAuthClient.class, attributes = "*", action = EntityAttributePolicyAction.MODIFY)
    @EntityPolicy(entityClass = OAuthClient.class, actions = {EntityPolicyAction.ALL})
    void OAuthClient();

    @EntityAttributePolicy(entityClass = Plan.class, attributes = "*", action = EntityAttributePolicyAction.MODIFY)
    @EntityPolicy(entityClass = Plan.class, actions = {EntityPolicyAction.ALL})
    void plan();

    @EntityAttributePolicy(entityClass = PricingPlan.class, attributes = "*", action = EntityAttributePolicyAction.MODIFY)
    @EntityPolicy(entityClass = PricingPlan.class, actions = {EntityPolicyAction.READ})
    void PricingPlan();

    @EntityAttributePolicy(entityClass = PortalService.class, attributes = "*", action = EntityAttributePolicyAction.VIEW)
    @EntityPolicy(entityClass = PortalService.class, actions = {EntityPolicyAction.READ})
    void portalService();

    @EntityAttributePolicy(entityClass = PortalUsagePermission.class, attributes = "*", action = EntityAttributePolicyAction.VIEW)
    @EntityPolicy(entityClass = PortalUsagePermission.class, actions = {EntityPolicyAction.READ})
    void portalUsagePermission();

    @EntityAttributePolicy(entityClass = Product.class, attributes = "*", action = EntityAttributePolicyAction.MODIFY)
    @EntityPolicy(entityClass = Product.class, actions = {EntityPolicyAction.ALL})
    void product();

    //////////////////


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

    @EntityAttributePolicy(entityClass = SiteGateway.class, attributes = "*", action = EntityAttributePolicyAction.MODIFY)
    @EntityPolicy(entityClass = SiteGateway.class, actions = {EntityPolicyAction.ALL})
    void gateway();

    @EntityAttributePolicy(entityClass = User.class, attributes = "*", action = EntityAttributePolicyAction.MODIFY)
    @EntityPolicy(entityClass = User.class, actions = {EntityPolicyAction.ALL})
    void user();

    @EntityAttributePolicy(entityClass = VisibilyType.class, attributes = "*", action = EntityAttributePolicyAction.VIEW)
    @EntityPolicy(entityClass = VisibilyType.class, actions = {EntityPolicyAction.READ})
    void visibilyType();

    @EntityAttributePolicy(entityClass = VirtualArea.class, attributes = "*", action = EntityAttributePolicyAction.VIEW)
    @EntityPolicy(entityClass = VirtualArea.class, actions = {EntityPolicyAction.READ})
    void virtualArea();

    @EntityAttributePolicy(entityClass = JwtClient.class, attributes = "*", action = EntityAttributePolicyAction.MODIFY)
    @EntityPolicy(entityClass = VirtualArea.class, actions = {EntityPolicyAction.ALL})
    void jwtClient();

    @EntityAttributePolicy(entityClass = User.class, attributes = "*", action = EntityAttributePolicyAction.MODIFY)
    @EntityPolicy(entityClass = User.class, actions = {EntityPolicyAction.ALL})
    void provider_user();

    @EntityAttributePolicy(entityClass = PublishedRestApi.class, attributes = "*", action = EntityAttributePolicyAction.VIEW)
    @EntityPolicy(entityClass = PublishedRestApi.class, actions = {EntityPolicyAction.ALL})
    void publishedRestApi();

    @EntityAttributePolicy(entityClass = BalancedHost.class, attributes = "*", action = EntityAttributePolicyAction.MODIFY)
    @EntityPolicy(entityClass = BalancedHost.class, actions = {EntityPolicyAction.ALL})
    void balancedHost();

    @EntityAttributePolicy(entityClass = SelfHostedGateway.class, attributes = "*", action = EntityAttributePolicyAction.MODIFY)
    @EntityPolicy(entityClass = SelfHostedGateway.class, actions = {EntityPolicyAction.ALL})
    void selfHostedGateway();
}