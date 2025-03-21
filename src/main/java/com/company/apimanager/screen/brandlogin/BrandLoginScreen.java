package com.company.apimanager.screen.brandlogin;

import com.company.apimanager.app.EncodePassword;
import com.company.apimanager.app.RestClient;
import com.company.apimanager.app.Utility;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.vaadin.ui.Dependency;
import io.jmix.core.DataManager;
import io.jmix.core.MessageTools;
import io.jmix.core.Messages;
import io.jmix.securityui.authentication.AuthDetails;
import io.jmix.securityui.authentication.LoginScreenSupport;
import io.jmix.ui.JmixApp;
import io.jmix.ui.Notifications;
import io.jmix.ui.action.Action;
import io.jmix.ui.component.*;
import io.jmix.ui.navigation.NavigationState;
import io.jmix.ui.navigation.Route;
import io.jmix.ui.navigation.UrlRouting;
import io.jmix.ui.screen.*;
import io.jmix.ui.security.UiLoginProperties;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Locale;
import java.util.Objects;

@UiController("BrandLogin")
@UiDescriptor("brand-login-screen.xml")
@Route(path = "login", root = true)
public class BrandLoginScreen extends Screen {

    @Autowired
    private Image logoImage;

    @Autowired
    private TextField<String> usernameField;

    @Autowired
    private PasswordField passwordField;

    @Autowired
    private CheckBox rememberMeCheckBox;

    @Autowired
    private ComboBox<Locale> localesField;

    @Autowired
    private Notifications notifications;

    @Autowired
    private Messages messages;

    @Autowired
    private MessageTools messageTools;

    @Autowired
    private LoginScreenSupport loginScreenSupport;

    @Autowired
    private UiLoginProperties loginProperties;

    @Autowired
    private JmixApp app;

    @Autowired
    private UrlRouting urlRouting;

    @Autowired
    private DataManager dataManager;

    protected NavigationState navigationState;
    private final Logger log = LoggerFactory.getLogger(getClass());

    @Subscribe
    private void onInit(InitEvent event) {
        usernameField.focus();
        initLogoImage();
        initLocalesField();
        initDefaultCredentials();
        loadStyles();
    }

    private void initLocalesField() {
        localesField.setOptionsMap(messageTools.getAvailableLocalesMap());
        localesField.setValue(app.getLocale());
        localesField.addValueChangeListener(this::onLocalesFieldValueChangeEvent);
    }

    private void onLocalesFieldValueChangeEvent(HasValue.ValueChangeEvent<Locale> event) {
        //noinspection ConstantConditions
        app.setLocale(event.getValue());
        UiControllerUtils.getScreenContext(this).getScreens()
                .create(this.getClass(), OpenMode.ROOT)
                .show();
    }

//    @Subscribe
//    private void onBeforeShow(BeforeShowEvent event) {
//        navigationState = urlRouting.getState();
//    }
//
//    @Subscribe
//    private void onAfterShow(AfterShowEvent event) {
//        String token = navigationState.getParams().get("access_token");
//        String email = Utility.verifyToken(token);
//        if (!email.isEmpty()) {
//            int index = email.indexOf('@');
//            String username = email.substring(0,index);
//            String accessToken = Utility.getToken();
//            RestClient restTemplate = new RestClient();
//            String checkUserUrl = "http://103.160.79.193:8080/api-manager/rest/entities/User/search";
//            String filterQuery = "{\n" +
//                    "  \"filter\": {\n" +
//                    "    \"conditions\": [\n" +
//                    "      {\n" +
//                    "        \"property\": \"username\",\n" +
//                    "        \"operator\": \"=\",\n" +
//                    "        \"value\": \"" + username + "\"\n" +
//                    "      }\n" +
//                    "    ]\n" +
//                    "  }\n" +
//                    "}";
//            String checkResult = restTemplate.postToken(checkUserUrl, filterQuery, "Authorization", "Bearer " + accessToken);
//            JsonArray convertCheckResult = new Gson().fromJson(checkResult, JsonArray.class);
//            if (convertCheckResult.size() <= 0) {
//                PasswordEncoder encoder = EncodePassword.setPasswordEncoder();
//                String newPass = encoder.encode(username);
//                String postStr = "{\n" +
//                        "        \"_entityName\": \"User\",\n" +
//                        "        \"_instanceName\": \"SSI Company [ssi]\",\n" +
//                        "        \"firstName\": \"" + username + "\",\n" +
//                        "        \"lastName\": \"Company\",\n" +
//                        "        \"timeZoneId\": \"Asia/Bangkok\",\n" +
//                        "        \"active\": true,\n" +
//                        "        \"providerPlan\": \"Enterprise\",\n" +
//                        "        \"username\": \"" + username + "\",\n" +
//                        "        \"password\": \"" + newPass + "\"\n" +
//                        "    }";
//                String createUserUrl = "http://103.160.79.193:8080/api-manager/rest/entities/User";
//                String createResult = restTemplate.postToken(createUserUrl, postStr, "Authorization", "Bearer " + accessToken);
//                String roleBody = "{\n" +
//                        "    \"username\": \"" + username + "\",\n" +
//                        "    \"roleCode\": \"api_provider_owner\",\n" +
//                        "    \"roleType\": \"resource\"\n" +
//                        "}";
//                String createRoleUrl = "http://103.160.79.193:8080/api-manager/rest/entities/sec_RoleAssignmentEntity";
//                String roleResult = restTemplate.postToken(createRoleUrl, roleBody, "Authorization", "Bearer " + accessToken);
//                StringBuilder outputCmd = new StringBuilder();
//                StringBuilder errCmd = new StringBuilder();
//                Utility.executeOsCommand("bash /home/createdns.sh " + username, outputCmd, errCmd);
//                Utility.executeOsCommand("docker exec -it kong-demo /add.sh " + username, outputCmd, errCmd);
//            }
//            usernameField.setValue(username);
//            passwordField.setValue(username);
//
//            login();
//        }
//    }

    private void initDefaultCredentials() {
        String defaultUsername = loginProperties.getDefaultUsername();
        if (!StringUtils.isBlank(defaultUsername) && !"<disabled>".equals(defaultUsername)) {
            //usernameField.setValue(defaultUsername);
        } else {
            usernameField.setValue("");
        }

        String defaultPassword = loginProperties.getDefaultPassword();
        if (!StringUtils.isBlank(defaultPassword) && !"<disabled>".equals(defaultPassword)) {
            //passwordField.setValue(defaultPassword);
        } else {
            passwordField.setValue("");
        }
    }

    @Subscribe("submit")
    private void onSubmitActionPerformed(Action.ActionPerformedEvent event) {
        login();
    }

    private void login() {
        String username = usernameField.getValue();
        String password = passwordField.getValue();

        if (StringUtils.isEmpty(username) || StringUtils.isEmpty(password)) {
            notifications.create(Notifications.NotificationType.WARNING)
                    .withCaption(messages.getMessage(getClass(), "emptyUsernameOrPassword"))
                    .show();
            return;
        }

        try {
            loginScreenSupport.authenticate(
                    AuthDetails.of(username, password)
                            .withLocale(localesField.getValue())
                            .withRememberMe(rememberMeCheckBox.isChecked()), this);
        } catch (BadCredentialsException | DisabledException e) {
            log.info("Login failed", e);
            notifications.create(Notifications.NotificationType.ERROR)
                    .withCaption(messages.getMessage(getClass(), "loginFailed"))
                    .withDescription(messages.getMessage(getClass(), "badCredentials"))
                    .show();
        }
    }

    private void loadStyles() {
        ScreenDependencyUtils.addScreenDependency(this,
                "vaadin://brand-login-screen/login.css", Dependency.Type.STYLESHEET);
    }

    private void initLogoImage() {
        logoImage.setSource(RelativePathResource.class)
                .setPath("VAADIN/brand-login-screen/jmix-icon-login.svg");
    }
}