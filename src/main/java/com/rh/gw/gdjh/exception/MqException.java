package com.rh.gw.gdjh.exception;
 
/**
 * MQ异常
 */
public class MqException extends BaseException {

    private static final long serialVersionUID = 772046747932011086L;

    public MqException(String message) {
        super(message);
    }

    public MqException(String code, String message) {
        super(code, message);
    }

    public MqException(String message, Throwable e) {
        super(message, e);
    }
}