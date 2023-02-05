package com.demo.service;

import java.util.Set;

public interface StockService {

    String confirmStock(String stockId, Set<String> setKeys);
}
