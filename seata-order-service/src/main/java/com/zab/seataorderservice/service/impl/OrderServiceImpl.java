package com.zab.seataorderservice.service.impl;


import com.zab.seataorderservice.dao.OrderMapper;
import com.zab.seataorderservice.domain.Order;
import com.zab.seataorderservice.service.AccountService;
import com.zab.seataorderservice.service.OrderService;
import com.zab.seataorderservice.service.StorageService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
/**
 * 订单实现
 * 
 * @author zab
 * @date 2022/5/8 11:19
 */
@Service
@Slf4j
public class OrderServiceImpl implements OrderService {
    @Autowired
    private OrderMapper orderMapper;
    @Resource
    private StorageService storageService;
    @Resource
    private AccountService accountService;
    /**
     * 创建订单
     *
     * @param order
     */
    @Override
    @GlobalTransactional(name = "creat-order-transaction", rollbackFor = Exception.class)
    public void createOrder(Order order) {
        log.info("=====>{}","开始新建订单");
        orderMapper.createOrder(order);
        log.info("=====>{}","新建订单成功");
        log.info("=====>{}","库存减少");
        storageService.decrease(order.getProductId(),order.getCount());
        log.info("=====>{}","库存减少成功");
        log.info("=====>{}","开始减少余额");
        accountService.decrease(order.getUserId(),order.getMoney());
        log.info("=====>{}","减少余额成功");

        log.info("=====>{}","开始修改订单状态");
        orderMapper.updateStatus(order.getUserId(),0);
        log.info("=====>{}","修改订单状态成功");

        log.info("=====>{}","end");
    }
}


