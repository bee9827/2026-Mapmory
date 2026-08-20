package com.mapmory.backend.upload.policy;

import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class ObjectKeyGenerator {

    private static final String PREFIX = "travel-records";

    public String generate(Long memberId, UploadFileType fileType) {
        return "%s/%d/%s.%s".formatted(
                PREFIX,
                memberId,
                UUID.randomUUID(),
                fileType.canonicalExtension()
        );
    }
}
