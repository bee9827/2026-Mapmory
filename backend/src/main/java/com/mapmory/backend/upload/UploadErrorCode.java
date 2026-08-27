package com.mapmory.backend.upload;

import com.mapmory.backend.common.exception.ErrorCode;
import com.mapmory.backend.common.exception.ErrorKind;

public enum UploadErrorCode implements ErrorCode {

    INVALID_FILE_TYPE(
            "허용되지 않은 파일 형식입니다.",
            "jpeg, png, webp, heic 형식의 이미지만 업로드할 수 있습니다."
    ),
    FILE_SIZE_EXCEEDED(
            "파일 크기가 너무 큽니다.",
            "파일 크기가 업로드 가능한 최대 크기를 초과했습니다."
    ),
    TOO_MANY_FILES(
            "파일 개수가 너무 많습니다.",
            "한 번에 업로드할 수 있는 최대 파일 개수를 초과했습니다."
    );

    private final String title;
    private final String detail;

    UploadErrorCode(String title, String detail) {
        this.title = title;
        this.detail = detail;
    }

    @Override
    public ErrorKind kind() {
        return ErrorKind.INVALID_INPUT;
    }

    @Override
    public String code() {
        return name();
    }

    @Override
    public String title() {
        return title;
    }

    @Override
    public String detail() {
        return detail;
    }
}
