package com.mapmory.backend.travelrecord.mapsummary.policy;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
class MapSummaryConfiguration {

    @Bean
    LevelPolicy levelPolicy() {
        return LevelPolicy.standard();
    }
}
