package com.company.apimanager.screen.report;

import com.company.apimanager.entity.*;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.jmix.charts.component.SerialChart;
import io.jmix.charts.model.graph.Graph;
import io.jmix.charts.model.graph.GraphType;
import io.jmix.core.DataManager;
import io.jmix.core.Messages;
import io.jmix.core.security.CurrentAuthentication;
import io.jmix.ui.Notifications;
import io.jmix.ui.component.ComboBox;
import io.jmix.ui.component.HasValue;
import io.jmix.ui.data.DataItem;
import io.jmix.ui.data.impl.ListDataProvider;
import io.jmix.ui.data.impl.MapDataItem;
import io.jmix.ui.screen.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@UiController("ReportScreen")
@UiDescriptor("report-screen.xml")
public class ReportScreen extends Screen {
    @Autowired
    private SerialChart chart;
    @Autowired
    private SerialChart api_chart;
    @Autowired
    private SerialChart topo_consumer_chart;
    @Autowired
    private DataManager dataManager;
    @Autowired
    private CurrentAuthentication currentAuthentication;
    @Autowired
    private ComboBox<UUID> analyticsUrl;

    private final Logger log = LoggerFactory.getLogger(ReportScreen.class);

    @Autowired
    private Notifications notifications;
    @Autowired
    private Messages messages;

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
        User user = (User) currentAuthentication.getUser();
        List<Site> sites = dataManager.loadValue("select s from Site s where s.owner = :owner", Site.class).store("main").parameter("owner", user).list();
        Map<String, UUID> map = new LinkedHashMap<>();
        for (Site site:sites) {
            map.put(site.getName(), site.getId());
        }
        analyticsUrl.setOptionsMap(map);
        topo_consumer_chart.setVisible(false);
        chart.setVisible(false);
        api_chart.setVisible(false);
    }

    @Subscribe("analyticsUrl")
    public void onAnalyticsUrlValueChange(HasValue.ValueChangeEvent event) throws IOException {
        try {
            String roleName = this.getRoleNames(currentAuthentication.getAuthentication());
            String[] roles = roleName.split(",");

            Boolean isTopoAdmin = false;
            Boolean isProvider = false;
            for (int i = 0; i < roles.length; i++) {
                if (roles[i].matches("topo_admin") ) {
                    isTopoAdmin = true;
                    break;
                }

                if (roles[i].matches("api_provider_owner") ) {
                    isProvider = true;
                    break;
                }
            }

            User user = (User) currentAuthentication.getUser();
            UUID esId = (UUID) event.getValue();
            assert esId != null;
            Site siteInfo = dataManager.load(Site.class).id(esId).one();
            AnalyticsService esInfo = siteInfo.getGateway().getAnalyticsAssociated();

            if (Objects.isNull(esInfo)) {
                notifications.create(Notifications.NotificationType.WARNING)
                        .withCaption(messages.getMessage(getClass(), "reportScreen.errorRelation"))
                        .show();
                return;
            }

            if (isProvider) {
                topo_consumer_chart.setVisible(false);
                chart.setVisible(true);
                api_chart.setVisible(true);
                this.buildConsumerChart(esInfo.getManagement_endpoint(), user, siteInfo);
                this.buildApiChart(esInfo.getManagement_endpoint(), user, siteInfo);
            } else if (isTopoAdmin) {
                chart.setVisible(false);
                api_chart.setVisible(false);
                topo_consumer_chart.setVisible(true);
//            this.buildTopoConsumerChart();
            } else {
                notifications.create(Notifications.NotificationType.WARNING)
                        .withCaption(messages.getMessage(getClass(), "reportScreen.errorPermission"))
                        .show();
            }
        } catch (Exception exception)
        {
            log.info("Catch error report ES");
            log.info(exception.getMessage());
            notifications.create(Notifications.NotificationType.WARNING)
                    .withCaption(messages.getMessage(getClass(), "reportScreen.errorAnalytic"))
                    .show();
        }
    }

    private DataItem transportCount(String name, int count, String key) {
        MapDataItem item = new MapDataItem();
        item.add(key, name);
        item.add(name, count);
        return item;
    }

    private JsonArray callEsReport (String esIp, String username, String siteName) throws IOException {
        String kongRouteUrl = esIp + "/fluentd-*/_search";
        com.company.apimanager.app.RestClient restTemplate = new com.company.apimanager.app.RestClient();
        String body = "{\n" +
                "    \"query\": {\n" +
                "        \"bool\": {\n" +
                "            \"must\": [" +
                "{\n" +
                "                \"wildcard\": {\n" +
                "                    \"consumer.username\": {\n" +
                "                        \"value\": \"" + username + "*\"\n" +
                "                    }\n" +
                "                }\n" +
                "            }," +
                "{\n" +
                "                \"wildcard\": {\n" +
                "                    \"route.name\": {\n" +
                "                        \"value\": \"" + username + "*\"\n" +
                "                    }\n" +
                "                }\n" +
                "            }" +
                "]\n" +
                "        }\n" +
                "    },\n" +
                "    \"aggs\": {\n" +
                "        \"consumer\": {\n" +
                "            \"terms\": {\n" +
                "                \"field\": \"consumer.username.keyword\"\n" +
                "            }\n" +
                "        }\n" +
                "    }\n" +
                "}";

        String resultAsJsonStr = restTemplate.post(kongRouteUrl, body);
        JsonObject convertedObject = new Gson().fromJson(resultAsJsonStr, JsonObject.class);
        return convertedObject.get("aggregations").getAsJsonObject().get("consumer").getAsJsonObject().get("buckets").getAsJsonArray();
    }

    private JsonArray callEsApiReport (String esIp, String username, String siteName) {
        String kongRouteUrl = esIp + "/fluentd-*/_search";
        com.company.apimanager.app.RestClient restTemplate = new com.company.apimanager.app.RestClient();
        String body = "{\n" +
                "    \"query\": {\n" +
                "        \"bool\": {\n" +
                "            \"must\": [\n" +
                "                {\n" +
                "                    \"wildcard\": {\n" +
                "                        \"route.name\": {\n" +
                "                            \"value\": \"" + username + "*\"\n" +
                "                        }\n" +
                "                    }\n" +
                "                },\n" +
                "                {\n" +
                "                    \"wildcard\": {\n" +
                "                        \"route.name\": {\n" +
                "                            \"value\": \"" + username + "*\"\n" +
                "                        }\n" +
                "                    }\n" +
                "                },\n" +
                "                {\n" +
                "                    \"exists\": {\"field\": \"route.name\"}\n" +
                "                }\n" +
                "            ]\n" +
                "        }\n" +
                "    },\n" +
                "    \"aggs\": {\n" +
                "        \"routes\": {\n" +
                "            \"terms\": {\n" +
                "                \"field\": \"route.name.keyword\"\n" +
                "            }\n" +
                "        }\n" +
                "    },\n" +
                "    \"size\": 10000\n" +
                "}";

        String resultAsJsonStr = restTemplate.post(kongRouteUrl, body);
        JsonObject convertedObject = new Gson().fromJson(resultAsJsonStr, JsonObject.class);
        return convertedObject.get("aggregations").getAsJsonObject().get("routes").getAsJsonObject().get("buckets").getAsJsonArray();
    }

    private void buildConsumerChart(String esIp, User provider, Site site) throws IOException {
        ListDataProvider dataProvider = new ListDataProvider();
        List<Graph> listGraph = new ArrayList<>();
        List<Consumer> listConsumers = dataManager.load(Consumer.class)
                .query("select c from Consumer c where c.provider = :provider")
                .parameter("provider", provider)
                .list();
        JsonArray consumerReport = this.callEsReport(esIp, provider.getUsername(), site.getName());
        HashMap<String, Integer> report = new HashMap<>();

        for (JsonElement element:consumerReport) {
            JsonObject elementObject = element.getAsJsonObject();
            if (elementObject.get("key").getAsString().contains("." + site.getName().toLowerCase() + ".")) {
                report.put(elementObject.get("key").getAsString(), elementObject.get("doc_count").getAsInt());
            }
        }

        for (Consumer listConsumer : listConsumers) {
            Graph graph = new Graph();
            String name = listConsumer.getProvider_consumer_name();
            if (report.containsKey(name) && report.get(name) > 0) {
                graph.setTitle(name);
                graph.setType(GraphType.COLUMN);
                graph.setFillAlphas(0.9);
                graph.setValueField(name);
                graph.setAlphaField("0.6");
                graph.setLineAlpha(0.4);
                dataProvider.addItem(transportCount(name, report.get(name), "name"));
                listGraph.add(graph);
            }
        }
        if (!listGraph.isEmpty()) {
            chart.setGraphs(listGraph);
        }
        chart.setCategoryField("name");
        chart.setDataProvider(dataProvider);
    }

    private void buildApiChart(String esIp, User user, Site site) {
        JsonArray jsonArray = this.callEsApiReport(esIp, user.getUsername(), site.getName());
        ListDataProvider dataProviderApi = new ListDataProvider();

        List<Graph> listGraphApi = new ArrayList<>();
        for (int i = 0; i < jsonArray.size(); i++) {
            JsonObject groupObject = jsonArray.get(i).getAsJsonObject();
            String key = groupObject.get("key").getAsString();
            if (key.toLowerCase().contains("." + site.getName().toLowerCase() + ".")) {
                int value = groupObject.get("doc_count").getAsInt();

                Graph graphApi = new Graph();
                graphApi.setTitle(key);
                graphApi.setType(GraphType.COLUMN);
                graphApi.setFillAlphas(0.9);
                graphApi.setValueField(key);
                graphApi.setAlphaField("0.6");
                graphApi.setLineAlpha(0.4);
                if (value > 0) {
                    dataProviderApi.addItem(transportCount(key, value, "api"));
                    listGraphApi.add(graphApi);
                }
            }
        }

        api_chart.setGraphs(listGraphApi);
        api_chart.setCategoryField("api");
        api_chart.setDataProvider(dataProviderApi);
    }

    private void buildTopoConsumerChart(User user) {
        JsonArray jsonArray = this.callEsApiReport("", user.getUsername(), "");
        ListDataProvider dataProviderApi = new ListDataProvider();

        List<Graph> listGraphApi = new ArrayList<>();
        for (int i = 0; i < jsonArray.size(); i++) {
            JsonObject groupObject = jsonArray.get(i).getAsJsonObject();
            String key = groupObject.get("key").getAsString();
            int value = groupObject.get("doc_count").getAsInt();

            Graph graphApi = new Graph();
            graphApi.setTitle(key);
            graphApi.setType(GraphType.COLUMN);
            graphApi.setFillAlphas(0.9);
            graphApi.setValueField(key);
            graphApi.setAlphaField("0.6");
            graphApi.setLineAlpha(0.4);
            if (value > 0) {
                dataProviderApi.addItem(transportCount(key, value, "api"));
                listGraphApi.add(graphApi);
            }
        }

        topo_consumer_chart.setGraphs(listGraphApi);
        topo_consumer_chart.setCategoryField("api");
        topo_consumer_chart.setDataProvider(dataProviderApi);
    }
}