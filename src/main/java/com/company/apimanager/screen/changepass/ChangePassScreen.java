package com.company.apimanager.screen.changepass;

import com.company.apimanager.entity.User;
import io.jmix.core.DataManager;
import io.jmix.core.Messages;
import io.jmix.core.security.CurrentAuthentication;
import io.jmix.ui.Notifications;
import io.jmix.ui.action.Action;
import io.jmix.ui.component.PasswordField;
import io.jmix.ui.screen.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.*;
import org.springframework.security.crypto.scrypt.SCryptPasswordEncoder;

import java.util.HashMap;
import java.util.Map;

@UiController("ChangePassScreen")
@UiDescriptor("change-pass-screen.xml")
public class ChangePassScreen extends Screen {
    private static final Logger log = org.slf4j.LoggerFactory.getLogger(ChangePassScreen.class);
    @Autowired
    private PasswordField oldPasswordField;
    @Autowired
    private PasswordField newPasswordField;
    @Autowired
    private PasswordField confirmPasswordField;
    @Autowired
    private Notifications notifications;
    @Autowired
    private Messages messages;
    @Autowired
    private CurrentAuthentication currentAuthentication;

    private PasswordEncoder passwordEncoder;
    @Autowired
    private DataManager dataManager;

    public ChangePassScreen() {
    }

    @Subscribe("submit")
    private void onSubmitActionPerformed(Action.ActionPerformedEvent event) {
        changePassword();
    }

    private boolean checkMatchPassword (String rawPassword) {
        UserDetails user = currentAuthentication.getUser();
        String currentPass = user.getPassword();
        return this.passwordEncoder.matches(rawPassword, currentPass);
    }

    public void setPasswordEncoder(String encodeType) {
        Map<String, PasswordEncoder> encoders = new HashMap<>();
        encoders.put("bcrypt", new BCryptPasswordEncoder());
        encoders.put("noop", NoOpPasswordEncoder.getInstance());
        encoders.put("pbkdf2", new Pbkdf2PasswordEncoder());
        encoders.put("scrypt", new SCryptPasswordEncoder());
        encoders.put("sha256", new StandardPasswordEncoder());
        this.passwordEncoder = new DelegatingPasswordEncoder(encodeType, encoders);
    }

    private boolean compareString(String targetString, String sourceString) {
        return targetString.equals(sourceString);
    }

    private void changePassword() {
        UserDetails userDetail = currentAuthentication.getUser();

        if (userDetail.getUsername().equals("admin")) {
            this.setPasswordEncoder("noop");
        } else {
            this.setPasswordEncoder("bcrypt");
        }

        if (StringUtils.isEmpty(oldPasswordField.getValue()) || StringUtils.isEmpty(newPasswordField.getValue()) || StringUtils.isEmpty(confirmPasswordField.getValue())) {
            notifications.create(Notifications.NotificationType.WARNING)
                    .withCaption(messages.getMessage(getClass(), "emptyFields"))
                    .show();
            return;
        }

        String oldPassword = StringUtils.isEmpty(oldPasswordField.getValue()) ? "" : oldPasswordField.getValue();
        String newPassword = StringUtils.isEmpty(newPasswordField.getValue()) ? "" : newPasswordField.getValue();
        String confirmPassword = StringUtils.isEmpty(confirmPasswordField.getValue()) ? "" : confirmPasswordField.getValue();

        if (!checkMatchPassword(oldPassword)) {
            notifications.create(Notifications.NotificationType.WARNING)
                    .withCaption(messages.getMessage(getClass(), "oldPasswordNotMatch"))
                    .show();
            return;
        }

        if (!compareString(newPassword, confirmPassword)) {
            notifications.create(Notifications.NotificationType.WARNING)
                    .withCaption(messages.getMessage(getClass(), "passwordNotMatch"))
                    .show();
            return;
        }
        User user = dataManager.loadValue("select u from User u where u.username = :username", User.class).store("main").parameter("username", userDetail.getUsername()).one();
        user.setPassword(this.passwordEncoder.encode(newPassword));
        dataManager.save(user);

        notifications.create(Notifications.NotificationType.HUMANIZED)
                .withCaption(messages.getMessage(getClass(), "changePasswordSuccess"))
                .show();
    }
}