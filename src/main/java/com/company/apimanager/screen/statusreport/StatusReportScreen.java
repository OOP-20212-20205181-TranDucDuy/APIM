package com.company.apimanager.screen.statusreport;

import com.company.apimanager.app.RestClient;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.jmix.charts.component.PieChart;
import io.jmix.charts.component.SerialChart;
import io.jmix.charts.model.graph.Graph;
import io.jmix.charts.model.graph.GraphType;
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
import io.jmix.charts.model.axis.CategoryAxis;

import java.util.*;

@UiController("StatusReportScreen")
@UiDescriptor("status-report-screen.xml")
public class StatusReportScreen extends Screen {
    private static final Logger log = LoggerFactory.getLogger(StatusReportScreen.class);
    @Autowired
    private PieChart donutChart;
    @Autowired
    private SerialChart lineChart;
    @Autowired
    private Label<String> totalApiCallsLabel;
    @Autowired
    private Label<String> totalErrorsLabel;

    @Subscribe
    protected void onInit(InitEvent event) {
       donutChart.setVisible(false);
       lineChart.setVisible(false);
    }

    @Subscribe
    public void onAfterShow(AfterShowEvent event) {
        try {
            donutChart.setVisible(true);
            lineChart.setVisible(true);
            this.buildEsStatusByCall("http://10.14.171.25:9200");
            this.buildEsSuccessRateChart("http://10.14.171.25:9200");
        } catch (Exception exception)
        {
            log.info("Catch error report ES");
            log.info(exception.getMessage());
        }
    }

    private void buildEsStatusByCall(String esIp) {
        JsonObject jsonObject = this.callEsStatusReport(esIp);
        JsonArray statusCodes = jsonObject.get("status_codes").getAsJsonObject().get("buckets").getAsJsonArray();
        ListDataProvider dataProvider = new ListDataProvider();
        String totalApiCalls = jsonObject.get("total_api_call").getAsJsonObject().get("value").getAsString();
        String totalErrors = jsonObject.get("total_error").getAsJsonObject().get("doc_count").getAsString();

        for(JsonElement statusCode : statusCodes) {
            String title = statusCode.getAsJsonObject().get("key").getAsString();
            String value = statusCode.getAsJsonObject().get("doc_count").getAsString();
            dataProvider.addItem(new MapDataItem().add("title", title).add("value", Integer.parseInt(value)));
        }
        donutChart.setDataProvider(dataProvider);
        totalApiCallsLabel.setValue(totalApiCalls);
        totalErrorsLabel.setValue(totalErrors);
    }

    private void buildEsSuccessRateChart(String esIp) {
        JsonObject jsonObject = this.callEsStatusReport(esIp);
        JsonArray statusSuccessRates = jsonObject.get("status_success_rate").getAsJsonObject()
                .get("per_day").getAsJsonObject().get("buckets").getAsJsonArray();
        ListDataProvider dataProviderApi = new ListDataProvider();
        List<Graph> listGraphApi = new ArrayList<>();

        for(JsonElement statusCode : statusSuccessRates) {
            String date = statusCode.getAsJsonObject().get("key_as_string").getAsString().split("T")[0];
            String value = statusCode.getAsJsonObject().get("doc_count").getAsString();
            dataProviderApi.addItem(new MapDataItem().add("date", date).add("value", value));
        }

        lineChart.setDataProvider(dataProviderApi);
        CategoryAxis categoryAxis = new CategoryAxis();
        categoryAxis.setTitle("Date");
        categoryAxis.setLabelRotation(-45);
        lineChart.setCategoryAxis(categoryAxis);
    }


    private JsonObject callEsStatusReport(String esIp) {
        String kongRouteUrl = esIp + "/fluentd-*/_search";
        RestClient restTemplate = new RestClient();
        String body = "{\n" +
                "    \"size\": 0,\n" +
                "    \"aggs\": {\n" +
                "        \"status_codes\": {\n" +
                "            \"terms\": {\n" +
                "                \"field\": \"response.status\"\n" +
                "            }\n" +
                "        },\n" +
                "        \"total_api_call\": {\n" +
                "            \"value_count\": {\n" +
                "                \"field\": \"response.status\"\n" +
                "            }\n" +
                "        },\n" +
                "        \"total_error\": {\n" +
                "            \"filter\": {\n" +
                "                \"range\": {\n" +
                "                    \"response.status\": {\n" +
                "                        \"gte\": 400\n" +
                "                    }\n" +
                "                }\n" +
                "            }\n" +
                "        },\n" +
                "        \"status_success_rate\": {\n" +
                "            \"filter\": {\n" +
                "                \"term\": {\n" +
                "                    \"response.status\": 200\n" +
                "                }\n" +
                "            },\n" +
                "            \"aggs\": {\n" +
                "                \"per_day\": {\n" +
                "                    \"date_histogram\": {\n" +
                "                        \"field\": \"@timestamp\",\n" +
                "                        \"calendar_interval\": \"day\"\n" +
                "                    }\n" +
                "                }\n" +
                "            }\n" +
                "        }\n" +
                "    }\n" +
                "}";

        String resultAsJsonStr = restTemplate.post(kongRouteUrl, body);
        JsonObject convertedObject = new Gson().fromJson(resultAsJsonStr, JsonObject.class);
        return convertedObject.get("aggregations").getAsJsonObject();
    }
}