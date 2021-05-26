package com.br.marketing.es.test;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.es.bean.ApprovalHistory;
import com.br.marketing.es.bean.ApprovalHistoryEs;
import com.br.marketing.es.bean.DateAddBaseBean;
import com.br.marketing.es.bean.MarketingHistory;
import com.br.marketing.es.util.ApprovalHistoryEsBuilder;
import com.br.marketing.es.util.EsConstants;
import com.br.marketing.es.util.SwiftNumberManager;
import com.br.marketing.es.util.UuidUtils;
import com.br.marketing.es.util.es.EsUtil;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
//        demo.update();
//        demo.updateByQuery();
//        demo.updateBySnRefreshPolicyImmediate();
//        demo.select();
//        demo.count("7410480");
//         demo.delete("9fbba328f3e94e3ca149730fd9b6856b");
//        demo.selectByTemplate();
//        demo.selectByListTemplate();
//        demo.qryCountByCondition();
        System.exit(0);
    }

    public void insert() {
        MarketingHistory mh = new MarketingHistory();
        String id = UuidUtils.getUuid();
        System.out.println(id);
        String apiCode = "7410480";
        mh.setApiCode(apiCode);
        mh.setRequestTime(new Date());
        mh.setBatchNumber("7410480_20210517210400_4077");
        //流水号
        String swiftNumber = apiCode + "_" + SwiftNumberManager.getSwiftNumberManager().getSwiftNumberPre();
        mh.setSwiftNumber(swiftNumber);
        mh.setCusNum("60");
        mh.setStrategyId("DTB0000001");
        mh.setVersion("");
        JSONObject params = (JSONObject) JSON.toJSON(mh);
        params.put("_id", id);
        params.put("scorencashonszyxxy_flag", "1");
        params.put("scorencashonszyxxy", "827.391");
        params.put("scoremcashonxhqbdzcd_flag", "1");
        params.put("scoremcashonxhqbdzcd", "445");
        try {
            EsUtil.insert("request_marketing_history_202105_02", params);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void update() {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("_id", "9fbba328f3e94e3ca149730fd9b6856b");
            params.put("scoremcashonxhqbdzcd", "700");
            System.out.println(EsUtil.update("request_marketing_history_202105_02", params));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void updateByQuery() {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("scoremcashonxhqbdzcd", "449");
            SimpleDateFormat sdf = new SimpleDateFormat(EsConstants.DATE_FORMAT_YMD);
            params.put("request_time", sdf.format(new Date()));
            BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
            boolQueryBuilder.must(new TermQueryBuilder("swift_number", "7410480_20210521101646_62330063A66"));
            System.out.println(EsUtil.update(new String[]{"request_marketing_history_202105_02"}, boolQueryBuilder, params));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void updateBySnRefreshPolicyImmediate() throws IOException {
        Map<String, Object> params = new HashMap<>();
        params.put("scoremcashonxhqbdzcd", "500");
        SimpleDateFormat sdf = new SimpleDateFormat(EsConstants.DATE_FORMAT_YMD);
        params.put("request_time", sdf.format(new Date()));
        params.put("_id", "9fbba328f3e94e3ca149730fd9b6856b");
        //更新立即进行数据刷新
        EsUtil.updateRefreshPolicyImmediate("request_marketing_history_202105_02", params);
    }

    public void select() {
        try {
            BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
            boolQueryBuilder.must(new TermQueryBuilder("swift_number", "7410480_20210521101646_62330063A66"));
            System.out.println(EsUtil.select(new String[]{"request_marketing_history_202105_02"}, boolQueryBuilder));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 采用mustache语言作为elasticsearch搜索请求的预处理模板
     *
     * @param
     * @return
     */
    public void selectByTemplate() {
        try {
            Map<String, Object> params = new HashMap<>(1);
            //params.put("source", "[\"_id\",\"swift_number\",\"id\",\"cell\",\"name\"]");
            String fields = "id,cell,name,owner_user";
            List<String> fieldList = Arrays.asList(fields.split(","));
            params.put("source", JSON.toJSONString(fieldList));
            params.put("swift_number", "7410080_20210202154519_55672512B37");
            SearchHits hits = EsUtil.selectByTemplate(new String[]{"request_approval_history_202102"},
                    "str_sn_approval_template", params);
            List<ApprovalHistoryEs> result = new ArrayList<>();
            for (SearchHit hit : hits) {
                result.add(JSON.parseObject(hit.getSourceAsString(), ApprovalHistoryEs.class));
            }
            if (!result.isEmpty()) {
                System.out.println(JSON.toJSONString(result.get(0)));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 采用mustache语言作为elasticsearch搜索请求的预处理模板
     *
     * @param
     * @return
     */
    public void selectByListTemplate() {
        try {
            Map<String, Object> params = new HashMap<>(1);
            String sources = EsConstants.B_HISTORY_WITH_PAGE_KEY;
            List<String> sourceList = Arrays.asList(sources.split(","));
            params.put("source", JSON.toJSONString(sourceList));
            JSONObject sortObj = new JSONObject();
            sortObj.put("start_time", "desc");
            sortObj.put("swift_number", "desc");
            params.put("sort", sortObj.toJSONString());
            params.put("api_code", "7410080");
            //1
            /*String swiftNumbers = "7410086_20210105101435_74655C1BC10,7410080_20210105111651_74665C1BC10";
            List<String> swiftNumberList = Arrays.asList(swiftNumbers.split(","));
            params.put("swift_number", JSON.toJSONString(swiftNumberList));
            params.put("from", 0);
            params.put("size", swiftNumberList.size());*/
            //2
            String ownerUsers = "13413,13608";
            List<String> ownerUserList = Arrays.asList(ownerUsers.split(","));
            params.put("owner_user", JSON.toJSONString(ownerUserList));
            //params.put("from", 0);
            params.put("size", 10);
            //params.put("name", "13240332232");
            params.put("start_begin_time", "2021-01-17 00:00:00");
            params.put("start_end_time", "2021-01-20 13:59:43");
            params.put("api_code", "7410080");
            System.out.println(JSON.toJSON(params));
            SearchHits hits = EsUtil.selectByTemplate(new String[]{"request_approval_history_202101"},
                    "str_list_approval_template", params);
            List<ApprovalHistoryEs> result = new ArrayList<>();
            for (SearchHit hit : hits) {
                result.add(JSON.parseObject(hit.getSourceAsString(), ApprovalHistoryEs.class));
            }
            if (!result.isEmpty()) {
                System.out.println(JSON.toJSONString(result));
            }
            System.out.println(hits.getTotalHits().value);
            /*if(hits.getTotalHits().value > 100){
                long count = EsUtil.selectByTemplateCount(new String[]{"request_approval_history_202101"},
                        "str_list_approval_template", params);
                System.out.println(count);
            }*/
            System.out.println("结束");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void delete(String id) {
        try {
            BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
            boolQueryBuilder.must(new TermQueryBuilder("_id", id));
            System.out.println(EsUtil.delete(new String[]{"request_marketing_history_202105_02"}, boolQueryBuilder));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void count(String apiCode) {
        try {
            BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
            boolQueryBuilder.must(new TermQueryBuilder("api_code", apiCode));
            System.out.println(EsUtil.count(new String[]{"request_marketing_history_*"}, boolQueryBuilder));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void qryCountByCondition() {
        try {
            DateAddBaseBean bean = new DateAddBaseBean();
            ApprovalHistory approvalHistory = new ApprovalHistory();
            approvalHistory.setApicode("7410084");
            String startDateStr = "2021-02-01 00:00:00";
            String endDateStr = "2021-02-06 23:59:59";
            try {
                SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                bean.setStartDate(sf.parse(startDateStr));
                bean.setEndDate(sf.parse(endDateStr));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            if (approvalHistory.getApicode() != null) {
                ApprovalHistoryEsBuilder esBuilder = new ApprovalHistoryEsBuilder(bean, approvalHistory);
                //根据时间、流水号判断查询ES索引
                Set<String> indexSet = esBuilder.builderHistoryWithIndexSet();
                Map<String, Object> paramsCount = new HashMap<>();
                esBuilder.historyWhereCount(paramsCount);
                String[] indexArr = indexSet.toArray(new String[indexSet.size()]);
                //查询数量
                int total = (int) EsUtil.selectByTemplateCount(indexArr, EsConstants.PAGE_TEMPLATE, paramsCount);
                System.out.println(total);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
