package com.my.message.exception;

import com.my.message.bean.Message;

/**
 * Class RetryableOcmsException
 *
 * @author linyang
 * @date 2020/8/31
 */
public class RetryableOcmsException extends OcmsException {

    public RetryableOcmsException(String message, boolean retryable) {
        super(message);
        this.retryable = retryable;
    }

    public RetryableOcmsException(boolean retryable, Message message) {
        super(message.getReturnDesc());
        this.retryable = retryable;
        this.setMsg(message);
    }

    private boolean retryable = false;

    public boolean isRetryable() {
        return retryable;
    }

    public void setRetryable(boolean retryable) {
        this.retryable = retryable;
    }
}
