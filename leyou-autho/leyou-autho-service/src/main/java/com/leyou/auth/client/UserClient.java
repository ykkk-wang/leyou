package com.leyou.auth.client;

import com.leyou.user.api.UserApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @author wyk
 * @date 2020/7/28 22:59
 */

@FeignClient("user-service")
public interface UserClient extends UserApi {
}
