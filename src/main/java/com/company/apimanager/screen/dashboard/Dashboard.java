package com.company.apimanager.screen.dashboard;

import com.company.apimanager.entity.Site;
import com.company.apimanager.entity.User;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.jmix.core.DataManager;
import io.jmix.core.security.CurrentAuthentication;
import io.jmix.ui.screen.Screen;
import io.jmix.ui.screen.Subscribe;
import io.jmix.ui.screen.UiController;
import io.jmix.ui.screen.UiDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@UiController("Dashboard")
@UiDescriptor("dashboard.xml")
public class Dashboard extends Screen {
    @Autowired
    private CurrentAuthentication currentAuthentication;
    @Autowired
    private DataManager dataManager;

    private static Logger log = LoggerFactory.getLogger(Dashboard.class);
    @Subscribe
    private void onInit(InitEvent event) throws IOException {
        User user = (User) currentAuthentication.getUser();

    }

    private JsonArray callApiSuccess (String esIp, String username) throws IOException {
        String kongRouteUrl = esIp + "/fluentd-*/_search";
        com.company.apimanager.app.RestClient restTemplate = new com.company.apimanager.app.RestClient();
        String body = "{\n" +
                "   \"query\": {\n" +
                "        \"bool\": {\n" +
                "            \"must\": [\n" +
                "                {\n" +
                "                     \"exists\":{\n" +
                "                        \"field\":\"route\"\n" +
                "                    }\n" +
                "                },\n" +
                "                {\n" +
                "                    \"wildcard\": {\n" +
                "                        \"consumer.username\": {\n" +
                "                            \"value\": \""+username+"*\n" +
                "                        }\n" +
                "                    }\n" +
                "                }\n" +
                "            ],\n" +
                "            \"filter\": [\n" +
                "                {\n" +
                "                    \"range\":{\n" +
                "                        \"response.status\": {\n" +
                "                            \"gte\" : 399\n" +
                "                        }\n" +
                "                    }\n" +
                "                }\n" +
                "            ]\n" +
                "        }\n" +
                "    },\n" +
                "  \"aggs\": {\n" +
                "    \"group_by_url\": {\n" +
                "      \"terms\": {\n" +
                "        \"field\": \"request.url.keyword\"\n" +
                "      }\n" +
                "    },\n" +
                "    \"group_by_consumer\":{\n" +
                "        \"terms\":{\n" +
                "            \"field\": \"consumer.username.keyword\"\n" +
                "        }\n" +
                "    },\n" +
                "    \"group_by_days\":{\n" +
                "        \"date_histogram\": {\n" +
                "            \"field\": \"started_at\", \n" +
                "            \"calendar_interval\": \"day\",\n" +
                "            \"format\": \"yyyy-MM-dd\"\n" +
                "      }\n" +
                "    }    \n" +
                "  },\n" +
                "  \"_source\":false,\n" +
                "  \"size\":0\n" +
                "}";
        String resultAsJsonStr = restTemplate.post(kongRouteUrl, body);
        JsonObject convertedObject = new Gson().fromJson(resultAsJsonStr, JsonObject.class);
        return  convertedObject.get("aggregations").getAsJsonObject().get("consumer").getAsJsonObject().get("buckets").getAsJsonArray();
    }
    private JsonArray callApiError (String esIp, String username) throws IOException {
        String kongRouteUrl = esIp + "/fluentd-*/_search";
        com.company.apimanager.app.RestClient restTemplate = new com.company.apimanager.app.RestClient();
        String body = "{\n" +
                "   \"query\": {\n" +
                "        \"bool\": {\n" +
                "            \"must\": [\n" +
                "                {\n" +
                "                     \"exists\":{\n" +
                "                        \"field\":\"route\"\n" +
                "                    }\n" +
                "                },\n" +
                "                {\n" +
                "                    \"wildcard\": {\n" +
                "                        \"consumer.username\": {\n" +
                "                            \"value\": \""+username+"*\"\n" +
                "                        }\n" +
                "                    }\n" +
                "                }\n" +
                "            ],\n" +
                "            \"filter\": [\n" +
                "                {\n" +
                "                    \"range\":{\n" +
                "                        \"response.status\": {\n" +
                "                            \"gte\" : 200,\n" +
                "                            \"lte\" : 400\n" +
                "                        }\n" +
                "                    }\n" +
                "                }\n" +
                "            ]\n" +
                "        }\n" +
                "    },\n" +
                "  \"aggs\": {\n" +
                "    \"group_by_url\": {\n" +
                "      \"terms\": {\n" +
                "        \"field\": \"request.url.keyword\"\n" +
                "      }\n" +
                "    },\n" +
                "    \"group_by_consumer\":{\n" +
                "        \"terms\":{\n" +
                "            \"field\": \"consumer.username.keyword\"\n" +
                "        }\n" +
                "    },\n" +
                "    \"group_by_days\":{\n" +
                "        \"date_histogram\": {\n" +
                "            \"field\": \"started_at\", \n" +
                "            \"calendar_interval\": \"day\",\n" +
                "            \"format\": \"yyyy-MM-dd\"\n" +
                "      }\n" +
                "    }    \n" +
                "  },\n" +
                "  \"_source\":false,\n" +
                "  \"size\":0\n" +
                "}";
        String resultAsJsonStr = restTemplate.post(kongRouteUrl, body);
        JsonObject convertedObject = new Gson().fromJson(resultAsJsonStr, JsonObject.class);
        return  convertedObject.get("aggregations").getAsJsonObject().get("consumer").getAsJsonObject().get("buckets").getAsJsonArray();
    }
}