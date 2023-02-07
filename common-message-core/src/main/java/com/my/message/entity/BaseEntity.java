package com.my.message.entity;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class BaseEntity implements Serializable {

    protected String remarks = "";

    protected String delFlag = "N";

    private String createdUser = "crb-message-core";

    private String updatedUser = "crb-message-core";
//    /**
//     * 乐观锁
//     */
//    protected Integer optCounter = 0;

    protected Date createdDate = new Date();

    protected Date updatedDate = new Date();

    private String isTest = "N";


}
