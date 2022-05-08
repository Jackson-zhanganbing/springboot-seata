package com.zab.seataorderservice.service;


import com.zab.seataorderservice.domain.Order;

/**
 * service层
 *
 * @author zab
 * @date 2022/5/8 11:14
 */
public interface OrderService {
    /**
     * 创建订单
     * @param order
     */
    void createOrder(Order order);
}


