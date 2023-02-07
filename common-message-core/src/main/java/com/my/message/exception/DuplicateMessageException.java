package com.my.message.exception;

import org.springframework.dao.DuplicateKeyException;

/**
 * Class DuplicateMessageException
 */
public class DuplicateMessageException extends DuplicateKeyException {

    /**
     * Instantiates a new Duplicate message exception.
     *
     * @param message the message
     */
    public DuplicateMessageException(String message) {
        super(message);
    }

    /**
     * Instantiates a new Duplicate message exception.
     *
     * @param msg   the msg
     * @param cause the cause
     */
    public DuplicateMessageException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
