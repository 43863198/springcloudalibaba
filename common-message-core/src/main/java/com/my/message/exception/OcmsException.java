/**
 * 描述:
 * 包名:com.crb.ocms.demo.domain.util.exceptions
 * 版本信息: 版本1.0
 * 日期:2019/2/2015:29
 * Copyright
 */
package com.my.message.exception;

import com.my.message.bean.Message;
import org.springframework.util.ObjectUtils;


/**
 * @Project crb-demo
 * @PackageName com.crb.ocms.demo.domain.util.exceptions
 * @ClassName OCmsExceptions
 * @Author jiahong.xing
 * @Date 2019/2/20 19:00
 * @Description 异常类
 */
public class OcmsException extends RuntimeException {
    private static final long serialVersionUID = 4586562848280721408L;

    private Message msg;


    public OcmsException() {
        super();
    }

    // 异常接收
    public OcmsException(OcmsExceptionType dmsExceptionType, String... message) {
        if (ObjectUtils.isEmpty(message) && ObjectUtils.isEmpty(dmsExceptionType)) {
            this.setMsg(Message.error("未知异常"));
            return;
        }
        if (ObjectUtils.isEmpty(message)) {
            String errorMessage = dmsExceptionType.getDescription();
            this.setMsg(Message.error(errorMessage, dmsExceptionType));
            return;
        } else if (ObjectUtils.isEmpty(dmsExceptionType)) {
            this.setMsg(Message.error(message[0]));
        } else {
            String errorMessage = null;
            if (dmsExceptionType.getDescription().contains("%s")) {
                errorMessage = String.format(dmsExceptionType.getDescription(), message);
            } else {
                errorMessage = message[0];
            }
            this.setMsg(Message.error(errorMessage, dmsExceptionType));
        }
    }

    // 异常接收
    public OcmsException(OcmsExceptionType dmsExceptionType, Object... message) {
        if (ObjectUtils.isEmpty(message) && ObjectUtils.isEmpty(dmsExceptionType)) {
            this.setMsg(Message.error("未知异常"));
            return;
        }
        if (ObjectUtils.isEmpty(message)) {
            String errorMessage = dmsExceptionType.getDescription();
            this.setMsg(Message.error(errorMessage, dmsExceptionType));
            return;
        } else if (ObjectUtils.isEmpty(dmsExceptionType)) {
            this.setMsg(Message.error(message[0].toString()));
        } else {
            this.setMsg(Message.error(ObjectUtils.isEmpty(message) ? dmsExceptionType.getDescription() : message[0], dmsExceptionType));
        }
    }

    //不暴露为公共服务的异常操作
    public void codeAndMessage(String errorCode, String message) {
        this.setMsg(Message.getMessage(message, errorCode, null));
    }

    public OcmsException(OcmsExceptionType ocmsExceptionType) {
        super(ocmsExceptionType.getDescription());
        this.setMsg(Message.error(ocmsExceptionType.getDescription(), ocmsExceptionType));
    }

    public OcmsException(String message) {
        super(message);
        this.setMsg(Message.error(message));
    }

    public OcmsException(String message, Object data) {
        super(message);
        this.setMsg(Message.errorByData(message, data));
    }

    public Message getMsg() {
        return msg;
    }

    public void setMsg(Message msg) {
        this.msg = msg;
    }
}