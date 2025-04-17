package org.example.videoapi.exception;

public class ApiException extends RuntimeException {
    private final int code;
    public ApiException(String message) {
        super(message);
        this.code = 500;
    }
    public ApiException(String message, int code) {
        super(message);
        this.code = code;
    }
    public int getCode() {
        return code;
    }
}
