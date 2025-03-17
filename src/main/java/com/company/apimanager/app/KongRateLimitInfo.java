package com.company.apimanager.app;

import org.springframework.stereotype.Component;

@Component
public class KongRateLimitInfo {
    private long max_per_second;
    private long max_per_minute;
    private long max_per_hour;

    public KongRateLimitInfo() {

    }
    public KongRateLimitInfo(long max_per_second, long max_per_minute, long max_per_hour) {
        this.max_per_second = max_per_second;
        this.max_per_minute = max_per_minute;
        this.max_per_hour = max_per_hour;
    }
    public long getMax_per_hour() {
        return max_per_hour;
    }

    public long getMax_per_minute() {
        return max_per_minute;
    }

    public long getMax_per_second() {
        return max_per_second;
    }

    public void setMax_per_hour(long max_per_hour) {
        this.max_per_hour = max_per_hour;
    }

    public void setMax_per_minute(long max_per_minute) {
        this.max_per_minute = max_per_minute;
    }

    public void setMax_per_second(long max_per_second) {
        this.max_per_second = max_per_second;
    }
}