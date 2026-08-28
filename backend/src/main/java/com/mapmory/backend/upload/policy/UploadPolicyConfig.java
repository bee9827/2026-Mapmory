package com.mapmory.backend.upload.policy;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(UploadPolicyProperties.class)
public class UploadPolicyConfig {
}
