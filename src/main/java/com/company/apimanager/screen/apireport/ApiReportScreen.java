package com.company.apimanager.screen.apireport;

import com.company.apimanager.app.RestClient;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.jmix.charts.component.SerialChart;
import io.jmix.charts.model.graph.Graph;
import io.jmix.charts.model.graph.GraphType;
import io.jmix.core.DataManager;
import io.jmix.core.Messages;
import io.jmix.core.security.CurrentAuthentication;
import io.jmix.ui.Notifications;
import io.jmix.ui.component.Label;
import io.jmix.ui.data.DataItem;
import io.jmix.ui.data.impl.ListDataProvider;
import io.jmix.ui.data.impl.MapDataItem;
import io.jmix.ui.screen.Screen;
import io.jmix.ui.screen.Subscribe;
import io.jmix.ui.screen.UiController;
import io.jmix.ui.screen.UiDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@UiController("ApiReportScreen")
@UiDescriptor("api-report-screen.xml")
public class ApiReportScreen extends Screen {
    @Autowired
    private SerialChart columnChart;
    @Autowired
    private  SerialChart apiByCallChart;
    @Autowired
    private CurrentAuthentication currentAuthentication;
    @Autowired
    private Notifications notifications;
    @Autowired
    private DataManager dataManager;
    private final Logger log = LoggerFactory.getLogger(ApiReportScreen.class);
    @Autowired
    private Messages messages;
    @Autowired
    private Label<String> totalApiLabel;
    @Autowired
    private Label<String> totalPathLabel;
    @Autowired
    private Label<String> totalOperationLabel;

    @Subscribe
    private void onInit(InitEvent event) throws IOException {
        columnChart.setVisible(false);
        apiByCallChart.setVisible(false);
    }

    @Subscribe
    public void onAfterShow(AfterShowEvent event) {
        try {
            columnChart.setVisible(true);
            apiByCallChart.setVisible(true);
            this.buildEsApiOvertimeChart("http://10.14.171.25:9200");
            this.buildEsApiByCall("http://10.14.171.25:9200");
        } catch (Exception exception)
        {
            log.info("Catch error report ES");
            log.info(exception.getMessage());
        }
    }

    private JsonObject callEsAPIOvertimeReport(String esIp) {
        String kongRouteUrl = esIp + "/fluentd-*/_search";
        RestClient restTemplate = new RestClient();
        String body = "{\n" +
                "    \"size\": 0,\n" +
                "    \"query\": {\n" +
                "        \"range\": {\n" +
                "            \"@timestamp\": {\n" +
                "                \"gte\": \"now-30d/d\",\n" +
                "                \"lte\": \"now/d\"\n" +
                "            }\n" +
                "        }\n" +
                "    },\n" +
                "    \"aggs\": {\n" +
                "        \"apis_over_time\": {\n" +
                "            \"date_histogram\": {\n" +
                "                \"field\": \"@timestamp\",\n" +
                "                \"calendar_interval\": \"day\"\n" +
                "            },\n" +
                "            \"aggs\": {\n" +
                "                \"api_count\": {\n" +
                "                    \"terms\": {\n" +
                "                        \"field\": \"route.name.keyword\"\n" +
                "                    }\n" +
                "                }\n" +
                "            }\n" +
                "        },\n" +
                "        \"total_apis\": {\n" +
                "            \"cardinality\": {\n" +
                "                \"field\": \"route.id.keyword\"\n" +
                "            }\n" +
                "        },\n" +
                "        \"total_paths\": {\n" +
                "            \"cardinality\": {\n" +
                "                \"field\": \"route.paths.keyword\"\n" +
                "            }\n" +
                "        },\n" +
                "        \"total_methods\": {\n" +
                "            \"value_count\": {\n" +
                "                \"field\": \"request.method.keyword\"\n" +
                "            }\n" +
                "        }\n" +
                "    }\n" +
                "}";

        String resultAsJsonStr = restTemplate.post(kongRouteUrl, body);
        JsonObject convertedObject = new Gson().fromJson(resultAsJsonStr, JsonObject.class);
        return convertedObject.get("aggregations").getAsJsonObject();
    }

    private void buildEsApiOvertimeChart(String esIp) {
        JsonObject jsonObject = this.callEsAPIOvertimeReport(esIp);
        JsonArray jsonArray = jsonObject.get("apis_over_time").getAsJsonObject().get("buckets").getAsJsonArray();
        ListDataProvider dataProviderApi = new ListDataProvider();
        List<Graph> listGraphApi = new ArrayList<>();
        Map<String, String> fullApiNamesMap = new HashMap<>(); 
        Set<String> apiNames = new HashSet<>();

        for (int i = 0; i < jsonArray.size(); i++) {
            JsonObject groupObject = jsonArray.get(i).getAsJsonObject();
            JsonArray apiArray = groupObject.get("api_count").getAsJsonObject().get("buckets").getAsJsonArray();
            String dateKey = groupObject.get("key_as_string").getAsString().split("T")[0];

            MapDataItem existingItem = null;
            for (DataItem item : dataProviderApi.getItems()) {
                if (item.getValue("date").equals(dateKey)) {
                    existingItem = (MapDataItem) item;
                    break;
                }
            }

            // Nếu ngày đã tồn tại, thêm apiKey và count vào mục hiện có
            if (existingItem != null) {
                for (int j = 0; j < apiArray.size(); j++) {
                    JsonObject apiObject = apiArray.get(j).getAsJsonObject();
                    String apiName = apiObject.get("key").getAsString();
                    int callCount = apiObject.get("doc_count").getAsInt();

                    // Rút gọn tên API nếu dài hơn 10 ký tự
                    String shortApiName = apiName.length() > 10 ? apiName.substring(0, 10) + "..." : apiName;
                    fullApiNamesMap.put(shortApiName, apiName); 

                    existingItem.add(shortApiName, callCount);  // Thêm callCount cho apiName vào ngày đã có
                    apiNames.add(shortApiName);
                }
            } else {
                // Nếu ngày chưa tồn tại, tạo mục mới và thêm vào dataProviderApi
                MapDataItem newItem = new MapDataItem();
                newItem.add("date", dateKey);
                for (int j = 0; j < apiArray.size(); j++) {
                    JsonObject apiObject = apiArray.get(j).getAsJsonObject();
                    String apiName = apiObject.get("key").getAsString();
                    int callCount = apiObject.get("doc_count").getAsInt();

                    // Rút gọn tên API nếu dài hơn 10 ký tự
                    String shortApiName = apiName.length() > 10 ? apiName.substring(0, 10) + "..." : apiName;
                    fullApiNamesMap.put(shortApiName, apiName); // Lưu tên đầy đủ trong bản đồ

                    newItem.add(shortApiName, callCount);
                    apiNames.add(shortApiName);
                }
                dataProviderApi.addItem(newItem);  // Thêm mục mới vào dataProviderApi
            }
        }

        // Tạo đồ thị cho mỗi API
        for (String shortApiName : apiNames) {
            Graph graphApi = new Graph();
            graphApi.setTitle(shortApiName); // Sử dụng tên đã rút gọn
            graphApi.setType(GraphType.COLUMN);
            graphApi.setFillAlphas(0.9);
            graphApi.setValueField(shortApiName);
            graphApi.setLineAlpha(0.4);

            String fullApiName = fullApiNamesMap.get(shortApiName);
            graphApi.setBalloonText("[[value]]");

            listGraphApi.add(graphApi);
        }

        totalApiLabel.setValue(jsonObject.get("total_apis").getAsJsonObject().get("value").getAsString());
        totalPathLabel.setValue(jsonObject.get("total_paths").getAsJsonObject().get("value").getAsString());
        totalOperationLabel.setValue(jsonObject.get("total_methods").getAsJsonObject().get("value").getAsString());

        columnChart.setGraphs(listGraphApi);
        columnChart.setCategoryField("date");
        columnChart.setDataProvider(dataProviderApi);
        columnChart.getCategoryAxis().setLabelRotation(45);
    }

    private void buildEsApiByCall(String esIp) {
        JsonObject jsonObject = this.callEsAPIOvertimeReport(esIp);
        JsonArray jsonArray = jsonObject.get("apis_over_time").getAsJsonObject().get("buckets").getAsJsonArray();
        ListDataProvider dataProviderApi = new ListDataProvider();
        List<Graph> listGraphApi = new ArrayList<>();
        Set<String> apiNames = new HashSet<>();
        Map<String, String> fullApiNamesMap = new HashMap<>();

        for (int i = 0; i < jsonArray.size(); i++) {
            JsonObject groupObject = jsonArray.get(i).getAsJsonObject();
            JsonArray apiArray = groupObject.get("api_count").getAsJsonObject().get("buckets").getAsJsonArray();

            for (int j = 0; j < apiArray.size(); j++) {
                MapDataItem existingItem = null;
                JsonObject apiObject = apiArray.get(j).getAsJsonObject();
                String apiName = apiObject.get("key").getAsString();
                int callCount = apiObject.get("doc_count").getAsInt();

                String shortApiName = apiName.length() > 10 ? apiName.substring(0, 10) + "..." : apiName;
                fullApiNamesMap.put(shortApiName, apiName);

                for (DataItem item : dataProviderApi.getItems()) {
                    if (item.getValue("apiName").equals(shortApiName)) {
                        existingItem = (MapDataItem) item;
                        break;
                    }
                }

                if (existingItem != null) {
                    int currentCount = (int) existingItem.getValue(shortApiName);
                    existingItem.add(shortApiName, currentCount + callCount);
                } else {
                    MapDataItem newItem = new MapDataItem();
                    newItem.add("apiName", shortApiName);
                    newItem.add(shortApiName, callCount);
                    apiNames.add(shortApiName);
                    dataProviderApi.addItem(newItem);
                }
            }
        }

        // Tạo đồ thị cho mỗi API
        for (String shortApiName : apiNames) {
            Graph graphApi = new Graph();
            graphApi.setTitle(shortApiName);
            graphApi.setType(GraphType.COLUMN);
            graphApi.setFillAlphas(0.9);
            graphApi.setValueField(shortApiName);
            graphApi.setLineAlpha(0.4);
            graphApi.setBalloonText("(" + fullApiNamesMap.get(shortApiName) + "): [[value]]");
            listGraphApi.add(graphApi);
        }

        apiByCallChart.setGraphs(listGraphApi);
        apiByCallChart.setCategoryField("apiName");
        apiByCallChart.setDataProvider(dataProviderApi);
        apiByCallChart.getCategoryAxis().setLabelRotation(45);
    }
}