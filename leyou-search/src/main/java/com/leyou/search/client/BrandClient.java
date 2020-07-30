package com.leyou.search.client;

import com.leyou.item.api.BrandApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @author wyk
 * @date 2020/7/23 21:59
 */

@FeignClient("item-service")
public interface BrandClient extends BrandApi {
}
