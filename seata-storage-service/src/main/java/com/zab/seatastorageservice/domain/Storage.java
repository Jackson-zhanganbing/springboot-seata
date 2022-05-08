package com.zab.seatastorageservice.domain;


import lombok.Data;
/**
 * storage
 *
 * @author zab
 * @date 2022/5/8 11:36
 */
@Data
public class Storage {

    private Long id;

    /**
     * 产品id
     */
    private Long productId;

    /**
     * 总库存
     */
    private Integer total;

    /**
     * 已用库存
     */
    private Integer used;

    /**
     * 剩余库存
     */
    private Integer residue;
}


