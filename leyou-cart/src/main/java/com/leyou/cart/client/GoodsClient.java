package com.leyou.cart.client;

import com.leyou.item.api.GoodsApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @author wyk
 * @date 2020/7/29 23:00
 */
@FeignClient("item-service")
public interface GoodsClient extends GoodsApi {
}
