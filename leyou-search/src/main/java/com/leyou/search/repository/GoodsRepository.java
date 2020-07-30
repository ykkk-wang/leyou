package com.leyou.search.repository;

import com.leyou.search.pojo.Goods;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * @author wyk
 * @date 2020/7/23 22:53
 */
public interface GoodsRepository extends ElasticsearchRepository<Goods,Long> {
}
