package com.mapmory.backend.upload.policy;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.context.properties.bind.validation.BindValidationException;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.boot.validation.autoconfigure.ValidationAutoConfiguration;

class UploadPolicyPropertiesTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(ValidationAutoConfiguration.class))
            .withUserConfiguration(UploadPolicyConfig.class)
            .withPropertyValues(
                    "upload.policy.allowed-content-types[0]=image/jpeg",
                    "upload.policy.max-file-size=10MB",
                    "upload.policy.max-files-per-request=10"
            );

    @ParameterizedTest
    @ValueSource(strings = {"PT1S", "PT604800S"})
    void Presigned_URL_만료_시간의_경곗값을_허용한다(String expiration) {
        contextRunner
                .withPropertyValues("upload.policy.presigned-url-expiration=" + expiration)
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    assertThat(context.getBean(UploadPolicyProperties.class).presignedUrlExpiration())
                            .isEqualTo(Duration.parse(expiration));
                });
    }

    @ParameterizedTest
    @ValueSource(strings = {"PT0S", "-PT1S", "PT604801S"})
    void 범위를_벗어난_Presigned_URL_만료_시간은_속성_바인딩에_실패한다(String expiration) {
        contextRunner
                .withPropertyValues("upload.policy.presigned-url-expiration=" + expiration)
                .run(context -> {
                    assertThat(context).hasFailed();
                    assertThat(context.getStartupFailure())
                            .hasRootCauseInstanceOf(BindValidationException.class);
                });
    }
}
