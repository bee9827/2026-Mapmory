package com.mapmory.backend.upload.policy;

import com.mapmory.backend.common.exception.BusinessException;
import com.mapmory.backend.upload.UploadErrorCode;
import org.springframework.stereotype.Component;

@Component
public class UploadPolicy {

    private final long maxFileSize;
    private final int maxFilesPerRequest;

    public UploadPolicy(UploadPolicyProperties properties) {
        this.maxFileSize = properties.maxFileSize().toBytes();
        this.maxFilesPerRequest = properties.maxFilesPerRequest();
    }

    public void validateFileCount(int fileCount) {
        if (fileCount > maxFilesPerRequest) {
            throw new BusinessException(UploadErrorCode.TOO_MANY_FILES);
        }
    }

    public UploadFileType validateFile(String fileName, String contentType, long fileSize) {
        UploadFileType fileType = UploadFileType.findByContentType(contentType)
                .orElseThrow(() -> new BusinessException(UploadErrorCode.INVALID_FILE_TYPE));
        if (!fileType.matchesFileName(fileName)) {
            throw new BusinessException(UploadErrorCode.INVALID_FILE_TYPE);
        }
        if (fileSize > maxFileSize) {
            throw new BusinessException(UploadErrorCode.FILE_SIZE_EXCEEDED);
        }
        return fileType;
    }
}
