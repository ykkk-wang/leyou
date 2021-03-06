package com.leyou.goods.client;

import com.leyou.item.api.SpecificationApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @author wyk
 * @date 2020/7/23 22:00
 */

@FeignClient("item-service")
public interface SpecificationClient extends SpecificationApi {
}
