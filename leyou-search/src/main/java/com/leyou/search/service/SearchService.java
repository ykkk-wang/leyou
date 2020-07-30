package com.leyou.search.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.leyou.item.pojo.*;
import com.leyou.search.client.BrandClient;
import com.leyou.search.client.CategoryClient;
import com.leyou.search.client.GoodsClient;
import com.leyou.search.client.SpecificationClient;
import com.leyou.search.pojo.Goods;
import com.leyou.search.pojo.SearchRequest;
import com.leyou.search.pojo.SearchResult;
import com.leyou.search.repository.GoodsRepository;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.LongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.query.FetchSourceFilter;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author wyk
 * @date 2020/7/23 22:02
 */
@Service
public class SearchService {

    @Autowired
    private CategoryClient categoryClient;

    @Autowired
    private BrandClient brandClient;

    @Autowired
    private GoodsClient goodsClient;

    @Autowired
    private SpecificationClient specificationClient;

    @Autowired
    private GoodsRepository goodsRepository;

    //json工具
    private static final ObjectMapper MAPPER = new ObjectMapper();

    //构建Goods商品类
    public Goods buildGoods(Spu spu) throws IOException {
        Goods goods = new Goods();
        //根据分类的id查询分类名称
        List<String> names = this.categoryClient.queryNameByIds(Arrays.asList(spu.getCid1(), spu.getCid2(), spu.getCid3()));

        //根据品牌Id查询品牌
        Brand brand = this.brandClient.queryBrandById(spu.getBrandId());

        //根据spuId查询所有的sku
        List<Sku> skus = this.goodsClient.querySkusBySpuId(spu.getId());
        //初始化一个价格集合，来搜集所有sku的价格
        List<Long> prices = new ArrayList<>();
        //搜集sku的必要字段信 息
        List<Map<String, Object>> skuMapList = new ArrayList<>();
        skus.forEach(sku -> {
            prices.add(sku.getPrice());
            Map<String, Object> map = new HashMap<>();
            map.put("id", sku.getId());
            map.put("title", sku.getTitle());
            map.put("price", sku.getPrice());
            //获取sku中的图片，数据库中的图片可能是多张，多张是以逗号分隔。所以我们也以“，”切割，返回图片数组，获取第一张图片。
            map.put("image", StringUtils.isBlank(sku.getImages()) ? " " : StringUtils.split(sku.getImages(), ",")[0]);
            skuMapList.add(map);
        });
        //根据spu中的cid3查询所有的搜索规格参数
        List<SpecParam> params = this.specificationClient.queryParams(null, spu.getCid3(), null, true);

        //根据spuId查询spuDetail
        SpuDetail spuDetail = this.goodsClient.querySpuDetailBySpuId(spu.getId());
        //把通用的规格参数进行反序列化
        Map<String, Object> genericSpecMap = MAPPER.readValue(spuDetail.getGenericSpec(), new TypeReference<Map<String, Object>>() {
        });
        //把特殊规格参数值进行反序列化
        Map<String, List<Object>> specialSpecMap = MAPPER.readValue(spuDetail.getSpecialSpec(), new TypeReference<Map<String, List<Object>>>() {
        });

        Map<String, Object> specs = new HashMap<>();
        params.forEach(param -> {
            //判断规格参数的类型，是否是通用的规格参数
            if (param.getGeneric()) {
                //如果是通用类型的参数，从genericSpecMap获取规格参数值
                String value = genericSpecMap.get(param.getId().toString()).toString();
                //判断是否是数值类型，如果是就应该返回一个区间
                if (param.getNumeric()) {
                    value = chooseSegment(value, param);
                }
                specs.put(param.getName(), value);
            } else {
                //如果是特殊的数据类型就从genericSpecMap获取值
                List<Object> value = specialSpecMap.get(param.getId().toString());
                specs.put(param.getName(), value);
            }
        });

        goods.setId(spu.getId());
        goods.setCid1(spu.getCid1());
        goods.setCid2(spu.getCid2());
        goods.setCid3(spu.getCid3());
        goods.setBrandId(spu.getBrandId());
        goods.setCreateTime(spu.getCreateTime());
        goods.setSubTitle(spu.getSubTitle());
        //拼接ALL字段，需要分类名称以及品牌名称
        goods.setAll(spu.getTitle() + " " + StringUtils.join(names, " ") + " " + brand.getName());
        //获取Spu下的所有sku价格
        goods.setPrice(prices);
        //获取spu下的所有sku并转换成json字符串，并保存
        goods.setSkus(MAPPER.writeValueAsString(skuMapList));
        //获取查询的规格参数{规格参数名：所有参数}
        goods.setSpecs(specs);
        return goods;
    }

    private String chooseSegment(String value, SpecParam p) {
        double val = NumberUtils.toDouble(value);
        String result = "其它";
        // 保存数值段
        for (String segment : p.getSegments().split(",")) {
            String[] segs = segment.split("-");
            // 获取数值范围
            double begin = NumberUtils.toDouble(segs[0]);
            double end = Double.MAX_VALUE;
            if (segs.length == 2) {
                end = NumberUtils.toDouble(segs[1]);
            }
            // 判断是否在范围内
            if (val >= begin && val < end) {
                if (segs.length == 1) {
                    result = segs[0] + p.getUnit() + "以上";
                } else if (begin == 0) {
                    result = segs[1] + p.getUnit() + "以下";
                } else {
                    result = segment + p.getUnit();
                }
                break;
            }
        }
        return result;
    }

    /**
     * 搜索条件实现------ElasticSearch
     */
    public SearchResult search(SearchRequest request) {
        //判断查询条件是否为空
        if (StringUtils.isBlank(request.getKey())) {
            return null;
        }
        //自定义查询构建器
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        //添加查询条件
//        QueryBuilder basicQuery= QueryBuilders.matchQuery("all", request.getKey()).operator(Operator.AND);
        //构建Boolean查询
        BoolQueryBuilder basicQuery=buildBoolQueryBuilder(request);
        queryBuilder.withQuery(basicQuery);
        //添加分页  分页页码是从0开始的
        queryBuilder.withPageable(PageRequest.of(request.getPage() - 1, request.getSize()));
        //添加结果集过滤
        queryBuilder.withSourceFilter(new FetchSourceFilter(new String[]{"id", "skus", "subTitle"}, null));

        //添加分类和品牌的聚合
        String categoriesAggName="categories";//聚合字段
        String brandAggName="brands";
        queryBuilder.addAggregation(AggregationBuilders.terms(categoriesAggName).field("cid3"));
        queryBuilder.addAggregation(AggregationBuilders.terms(brandAggName).field("brandId"));

        //执行查询获取结果集
        AggregatedPage<Goods> goodsPage = (AggregatedPage<Goods>)this.goodsRepository.search(queryBuilder.build());

        //获取聚合结果集并解析
        List<Map<String,Object>> categories= getCategoryAggResult(goodsPage.getAggregation(categoriesAggName));
        List<Brand> brands=getBarndAggResult(goodsPage.getAggregation(brandAggName));
        List<Map<String,Object>> specs=null;
        //判断是否是一个分类，只有一个参数时，才做规格参数聚合
        if(!CollectionUtils.isEmpty(categories)&&categories.size()==1){
            //对规格参数做聚合
            specs= getParamAggResult((Long)categories.get(0).get("id"),basicQuery);
        }
        return new SearchResult(goodsPage.getTotalElements(),goodsPage.getTotalPages(),goodsPage.getContent(),categories,brands,specs);
    }

    /**
     * 解析分类的聚合结果集
     * @param aggregation
     * @return
     */
    private List<Map<String, Object>> getCategoryAggResult(Aggregation aggregation) {
        LongTerms terms=(LongTerms)aggregation; //桶

        //获取桶的集合转化成List<Map<String,Object>>
        return terms.getBuckets().stream().map(bucket -> {
            //初始化一个map
            Map<String,Object> map=new HashMap<>();
            //获取桶中分类Id(key)
            Long id = bucket.getKeyAsNumber().longValue();
            //根据分类Id查询分类名称
            List<String> names = this.categoryClient.queryNameByIds(Arrays.asList(id));
            map.put("id",id);
            map.put("name",names.get(0));
            return map;
        }).collect(Collectors.toList());
    }

    /**
     * 解析品牌的聚合结果集   brands
     * @param aggregation
     * @return
     */
    private List<Brand> getBarndAggResult(Aggregation aggregation) {
        LongTerms terms=(LongTerms)aggregation; //桶

      /*  //获取聚合中的桶
       List<Brand> brands=new ArrayList<>();
        terms.getBuckets().forEach(bucket -> {
            //根据品牌id查询品牌
            Brand brand = this.brandClient.queryBrandById(bucket.getKeyAsNumber().longValue());
            brands.add(brand);
        });
          return brands;
        */
        return terms.getBuckets().stream().map(bucket -> {
            return this.brandClient.queryBrandById(bucket.getKeyAsNumber().longValue());
        }).collect(Collectors.toList());
    }

    /**
     * 根据查询条件聚合规格参数
     * @param cid
     * @param basicQuery
     * @return
     */
    private List<Map<String, Object>> getParamAggResult(Long cid, QueryBuilder basicQuery) {
        //自定义查询对象构建器
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        //添加基本查询条件
        queryBuilder.withQuery(basicQuery);
        //查询要聚合的规格参数
        List<SpecParam> params = this.specificationClient.queryParams(null, cid, null, true);
        //添加规格参数聚合
        params.forEach(param->{
            queryBuilder.addAggregation(AggregationBuilders.terms(param.getName()).field("specs."+param.getName()+".keyword"));
        });
        //添加普通结果集过滤  不包含任何字段
        queryBuilder.withSourceFilter(new FetchSourceFilter(new String[]{},null));
        //执行聚合查询 ,获取聚合结果集
        AggregatedPage<Goods> goodsPage = (AggregatedPage<Goods>)this.goodsRepository.search(queryBuilder.build());

        List<Map<String, Object>> specs=new ArrayList<>();
        //解析聚合结果集  key==聚合名称（规格参数名）  value===聚合对象
        Map<String, Aggregation> aggregationMap = goodsPage.getAggregations().asMap();
        for (Map.Entry<String,Aggregation> entry:aggregationMap.entrySet()){
            //初始化一个Map key=规格参数名   value=聚合规格参数值
            Map<String,Object> map=new HashMap<>();
            map.put("k",entry.getKey());

            //初始化一个options集合，收集桶中的key
            List<String> options=new ArrayList<>();
            //获取聚合
            StringTerms terms=(StringTerms) entry.getValue();
            //获取桶集合
            terms.getBuckets().forEach(bucket -> {
                options.add(bucket.getKeyAsString());
            });
            map.put("options",options);
            specs.add(map);
        }
        return specs;
    }

    /**
     * 构建布尔查询
     * @param request
     * @return
     */
    private BoolQueryBuilder buildBoolQueryBuilder(SearchRequest request) {
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        //给布尔查询添加基本的查询条件
        boolQueryBuilder.must(QueryBuilders.matchQuery("all",request.getKey()).operator(Operator.AND));
        //添加过滤条件
        //获取用户选择的过滤信息
        Map<String,Object> filter=request.getFilter();
        for (Map.Entry<String, Object> entry : filter.entrySet()) {
            String key=entry.getKey();
            if (StringUtils.equals("品牌",key)){
                key="brandId";
            }else if (StringUtils.equals("分类",key)){
                key="cid3";
            }else {
                key="specs."+key+".keyword";
            }
            boolQueryBuilder.filter(QueryBuilders.termQuery(key,entry.getValue()));
        }
        return boolQueryBuilder;
    }

    /**
     * 监听rabbitMQ中的消息变化，保存操作的监听者
     * @param id
     */
    public void save(Long id) throws IOException {
        Spu spu = this.goodsClient.querySpuById(id);
        Goods goods = this.buildGoods(spu);
        this.goodsRepository.save(goods);
    }

    /**
     * 监听rabbitMQ中的消息变化，删除操作的监听
     * @param id
     */
    public void delete(Long id) {

        this.goodsRepository.deleteById(id);
    }
}