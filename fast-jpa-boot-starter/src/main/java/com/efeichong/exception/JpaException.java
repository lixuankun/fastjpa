package com.efeichong.exception;

/**
 * @author lxk
 * @date 2022/5/28
 * @description
 */
public class JpaException extends BaseException {
    private Integer code = 0;
    private String message;

    public JpaException() {
        super("服务器处理失败!");
    }

    public JpaException(Throwable cause) {
        super(cause);
        this.message = cause.getMessage();
    }

    public JpaException(String message) {
        super(message);
        this.message = message;
    }

    public JpaException(Integer code, String message) {
        super(message);
        this.code = code;
    }

    public JpaException(String message, Throwable error) {
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
