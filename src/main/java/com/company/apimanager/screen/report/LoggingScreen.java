package com.company.apimanager.screen.report;

import com.company.apimanager.entity.AnalyticsService;
import com.company.apimanager.entity.Consumer;
import com.company.apimanager.entity.User;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.jmix.core.DataManager;
import io.jmix.core.Messages;
import io.jmix.core.security.CurrentAuthentication;
import io.jmix.ui.Notifications;
import io.jmix.ui.component.ComboBox;
import io.jmix.ui.component.HasValue;
//import io.jmix.ui.component.JavaScriptComponent;
import io.jmix.ui.component.SourceCodeEditor;
import io.jmix.ui.screen.Screen;
import io.jmix.ui.screen.Subscribe;
import io.jmix.ui.screen.UiController;
import io.jmix.ui.screen.UiDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@UiController("LoggingScreen")
@UiDescriptor("logging-screen.xml")
public class LoggingScreen extends Screen {
    @Autowired
    private DataManager dataManager;
    @Autowired
    private CurrentAuthentication currentAuthentication;

    private final Logger log = LoggerFactory.getLogger(LoggingScreen.class);

    @Autowired
    private Notifications notifications;
    @Autowired
    private Messages messages;
    @Autowired
    private ComboBox<UUID> consumers;
    @Autowired
    private SourceCodeEditor logging;
//    @Autowired
//    protected JavaScriptComponent datetimePicker;

    private String getRoleNames(Authentication authentication) {
        return currentAuthentication
                .getAuthentication()
                .getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));
    }

    @Subscribe
    private void onInit(InitEvent event) throws IOException {
        logging.setEditable(false);
        logging.setShowGutter(true);
        User user = (User) currentAuthentication.getUser();
        List<Consumer> listConsumer = dataManager.loadValue("select c from Consumer c where c.provider = :owner", Consumer.class).store("main").parameter("owner", user).list();
        Map<String, UUID> map = new LinkedHashMap<>();
        for (Consumer consumer:listConsumer) {
            map.put(consumer.getName(), consumer.getId());
        }
        consumers.setOptionsMap(map);

        TimePickerState state = new TimePickerState();
        state.now = "12:35:57";

        state.showSeconds = true;
        state.twentyFour = true;

//        datetimePicker.setState(state);
    }

    @Subscribe("logging")
    public void loadMoreResult() {

    }

    @Subscribe("consumers")
    public void onAnalyticsUrlValueChange(HasValue.ValueChangeEvent event) throws IOException {
        try {
            String roleName = this.getRoleNames(currentAuthentication.getAuthentication());
            String[] roles = roleName.split(",");

            Boolean isProvider = false;
            for (int i = 0; i < roles.length; i++) {
                if (roles[i].matches("api_provider_owner") ) {
                    isProvider = true;
                    break;
                }
            }

            if (!isProvider) {
                notifications.create(Notifications.NotificationType.WARNING)
                        .withCaption(messages.getMessage(getClass(), "reportScreen.errorAnalytic"))
                        .show();
                return;
            }

            User user = (User) currentAuthentication.getUser();
            UUID consumerId = (UUID) event.getValue();
            assert consumerId != null;
            Consumer consumer = dataManager.load(Consumer.class).id(consumerId).one();
            AnalyticsService esInfo = consumer.getSite().getGateway().getAnalyticsAssociated();

            if (Objects.isNull(esInfo)) {
                notifications.create(Notifications.NotificationType.WARNING)
                        .withCaption(messages.getMessage(getClass(), "reportScreen.errorRelation"))
                        .show();
                return;
            }

            StringBuilder data = new StringBuilder();

            JsonArray consumerReport = callEsRequestLogging(esInfo.getManagement_endpoint(), user.getUsername(), consumer, consumer.getSite().getName());

            for (JsonElement element:consumerReport) {
                JsonObject elementObject = element.getAsJsonObject();
                JsonObject source = elementObject.get("_source").getAsJsonObject();
                JsonObject request = source.get("request").getAsJsonObject();
                JsonObject response = source.get("response").getAsJsonObject();
                String ip = source.get("client_ip") != null ? source.get("client_ip").getAsString() : null;
                String uri = request.get("uri").getAsString();
                String size = request.get("size").getAsString();
                String userAgent = request.get("headers").getAsJsonObject().get("user-agent").getAsString();
                String timestamp = source.get("@timestamp").getAsString();
                String method = request.get("method").getAsString();
                String statusCode = response.get("status").getAsString();
                data.append("""
                            %s -- [%s] "%s %s" %s %s "%s"
                        """.formatted(ip, timestamp, method, uri, statusCode, size, userAgent)).append("\n");
            }

            logging.setValue(data.toString());
        } catch (Exception exception) {
            log.info("Catch error report ES");
            log.info(exception.getMessage());
            notifications.create(Notifications.NotificationType.WARNING)
                    .withCaption(messages.getMessage(getClass(), "reportScreen.errorAnalytic"))
                    .show();
        }
    }

    private JsonArray callEsRequestLogging (String esIp, String username, Consumer consumer, String siteName) throws IOException {
        String kongRouteUrl = esIp + "/fluentd-*/_search";
        com.company.apimanager.app.RestClient restTemplate = new com.company.apimanager.app.RestClient();
        String consumerUsername = username + "." + siteName.toLowerCase() + "." + consumer.getName().toLowerCase();
        String body = """
                {
                    "query": {
                        "bool": {
                            "must": [
                                {
                                    "match": {
                                        "consumer.username": "%s"
                                    }
                                }
                            ]
                        }
                    },
                    "from": 0,
                    "size": 100
                }
                """.formatted(consumerUsername);

        String resultAsJsonStr = restTemplate.post(kongRouteUrl, body);
        JsonObject convertedObject = new Gson().fromJson(resultAsJsonStr, JsonObject.class);
        return  convertedObject.get("hits").getAsJsonObject().get("hits").getAsJsonArray();
    }
}