package com.leyou.search.client;

import com.leyou.item.api.GoodsApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @author wyk
 * @date 2020/7/23 21:27
 */

@FeignClient("item-service")
public interface GoodsClient extends GoodsApi {
}
