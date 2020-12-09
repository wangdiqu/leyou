package com.leyou.common.dto;

import com.sun.istack.internal.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CartDTO {
    @NotNull
    private Long skuId;//商品skuid
    @NotNull
    private Integer num;//购买数量
}
