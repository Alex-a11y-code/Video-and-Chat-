package org.example.videoapi.handler;
import org.example.videoapi.pojo.dto.ResultResponse;
import org.example.videoapi.exception.ApiException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ApiException.class)
    public ResultResponse<?> handleApiException(ApiException e) {
        return ResultResponse.error(e.getCode(), e.getMessage());
    }
    @ExceptionHandler(Exception.class)
    public ResultResponse<?> handleException(Exception e) {

        return ResultResponse.error(500, "服务器内部错误");
    }
}
