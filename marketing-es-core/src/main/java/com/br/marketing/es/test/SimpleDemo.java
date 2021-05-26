package com.br.marketing.es.test;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.es.bean.MarketingHistory;
import com.br.marketing.es.bean.Product;
import com.br.marketing.es.service.MarketingHistoryEsService;
import com.br.marketing.es.service.impl.MarketingHistoryEsServiceImpl;
import com.br.marketing.es.util.BrCipherMaker;
import com.br.marketing.es.util.EsConstants;
import com.br.marketing.es.util.SwiftNumberManager;
import com.br.marketing.es.util.UuidUtils;
import com.br.marketing.es.util.es.EsHandleUtil;
import com.br.marketing.es.util.es.EsUtil;

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
        demo.insert();
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
        list.add(new Product("scorencashonszyxxy", "S1_0", "1", "827.391"));
        list.add(new Product("scoremcashonxhqbdzcd", "S1_0", "1", "445"));
        mh.setProduct(list);
        MarketingHistoryEsServiceImpl service = new MarketingHistoryEsServiceImpl();
        service.insert(mh, id);
    }

    /*public void insert() {
        MarketingHistory mh = new MarketingHistory();
        String id = UuidUtils.getUuid();
        System.out.println(id);
        String apiCode = "7410480";
        mh.setApiCode(apiCode);
        mh.setRequestTime(new Date());
        mh.setBatchNumber("7410480_20210526210400_4077");
        //流水号
        String swiftNumber = apiCode + "_" + SwiftNumberManager.getSwiftNumberManager().getSwiftNumberPre();
        mh.setSwiftNumber(swiftNumber);
        mh.setCusNum("60");
        mh.setStrategyId("DTB0000001");
        mh.setVersion("");
        mh.setIdCard();
        mh.setCell();
        mh.setName();
        List<Product> list = new ArrayList<>();
        list.add(new Product("scorencashonszyxxy","S1_0","1","827.391"));
        list.add(new Product("scoremcashonxhqbdzcd","S1_0","1","445"));
        mh.setProduct(list);
        JSONObject params = (JSONObject) JSON.toJSON(mh);
        params.put("_id", id);
        try {
            String date = EsHandleUtil.getDateFromSwiftNumber("7410480_20210526210400_4077");
            String index = String.format(EsConstants.HISTORY_KEY, date);
            EsUtil.insert(index, params);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }*/


}
