package com.br.marketing.es.test;

import com.alibaba.fastjson.JSON;
import com.br.marketing.es.bean.MarketingHistory;
import com.br.marketing.es.bean.Product;
import com.br.marketing.es.bean.QueryBaseBean;
import com.br.marketing.es.service.impl.MarketingHistoryEsServiceImpl;
import com.br.marketing.es.util.BrCipherMaker;
import com.br.marketing.es.util.UuidUtils;
import com.br.marketing.es.util.es.EsUtil;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.TermQueryBuilder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * demo
 *
 * @Author linquan.guo
 * @CreateDate 2020/12/29 16:08
 * @UpdateUser linquan.guo
 * @UpdateDate 2020/12/29 16:08
 * @UpdateRemark 修改内容
 * @Version 1.0
 */
public class SimpleDemo {
    public static void main(String[] args) throws IOException {
        SimpleDemo demo = new SimpleDemo();
//        demo.insert();
//        demo.delete("deda9fed6a8142be887bc33b77705c9a","request_marketing_history_202105_03");
//        demo.selectCount();
//        demo.builderMarketingWithSwiftNumber();
//        demo.builderMarketingWithList();
        demo.builderMarketingWithList("cell,product");
        System.exit(0);
    }

    public void insert() {
        MarketingHistory mh = new MarketingHistory();
        String id = UuidUtils.getUuid();
        System.out.println(id);
        String apiCode = "7410480";
        mh.setApiCode(apiCode);
        mh.setRequestTime(new Date());
        String batchNumber = "7410480_20210526210400_4077";
        mh.setBatchNumber(batchNumber);
        mh.setCusNum("60");
        mh.setStrategyId("DTB0000001");
        mh.setVersion("");
        mh.setIdCard(BrCipherMaker.getInstance().encode("321083197210254534"));
        mh.setCell(BrCipherMaker.getInstance().encode("13961135692"));
        mh.setName(BrCipherMaker.getInstance().encode("沙玉仁"));
        List<Product> list = new ArrayList<>();
        list.add(new Product("scorencashonszyxxy", "S1_0", "scorencashonszyxxy_S1_0", "1", 827.391));
        list.add(new Product("scoremcashonxhqbdzcd", "S1_0", "scoremcashonxhqbdzcd_S1_0", "1", 656D));
        mh.setProduct(list);
        MarketingHistoryEsServiceImpl service = new MarketingHistoryEsServiceImpl();
        service.insert(mh, id);
    }


    public void delete(String uuid, String index) {
        try {
            BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
            boolQueryBuilder.must(new TermQueryBuilder("_id", uuid));
            System.out.println(EsUtil.delete(new String[]{index}, boolQueryBuilder));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void selectCount() {
        String apiCode = "7410480";
        String batchNumber = "7410480_20210526210400_4077";
        QueryBaseBean qb = new QueryBaseBean();
        qb.setApiCode(apiCode);
        qb.setBatchNumbers(batchNumber);
        qb.setModelCode("scoremcashonxhqbdzcd");
        qb.setModelVersion("S1_0");
        qb.setScoreRange("100,556.1");
        qb.setAmountTop("0,10000");
        MarketingHistoryEsServiceImpl service = new MarketingHistoryEsServiceImpl();
        int count = service.builderMarketingWithTotal(qb);
        System.out.println(count);
    }

    public void builderMarketingWithSwiftNumber() {
        String apiCode = "7410480";
        String batchNumber = "7410480_20210526210400_4077";
        QueryBaseBean qb = new QueryBaseBean();
        qb.setApiCode(apiCode);
        qb.setBatchNumbers(batchNumber);
        qb.setModelCode("scoremcashonxhqbdzcd");
        qb.setModelVersion("S1_0");
        qb.setScoreRange("100,900");
        qb.setHisPageSwiftNumber("7410480_20210526200801_13010063A39");
        qb.setPageSize(1);
        MarketingHistoryEsServiceImpl service = new MarketingHistoryEsServiceImpl();
        String swiftNumber = service.builderMarketingWithSwiftNumber(qb);
        System.out.println(swiftNumber);
    }

    public void builderMarketingWithList() {
        String apiCode = "7410480";
        String batchNumber = "7410480_20210526210400_4077";
        QueryBaseBean qb = new QueryBaseBean();
        qb.setApiCode(apiCode);
        qb.setBatchNumbers(batchNumber);
        qb.setModelCode("scoremcashonxhqbdzcd");
        qb.setModelVersion("S1_0");
        qb.setScoreRange("100,900");
        qb.setHisPageSwiftNumber("7410480_20210526200801_13010063A39");
        qb.setPageSize(1);
        MarketingHistoryEsServiceImpl service = new MarketingHistoryEsServiceImpl();
        List<MarketingHistory> list = service.builderMarketingWithList(qb);
        System.out.println(JSON.toJSONString(list));
    }

    public void builderMarketingWithList(String columns) {
        String apiCode = "7410480";
        String batchNumber = "7410480_20210526210400_4077";
        QueryBaseBean qb = new QueryBaseBean();
        qb.setApiCode(apiCode);
        qb.setBatchNumbers(batchNumber);
        qb.setModelCode("scoremcashonxhqbdzcd");
        qb.setModelVersion("S1_0");
        qb.setScoreRange("100,900");
        qb.setHisPageSwiftNumber("7410480_20210526200801_13010063A39");
        qb.setPageSize(1);
        MarketingHistoryEsServiceImpl service = new MarketingHistoryEsServiceImpl();
        List<MarketingHistory> list = service.builderMarketingWithList(qb, columns);
        System.out.println(JSON.toJSONString(list));
    }

}
