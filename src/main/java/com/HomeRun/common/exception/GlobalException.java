package com.HomeRun.common.exception;

import com.HomeRun.common.error.ErrorCode;
import lombok.Getter;

@Getter
public class GlobalException extends RuntimeException {

    private final ErrorCode errorCode;

    public GlobalException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public GlobalException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public GlobalException(ErrorCode errorCode, Throwable cause) {
        super(errorCode.getMessage(), cause);
        this.errorCode = errorCode;
    }
}
