package com.company.apimanager.app;

import com.company.apimanager.entity.Statistic;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.jmix.core.Entity;
import io.jmix.core.security.Authenticated;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import io.jmix.core.DataManager;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.List;

public class StatisticElastic implements Job {
    @Autowired
    private DataManager dataManager;
    private static final Logger log = LoggerFactory.getLogger(StatisticElastic.class);

    @Authenticated
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        JobDataMap jobDataMap = context.getJobDetail().getJobDataMap();
        SimpleDateFormat parser = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Calendar cal = Calendar.getInstance();

        String startTime = ZonedDateTime.now(ZoneOffset.UTC).withNano(0).toString();
        String endTime = ZonedDateTime.now(ZoneOffset.UTC).plusHours(1).withNano(0).toString();
        try {
            JsonArray consumerReport = this.callEsReport(jobDataMap.getString("url"), startTime, endTime);
            for (JsonElement element:consumerReport) {
                JsonObject elementObject = element.getAsJsonObject();
                long timestamp = elementObject.get("key").getAsLong();
                Integer count = elementObject.get("doc_count").getAsInt();
                Date time = new Date(timestamp);
                cal.setTime(time);
                time = cal.getTime();
                Statistic statistic = dataManager.create(Statistic.class);
                statistic.setDate(parser.format(time));
                statistic.setTotal(count);
                dataManager.save(statistic);
            }


        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        log.info("Sample job is executed");
    }

    private JsonArray callEsReport (String esIp, String startAt, String endAt) throws IOException {
        String kongRouteUrl = esIp + "/logstash/_search";
        RestClient restTemplate = new RestClient();
        String body = "{\n" +
                "    \"query\": {\n" +
                "        \"bool\": {\n" +
                "            \"must\": [\n" +
                "                {\n" +
                "                    \"range\": {\n" +
                "                        \"@timestamp\": {\n" +
                "                            \"gte\": \"2022-09-24T00:00:00Z\",\n" +
                "                            \"lte\": \"2022-09-24T01:00:00Z\"\n" +
                "                        }\n" +
                "                    }\n" +
                "                }\n" +
                "            ]\n" +
                "        }\n" +
                "    },\n" +
                "    \"aggs\": {\n" +
                "        \"by_second\": {\n" +
                "            \"date_histogram\": {\n" +
                "                \"field\": \"started_at\",\n" +
                "                \"calendar_interval\": \"second\"\n" +
                "            }\n" +
                "        }\n" +
                "    }\n" +
                "}";

        String resultAsJsonStr = restTemplate.post(kongRouteUrl, body);
        JsonObject convertedObject = new Gson().fromJson(resultAsJsonStr, JsonObject.class);
        return  convertedObject.get("aggregations").getAsJsonObject().get("by_second").getAsJsonObject().get("buckets").getAsJsonArray();
    }
}
