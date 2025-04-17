package org.example.videoapi.pojo.dto;

import lombok.Data;

@Data
public class ResultResponse<T> {
    private int code;
    private String message;
    private T data;

    public static <T> ResultResponse<T> success(String message,T data) {
        ResultResponse<T> response = new ResultResponse<>();
        response.setCode(200);
        response.setMessage(message);
        response.setData(data);
        return response;
    }
    public static <T> ResultResponse<T> error(int code, String message) {
        ResultResponse<T> response = new ResultResponse<>();
        response.setCode(code);
        response.setMessage(message);
        response.setData(null);
        return response;
    }
}
