package com.leyou.search.client;

import com.leyou.common.pojo.PageResult;
import com.leyou.item.bo.SpuBo;
import com.leyou.search.client.GoodsClient;
import com.leyou.search.pojo.Goods;
import com.leyou.search.repository.GoodsRepository;
import com.leyou.search.service.SearchService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author wyk
 * @date 2020/7/23 22:48
 */

@SpringBootTest
@RunWith(SpringRunner.class)
public class ElasticsearchTest {

    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;

    @Autowired
    private GoodsRepository goodsRepository;

    @Autowired
    private SearchService searchService;

    @Autowired
    private GoodsClient goodsClient;

    @Test
    public void testData(){
        //创建索引库
        this.elasticsearchTemplate.createIndex(Goods.class);
        //创建映射
        this.elasticsearchTemplate.putMapping(Goods.class);
        //分页
        Integer page=1;
        Integer rows=100;
        do {
            //导入数据 向ES中
            //分页查询Spu,获取分页结果集
            PageResult<SpuBo> result = this.goodsClient.querySpuBoByPage(null,null,page,rows);
            //获取当前页的数据
            List<SpuBo> items = result.getItems();
            //处理List<SpuBo>==>List<Goods>的转换
            List<Goods> goodsList = items.stream().map(spuBo -> {
                try {
                    return this.searchService.buildGoods(spuBo);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }).collect(Collectors.toList());
            //执行新增数据的方法
            this.goodsRepository.saveAll(goodsList);
            rows=items.size(); //重新赋值页的大小
            page++;
        }while (rows==100);
    }
}
