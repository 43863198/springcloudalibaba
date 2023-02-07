package com.demo.service;

import java.util.Set;

public interface OrderService {

    String confirmOrder(String orderId, Set<String> lockKeys);

    void orderMesasge();
}
