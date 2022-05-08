package com.zab.seataorderservice.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
/**
 * 通用返回类
 *
 * @author zab
 * @date 2022/5/8 11:12
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CommonResult<T> {
    private Integer code;
    private String message;
    private T data;

}
