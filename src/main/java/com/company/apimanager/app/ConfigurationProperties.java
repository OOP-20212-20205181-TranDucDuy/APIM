package com.company.apimanager.app;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ConfigurationProperties {
    @Value("${bss.authenticate.url}")
    public String bssAuthUrl;

    @Value("${bss.authenticate.token}")
    public String bssAuthToken;
    @Value("${jmix.base_url}")
    public String jmixBaseUrl;

    @Value("${bss.fe_base_url}")
    public String bssBaseUrl;

    @Value("${jmix.login_key}")
    public String jmixLoginKey;

    @Value("${bss.back_caption}")
    public String bssBackCaption;

    @Value("${jmix.rate_max_per_second}")
    public long rateMaxPerSecond;

    @Value("${jmix.rate_max_per_minute}")
    public long rateMaxPerMinute;

    @Value("${jmix.rate_max_per_hour}")
    public long rateMaxPerHour;

    @Value("${spring.rabbitmq.pricing_plan_queue_name}")
    public String pricingPlanQueueName;

    @Value("${spring.rabbitmq.site_queue_name}")
    public String siteQueueName;
}
