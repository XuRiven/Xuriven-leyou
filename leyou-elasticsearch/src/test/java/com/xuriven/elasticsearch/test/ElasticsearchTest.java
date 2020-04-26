package com.xuriven.elasticsearch.test;

import com.leyou.common.pojo.PageResult;
import com.leyou.elasticsearch.ElasticSearchApplication;
import com.leyou.elasticsearch.client.GoodsClient;
import com.leyou.elasticsearch.pojo.Goods;
import com.leyou.elasticsearch.repository.GoodsRepository;
import com.leyou.elasticsearch.search.SearchService;
import com.leyou.item.bo.SpuBO;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@SpringBootTest(classes = ElasticSearchApplication.class)
@RunWith(SpringRunner.class)
public class ElasticsearchTest {
    @Autowired
    private ElasticsearchTemplate template;
    @Autowired
    private GoodsRepository goodsRepository;
    @Autowired
    private SearchService searchService;
    @Autowired
    private GoodsClient goodsClient;
    @Test
    public void testSave(){
        this.template.createIndex(Goods.class);
        this.template.putMapping(Goods.class);

        Integer page=1;
        Integer rows=100;

        do{
            //分页查询spu，获取分页结果集
            PageResult<SpuBO> result = this.goodsClient.querySpuByPage(null, null, page, rows);
            //获取当前页的数据
            List<SpuBO> items = result.getItems();
            //处理List<SpuBo>=>List<Goods>
            List<Goods> goodsList=items.stream().map(spuBO -> {
                try {
                    return this.searchService.buildGoods(spuBO);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }).collect(Collectors.toList());
            this.goodsRepository.saveAll(goodsList);
            rows=items.size();
            page++;

        }while (rows==100
        );

    }
}
