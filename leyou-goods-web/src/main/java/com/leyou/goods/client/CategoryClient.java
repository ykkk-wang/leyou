package com.leyou.goods.client;

import com.leyou.item.api.CategoryApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @author wyk
 * @date 2020/7/23 21:59
 */

@FeignClient("item-service")
public interface CategoryClient extends CategoryApi {
}
