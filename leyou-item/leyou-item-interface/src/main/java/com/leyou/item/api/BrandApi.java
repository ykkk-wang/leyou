package com.leyou.item.api;

import com.leyou.item.pojo.Brand;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author wyk
 * @date 2020/7/23 21:54
 */

@RequestMapping("brand")
public interface BrandApi {

    @GetMapping("{id}")
    public Brand queryBrandById(@PathVariable("id")Long id);
}
