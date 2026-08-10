package com.mapmory.backend.common.exception;

import java.io.Serializable;

public interface ErrorCode extends Serializable {

    ErrorKind kind();

    String code();

    String title();

    String detail();
}
