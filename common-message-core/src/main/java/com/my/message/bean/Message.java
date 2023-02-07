package com.my.message.bean;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.my.message.exception.OcmsExceptionType;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;


/**
 * @Project ocms-product
 * @PackageName com.crb.ocms.product.domain.util.bean
 * @ClassName Message
 * @Author jiahong.xing
 * @Date 2019/2/20 19:00
 * @Description 返回的消息对象
 */
@Data
public class Message implements Serializable {

    // 成功失败基础数据 ,成功200，错误1100
    public static final String SUCCESS_MSG = "success";
    public static final String SUCCESS_CODE = "SUCCESS_CODE";
    public static final String ERROR_MSG = "系统错误";
    public static final String ERROR_CODE = "EROOR_CODE";
    private static final Logger logger = LoggerFactory.getLogger(Message.class);
    private static final long serialVersionUID = -5806476053133922432L;

    private Message() {

    }

    /**
     * 内容、错误描述
     */
    @JSONField(name = "RETURN_DESC")
    private String returnDesc;

    /**
     * 错误码
     */
    @JSONField(name = "RETURN_CODE")
    private String returnCode;


    /**
     * data数据
     */
    @JSONField(name = "RETURN_DATA", serialzeFeatures = SerializerFeature.WriteMapNullValue)
    private Object returnData;

    /**
     * 时间戳
     */
//    @JSONField(name = "RETURN_STAMP")
//    private final String returnStamp = DateUtils.formatDate(new Date(), "yyyy-MM-dd HH:mm:ss.SSS");

//    //接口流水号，可用于定位接口流程和异常定位
//    @JSONField(name = "SNO")
//    private String sno = UUIDGenerator.generate();

    // 得到信息类
    public static Message getMessage(String msg, String code, Object data) {
        Message message = new Message();
//        if (StringUtils.isEmpty(code)) {
//            code = SUCCESS_CODE;
//        }
//        if (StringUtils.isEmpty(msg)) {
//            if (SUCCESS_CODE.equals(code)) {
//                msg = SUCCESS_MSG;
//            } else {
//                msg = ERROR_MSG;
//            }
//        }
        message.setReturnDesc(msg);
        message.setReturnCode(code);

        //根据中台的标准，建议使用base64
        String data_base64;
        if (null != data) {
//            data_base64 = Base64Util.encode(JSONObject.toJSONString(data));
        } else {
            data_base64 = "";
        }
//        message.setReturnData(data_base64);
        message.setReturnData(data);
        return message;
    }

    /**
     * 返回一个失败的消息
     */

    public static Message error() {
        return error(ERROR_MSG);
    }

    /**
     * 返回一个失败的消息
     */

    public static Message error(String msg) {
        Message message = getMessage(msg, ERROR_CODE, null);
        logger.error("msg:{},code:{},json:{}", msg, ERROR_CODE, message.toString());
        return message;
    }

    /**
     * 返回一个失败的消息
     */
    public static Message error(String msg, OcmsExceptionType dmsExceptionType) {
        Message message = getMessage(msg, dmsExceptionType.getCode(), null);
        logger.error("msg:{},code:{}", msg, dmsExceptionType.getCode());
        return message;
    }
    /**
     * 返回一个失败的消息
     */
    public static Message error(Object msg, OcmsExceptionType dmsExceptionType) {
//        if (ObjectUtils.isEmpty(msg)){
//            msg = dmsExceptionType.getDescription();
//        }
        Message message = getMessage(msg.toString(), dmsExceptionType.getCode(), null);
        logger.error("msg:{},code:{}", msg, dmsExceptionType.getCode());
        return message;
    }

    /**
     * 返回一个失败的消息
     *
     * @param msg
     * @param data
     * @return
     */
    public static Message errorByData(String msg, Object data) {
        Message message = getMessage(msg, ERROR_CODE, data);
        logger.error("msg:{},code:{},data:{}", msg, ERROR_CODE, data);
        return message;
    }

    //返回成功
    public static Message success() {
        return success(SUCCESS_MSG);
    }

    /**
     * 成功的消息
     */

    public static Message success(String msg) {
        return success(msg, null);
    }

    /**
     * 成功的消息
     */

    public static Message success(String msg, Object data) {
        return getMessage(msg, SUCCESS_CODE, data);
    }

    /**
     * 成功的消息
     */
    public static Message successByData(Object data) {
        return success(SUCCESS_MSG, data);
    }

    //判断是否成功
    public boolean onSuccess() {
        if (SUCCESS_CODE.equals(this.returnCode)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 转化为json格式
     *
     * @return
     */
    public JSONObject toJson() {
        JSONObject json = (JSONObject) JSONObject.toJSON(this);
        return json;
    }

    @Override
    public String toString() {
        return toJson().toString();
    }
}
