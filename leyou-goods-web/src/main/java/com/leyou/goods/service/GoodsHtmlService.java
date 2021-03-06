package com.leyou.goods.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

/**
 * @author wyk
 * @date 2020/7/27 15:24
 */

@Service
public class GoodsHtmlService {

    @Autowired
    private TemplateEngine engine;

    @Autowired
    private GoodsService goodsService;

    /**
     * 创建HTML静态页面模板
     * @param spuId
     */
    public void createHtml(Long spuId){

        //初始化运行上下文
        Context context = new Context();
        //设置数据模型
        context.setVariables(this.goodsService.loadData(spuId));
        PrintWriter printWriter=null;
        try {
            //静态文件生成到服务器本地
            File file = new File("D:\\tools\\nginx-1.14.0\\html\\item\\"+spuId+".html");
            printWriter = new PrintWriter(file);
            this.engine.process("item",context,printWriter);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }finally {
            if(printWriter!=null){
                printWriter.close();
            }
        }
    }
    //删除静态化页面
    public void deleteHtml(Long id) {
        File file = new File("D:\\tools\\nginx-1.14.0\\html\\item\\" + id + ".html");
        file.deleteOnExit();
    }
}
