package com.br.marketing.es.util;


/**
 * 常量
 *
 * @Author linquan.guo
 * @CreateDate 2020/12/29 18:13
 * @UpdateUser linquan.guo
 * @UpdateDate 2020/12/29 18:13
 * @UpdateRemark 修改内容
 * @Version 1.0
 */
public class EsConstants {
    /**
     * es索引
     */
    public static final String HISTORY_KEY = "request_marketing_history_%s";
    /**
     * 模板-流水号、状态查询单条
     */
    public static final String SN_TEMPLATE = "str_sn_approval_template";
    /**
     * 模板-列表
     */
    public static final String PAGE_TEMPLATE = "str_list_approval_template";

    public static final String SWIFT_NUMBER_KEY = "swift_number";
    /**
     * 加解密key
     */
    public static final String TAG_KEY = "id,name,cell,query_id,query_name,query_cell,mail";

    /**
     * builderHistoryWithPage
     */
    public static final String B_HISTORY_WITH_PAGE_KEY = "swift_number,id,cell,name,query_type,cus_num,start_time,batch_number,rule_s," +
            "scoreaf_s,score_s,infocheck_decision,infocheck_hit_count,infocheck_final_weight,strategy_decision,final_decision," +
            "decision_status,status,strategy_response_code,infocheck_response_code,owner_user,strategy_id,conf_id,remark";

    /**
     * builderFinalDecision
     */
    public static final String B_FINAL_DECISION_KEY = "id,name,cell,final_decision,query_type,start_time,api_code,approval_swift_number," +
            "strategy_response_code";

    /**
     * 时间格式
     */
    public static final String DATE_FORMAT_ALL = "yyyy-MM-dd HH:mm:ss";
    public static final String DATE_FORMAT_YMD = "yyyy-MM-dd";
}
