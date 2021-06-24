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
    public static final String CODEVERSION_KEY = "%s_%s";
    /**
     * 模板-列表
     */
    public static final String PAGE_TEMPLATE = "str_list_marketing_template";
    /**
     * builderMarketingWithList
     */
    public static final String ALL_MARKETING_KEY = "api_code,id_card,cell,name,request_time,cus_batch_number,batch_number,swift_number," +
            "cus_num,strategy_id,version,product";


}
