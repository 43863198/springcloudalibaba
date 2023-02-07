/**
 * 描述:
 * 包名:com.crb.ocms.order.order.util.exceptions
 * 版本信息: 版本1.0
 * 日期:2019/2/2016:49
 * Copyright
 */
package com.my.message.exception;


/**
 * @Project crb-order
 * @PackageName com.crb.ocms.order.order.util.exceptions
 * @ClassName OcmsExceptionType
 * @Author jiahong.xing
 * @Date 2019/2/20 19:00
 * @Description 异常字段接口类
 */
public interface OcmsExceptionType {
    /**
     * getCode
     *
     * @return
     */
    String getCode();

    /**
     * getDescription
     *
     * @return
     */
    String getDescription();
}