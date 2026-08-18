package com.mapmory.backend.upload.policy;

import com.mapmory.backend.common.exception.BusinessException;
import com.mapmory.backend.upload.UploadErrorCode;
import java.util.Locale;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class ObjectKeyGenerator {

    private static final String PREFIX = "travel-records";

    public String generate(Long memberId, String fileName) {
        return "%s/%d/%s.%s".formatted(
                PREFIX,
                memberId,
                UUID.randomUUID(),
                extensionOf(fileName)
        );
    }

    private static String extensionOf(String fileName) {
        if (fileName == null) {
            throw new BusinessException(UploadErrorCode.INVALID_FILE_TYPE);
        }

        String normalizedFileName = fileName.trim();
        int extensionIndex = normalizedFileName.lastIndexOf('.');
        if (extensionIndex <= 0 || extensionIndex == normalizedFileName.length() - 1) {
            throw new BusinessException(UploadErrorCode.INVALID_FILE_TYPE);
        }

        String extension = normalizedFileName.substring(extensionIndex + 1)
                .toLowerCase(Locale.ROOT);
        return switch (extension) {
            case "jpg", "jpeg", "png", "webp", "heic" -> extension;
            default -> throw new BusinessException(UploadErrorCode.INVALID_FILE_TYPE);
        };
    }
}
