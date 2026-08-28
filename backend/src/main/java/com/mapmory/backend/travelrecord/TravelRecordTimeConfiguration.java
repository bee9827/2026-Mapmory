package com.mapmory.backend.travelrecord;

import java.time.Clock;
import java.time.ZoneId;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class TravelRecordTimeConfiguration {

    private static final ZoneId SERVICE_ZONE = ZoneId.of("Asia/Seoul");

    @Bean
    public Clock travelRecordClock() {
        return Clock.system(SERVICE_ZONE);
    }
}
