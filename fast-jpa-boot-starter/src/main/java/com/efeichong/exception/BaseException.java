package com.efeichong.exception;

/**
 * @author lxk
 * @date 2021/2/4
 * @description 基础异常，基础包的异常类型
 */
public class BaseException extends RuntimeException {
    private Integer code = 0;
    private String message;

    public BaseException() {
        super("服务器处理失败!");
    }

    public BaseException(Throwable cause) {
        super(cause);
        this.message = cause.getMessage();
    }

    public BaseException(String message) {
        super(message);
        this.message = message;
    }

    public BaseException(Integer code, String message) {
        super(message);
        this.code = code;
    }

    public BaseException(String message, Throwable error) {
        super(message, error);
        this.message = message;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
