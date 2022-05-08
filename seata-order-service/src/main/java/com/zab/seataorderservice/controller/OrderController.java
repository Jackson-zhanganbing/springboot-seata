package com.zab.seataorderservice.controller;


import com.zab.seataorderservice.common.CommonResult;
import com.zab.seataorderservice.domain.Order;
import com.zab.seataorderservice.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author zhubayi
 */
@RestController
@RequestMapping("order")
public class OrderController {
    @Autowired
    private OrderService orderService;
    @GetMapping("create")
    public CommonResult create(Order order){
        orderService.createOrder(order);
        return CommonResult.builder().message("创建成功").code(200).build();
    }
}


