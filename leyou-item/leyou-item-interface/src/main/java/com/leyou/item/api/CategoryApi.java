package com.leyou.item.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * @author wyk
 * @date 2020/7/23 21:54
 */
@RequestMapping("category")
public interface CategoryApi {
    @GetMapping
    public List<String> queryNameByIds(@RequestParam("ids")List ids);
}
