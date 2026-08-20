package com.mapmory.backend.upload.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.mapmory.backend.member.Member;
import com.mapmory.backend.upload.dto.CreatePresignedUrlsRequest;
import com.mapmory.backend.upload.dto.CreatePresignedUrlsResponse;
import com.mapmory.backend.upload.dto.UploadFileRequest;
import com.mapmory.backend.upload.policy.ObjectKeyGenerator;
import com.mapmory.backend.upload.policy.UploadPolicy;
import com.mapmory.backend.upload.policy.UploadPolicyProperties;
import com.mapmory.backend.upload.storage.PresignedUrlProvider;
import java.net.URI;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.util.unit.DataSize;

class UploadServiceTest {

    @Test
    void S3_없이_Presigned_URL과_Object_Key를_발급한다() {
        UploadPolicyProperties properties = properties();
        FakePresignedUrlProvider provider = new FakePresignedUrlProvider();
        UploadService uploadService = new UploadService(
                new UploadPolicy(properties),
                new ObjectKeyGenerator(),
                provider,
                properties
        );
        CreatePresignedUrlsRequest request = new CreatePresignedUrlsRequest(List.of(
                new UploadFileRequest("jeju-trip", "IMAGE/JPEG", 3_145_728L),
                new UploadFileRequest("map.webp", "image/webp", 1024L)
        ));

        CreatePresignedUrlsResponse response = uploadService.createPresignedUrls(member(), request);

        assertThat(response.uploads()).hasSize(2);
        assertThat(response.uploads().getFirst().objectKey()).startsWith("travel-records/10/")
                .endsWith(".jpg");
        assertThat(response.uploads().getFirst().presignedUrl())
                .startsWith("https://upload.example/travel-records/10/");
        assertThat(response.uploads().getFirst().method()).isEqualTo("PUT");
        assertThat(response.uploads().getFirst().contentType()).isEqualTo("image/jpeg");
        assertThat(response.uploads().getFirst().expiresIn()).isEqualTo(300L);
        assertThat(provider.requests).containsExactly(
                new PresignRequest(
                        response.uploads().getFirst().objectKey(),
                        "image/jpeg",
                        3_145_728L,
                        Duration.ofMinutes(5)
                ),
                new PresignRequest(
                        response.uploads().get(1).objectKey(),
                        "image/webp",
                        1024L,
                        Duration.ofMinutes(5)
                )
        );
    }

    private static UploadPolicyProperties properties() {
        return new UploadPolicyProperties(
                DataSize.ofMegabytes(10),
                10,
                Duration.ofMinutes(5)
        );
    }

    private static Member member() {
        Member member = Member.of("테스터", UUID.randomUUID());
        ReflectionTestUtils.setField(member, "id", 10L);
        return member;
    }

    private static class FakePresignedUrlProvider implements PresignedUrlProvider {

        private final List<PresignRequest> requests = new ArrayList<>();

        @Override
        public URI createPresignedPutUrl(
                String objectKey,
                String contentType,
                long contentLength,
                Duration expiration
        ) {
            requests.add(new PresignRequest(objectKey, contentType, contentLength, expiration));
            return URI.create("https://upload.example/" + objectKey);
        }
    }

    private record PresignRequest(
            String objectKey,
            String contentType,
            long contentLength,
            Duration expiration
    ) {
    }
}
