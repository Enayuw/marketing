package com.br.marketing.es.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.es.bean.QueryBaseBean;
import com.br.marketing.es.util.es.EsHandleUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * ES条件组装
 *
 * @Author linquan.guo
 * @CreateDate 2021/1/9 17:29
 * @UpdateUser linquan.guo
 * @UpdateDate 2021/1/9 17:29
 * @UpdateRemark 修改内容
 * @Version 1.0
 */
@Slf4j
public class MarketingEsBuilder {
    private QueryBaseBean queryBaseBean;

    public MarketingEsBuilder(QueryBaseBean queryBaseBean) {
        this.queryBaseBean = queryBaseBean;
    }

    /**
     * 日期
     */
    public static final String YYYYMMDD = "yyyyMMdd";

    /**
     * 选取索引
     *
     * @param
     * @return
     */
    public Set<String> builderHistoryWithIndexSet() {
        Set<String> indexSet = new HashSet<>();
        //多个流水号
        Date startDate = queryBaseBean.getTaskStartTime();
        Date endDate = queryBaseBean.getTaskEndTime();
        if (startDate != null && endDate != null) {
            SimpleDateFormat dateFormat = new SimpleDateFormat(YYYYMMDD);
            String bDate = dateFormat.format(startDate);
            String eDate = dateFormat.format(endDate);
            if (bDate.equals(eDate)) {
                indexSet.add(String.format(EsConstants.HISTORY_KEY, EsHandleUtil.getIndexFromStr(bDate)));
            } else {
                indexSet.add(String.format(EsConstants.HISTORY_KEY, EsHandleUtil.getIndexFromStr(bDate)));
                String endFormat = String.format(EsConstants.HISTORY_KEY, EsHandleUtil.getIndexFromStr(eDate));
                while (true) {
                    Calendar next = Calendar.getInstance();
                    next.setTime(startDate);
                    next.add(Calendar.DATE, 1);
                    startDate = next.getTime();
                    String indexNext = String.format(EsConstants.HISTORY_KEY, EsHandleUtil.getIndexFromStr(dateFormat.format(startDate)));
                    if (endFormat.equals(indexNext)) {
                        indexSet.add(endFormat);
                        break;
                    } else {
                        indexSet.add(indexNext);
                    }
                }
            }
        } else {
            throw new RuntimeException("date is exception");
        }
        return indexSet;
    }

    /**
     * 查询营销数量构建
     *
     * @param paramsCount
     * @return
     */
    public void MarketingWhereCount(Map<String, Object> paramsCount) {
        //返回结果
        paramsCount.put("source", JSON.toJSONString(Arrays.asList("_id".split(","))));
        //排序
        JSONObject sortObj = new JSONObject();
        sortObj.put("swift_number", "desc");
        paramsCount.put("sort", sortObj.toJSONString());
        MarketingWhere(paramsCount);
    }

    /**
     * 查询营销条件构建
     *
     * @param params
     * @return
     */
    public void MarketingWhere(Map<String, Object> params) {
        //apicode
        String apiCode = queryBaseBean.getApiCode();
        if (StringUtils.isNotBlank(apiCode)) {
            params.put("api_code", apiCode);
        }
        //查询时间
        filterTime(params);
        //查询批次
        String batchNumber = queryBaseBean.getBatchNumber();
        if (StringUtils.isNotBlank(batchNumber)) {
            List<String> batchNumberList = Arrays.asList(batchNumber.split(","));
            params.put("batch_number", JSON.toJSONString(batchNumberList));
        }
        //分值区间

    }

    /**
     * 查询日期
     *
     * @param params
     * @return
     */
    private void filterTime(Map<String, Object> params) {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat(EsConstants.DATE_FORMAT_YMD);
            if (queryBaseBean.getTaskStartTime() != null) {
                params.put("task_begin_time", dateFormat.format(queryBaseBean.getTaskStartTime()));
            }
            if (queryBaseBean.getTaskEndTime() != null) {
                params.put("task_end_time", dateFormat.format(queryBaseBean.getTaskEndTime()));
            }
        } catch (Exception e) {
            log.error("MarketingEsBuilder 日期转换错误", e);
        }
    }
}
