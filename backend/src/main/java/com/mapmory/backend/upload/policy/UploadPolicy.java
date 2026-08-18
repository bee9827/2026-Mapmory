package com.mapmory.backend.upload.policy;

import com.mapmory.backend.common.exception.BusinessException;
import com.mapmory.backend.upload.UploadErrorCode;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class UploadPolicy {

    private final Set<String> allowedContentTypes;
    private final long maxFileSize;
    private final int maxFilesPerRequest;

    public UploadPolicy(UploadPolicyProperties properties) {
        this.allowedContentTypes = properties.allowedContentTypes().stream()
                .map(UploadPolicy::normalize)
                .collect(Collectors.toUnmodifiableSet());
        this.maxFileSize = properties.maxFileSize().toBytes();
        this.maxFilesPerRequest = properties.maxFilesPerRequest();
    }

    public void validateFileCount(int fileCount) {
        if (fileCount > maxFilesPerRequest) {
            throw new BusinessException(UploadErrorCode.TOO_MANY_FILES);
        }
    }

    public void validateFile(String fileName, String contentType, long fileSize) {
        String normalizedContentType = normalize(contentType);
        if (!allowedContentTypes.contains(normalizedContentType)
                || !hasMatchingExtension(fileName, normalizedContentType)) {
            throw new BusinessException(UploadErrorCode.INVALID_FILE_TYPE);
        }
        if (fileSize > maxFileSize) {
            throw new BusinessException(UploadErrorCode.FILE_SIZE_EXCEEDED);
        }
    }

    private static String normalize(String contentType) {
        if (contentType == null) {
            return "";
        }
        return contentType.toLowerCase(Locale.ROOT);
    }

    private static boolean hasMatchingExtension(String fileName, String contentType) {
        if (fileName == null) {
            return false;
        }

        String normalizedFileName = fileName.trim().toLowerCase(Locale.ROOT);
        int extensionIndex = normalizedFileName.lastIndexOf('.');
        if (extensionIndex <= 0 || extensionIndex == normalizedFileName.length() - 1) {
            return false;
        }

        String extension = normalizedFileName.substring(extensionIndex + 1);
        return switch (contentType) {
            case "image/jpeg" -> extension.equals("jpg") || extension.equals("jpeg");
            case "image/png" -> extension.equals("png");
            case "image/webp" -> extension.equals("webp");
            case "image/heic" -> extension.equals("heic");
            default -> false;
        };
    }
}
