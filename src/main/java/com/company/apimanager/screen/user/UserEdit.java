package com.company.apimanager.screen.user;
import com.company.apimanager.app.ConfigurationProperties;
import com.company.apimanager.app.Utility;
import com.company.apimanager.entity.GatewayService;
import com.company.apimanager.entity.PricingPlan;
import com.company.apimanager.entity.User;
import io.jmix.core.DataManager;
import io.jmix.core.EntityStates;
import io.jmix.core.security.CurrentAuthentication;
import io.jmix.ui.Notifications;
import io.jmix.ui.component.ComboBox;
import io.jmix.ui.component.PasswordField;
import io.jmix.ui.component.TextField;
import io.jmix.ui.navigation.Route;
import io.jmix.ui.screen.*;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.*;
import java.util.stream.Collectors;

@UiController("User.edit")
@UiDescriptor("user-edit.xml")
@EditedEntityContainer("userDc")
@Route(value = "users/edit", parentPrefix = "users")
public class UserEdit extends StandardEditor<User> {
    @Autowired
    private CurrentAuthentication currentAuthentication;
    @Autowired
    private Utility utility;

    @Autowired
    private EntityStates entityStates;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private PasswordField passwordField;

    @Autowired
    private TextField<String> usernameField;
    private AfterShowEvent event;

    private Boolean create_new;


    @Autowired
    private PasswordField confirmPasswordField;

    @Autowired
    private Notifications notifications;

    @Autowired
    private MessageBundle messageBundle;

    @Autowired
    private ComboBox<String> timeZoneField;
    @Autowired
    private ComboBox<String> providerPlanField;

    @Autowired
    private DataManager dataManager;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private ConfigurationProperties config;

    @Subscribe
    public void onInitEntity(InitEntityEvent<User> event) {
        usernameField.setEditable(true);
        passwordField.setVisible(true);
        confirmPasswordField.setVisible(true);
    }

    @Subscribe
    public void onAfterShow(AfterShowEvent event) {
        if (entityStates.isNew(getEditedEntity())) {
            usernameField.focus();
            create_new = true;
        }
        else {

            create_new = false;
        }

    }

    @Subscribe
    protected void onBeforeCommit(BeforeCommitChangesEvent event) {
        String name = getEditedEntity().getUsername();
        if (Utility.findSpecialChar(name) != 0) {
            notifications.create(Notifications.NotificationType.WARNING).withCaption("Name must not contain special characters")
                    .show();
            event.preventCommit();
            return;
        }
        getEditedEntity().setProviderPlan("Enterprise"); //Onpremise: Provider plan = Enterprise

        if (getEditedEntity().getProviderPlan() == null) {
            notifications.create(Notifications.NotificationType.WARNING).
                    withCaption("Provider plan is NULL")
                    .show();
            event.preventCommit();
            return;
        }

        if ((!getEditedEntity().getProviderPlan().equals("Pilot")) &&
        (!getEditedEntity().getProviderPlan().equals("Normal")) &&
                (!getEditedEntity().getProviderPlan().equals("Enterprise"))) {
            notifications.create(Notifications.NotificationType.WARNING).
                    withCaption("Please select Provider plan")
                    .show();
            event.preventCommit();
            return;
        }
        /*
        if (getEditedEntity().getPricingPlan()==null) {
            notifications.create(Notifications.NotificationType.WARNING).
                    withCaption("Please select Pricing Plan.")
                    .show();
            event.preventCommit();
            return;
        }

         */

        if (entityStates.isNew(getEditedEntity())) {
            if (!Objects.equals(passwordField.getValue(), confirmPasswordField.getValue())) {
                notifications.create(Notifications.NotificationType.WARNING)
                        .withCaption(messageBundle.getMessage("passwordsDoNotMatch"))
                        .show();
                event.preventCommit();
                return;
            }
            getEditedEntity().setPassword(passwordEncoder.encode(passwordField.getValue()));
        }
    }

    @Subscribe
    public void onAfterCommitChanges(AfterCommitChangesEvent event) {
        //Check if the user has Topo admin role
        String roles_str = currentAuthentication.getAuthentication().getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.joining(","));
        String[] roles = roles_str.split(",");

        Boolean is_topo_admin = false;
        for (int i = 0; i < roles.length; i++) {
            if (roles[i].matches("topo_admin") ) {
                is_topo_admin = true;
                break;
            }
        }
        if ((is_topo_admin) && (create_new)) {
            if (utility.CreateRoleAssignment(getEditedEntity().getUsername(), "api_provider_owner", "resource") != 0) {
                notifications.create(Notifications.NotificationType.ERROR)
                        .withCaption("Could not assign API Provider role to that user")
                        .show();
            }
        }
        User owner = getEditedEntity();
        List<GatewayService> gatewaysWithThisOwner = dataManager.load(GatewayService.class)
                .query("select distinct s.gateway from Site s where s.owner = :owner")
                .parameter("owner", owner)
                .list();
        for (GatewayService gateway: gatewaysWithThisOwner ) {
            Map<String, Object> map = new HashMap<>();
            map.put("owner", owner);
            map.put("gateway", gateway);
            //rabbitTemplate.convertAndSend(config.pricingPlanQueueName,map);
        }
    }

    @Subscribe
    public void onInit(InitEvent event) {
        timeZoneField.setOptionsList(Arrays.asList(TimeZone.getAvailableIDs()));

        List<String> list = User.getAvailableProviderPlans();
        providerPlanField.setOptionsList(list);
        providerPlanField.setValue(list.get(0));
    }
}