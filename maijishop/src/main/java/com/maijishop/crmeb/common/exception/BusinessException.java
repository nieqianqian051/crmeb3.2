package com.maijishop.crmeb.common.exception;

import lombok.Getter;

/**
 * 业务异常
 * 
 * @author maijishop
 */
@Getter
public class BusinessException extends RuntimeException {
    
    private final int code;
    
    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }
    
    public BusinessException(String message) {
        super(message);
        this.code = 500;
    }
} 