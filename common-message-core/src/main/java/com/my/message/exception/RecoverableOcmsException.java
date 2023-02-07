package com.my.message.exception;

import com.my.message.bean.Message;

/**
 * Class RecoverableOcmsException
 *
 * @author linyang
 * @date 2020/8/31
 */
public class RecoverableOcmsException extends OcmsException {

    public RecoverableOcmsException(boolean recoverable) {
        this.recoverable = recoverable;
    }
    public RecoverableOcmsException(boolean recoverable,String message) {
        super(message);
        this.recoverable = recoverable;
    }

    public RecoverableOcmsException(boolean recoverable, Message message) {
        super(message.getReturnDesc());
        this.recoverable = recoverable;
        this.setMsg(message);
    }
    private boolean recoverable = false;

    public boolean isRecoverable() {
        return recoverable;
    }

    public void setRecoverable(boolean recoverable) {
        this.recoverable = recoverable;
    }
}
