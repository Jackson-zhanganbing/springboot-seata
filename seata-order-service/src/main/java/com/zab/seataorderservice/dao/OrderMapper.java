package com.zab.seataorderservice.dao;


import com.zab.seataorderservice.domain.Order;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * dao层
 *
 * @author zab
 * @date 2022/5/8 11:13
 */
@Mapper
public interface OrderMapper {
    /**
     * 创建订单
     *
     * @param order
     */
    void createOrder(Order order);

    /**
     * 更新订单
     *
     * @param userId
     * @param status
     */
    void updateStatus(@Param("userId") Long userId, @Param("status") Integer status);
}


