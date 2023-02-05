package com.my.distribute;

public enum LockType {
    /**
     * 默认值
     */
    Default,
    /**
     * 库存锁
     */
    OCMS_STOCK_LOCK,

    /**
     * 库存占用锁
     */
    OCMS_STOCK_OCCUPY_LOCK,

    /**
     * 库存包装物锁
     */
    OCMS_STOCK_PACKING_LOCK,
    /**
     * 订单锁
     */
    OCMS_ORDER_LOCK,
    /**
     * 账户锁
     */
    OCMS_ACCOUNT_LOCK,
    /**
     * 价格锁
     */
    OCMS_PRICE_LOCK,
    /**
     * 购物车锁
     */
    OCMS_CAR_LOCK,

    /**
     * 支付锁
     */
    OCMS_PAY_LOCK
}
