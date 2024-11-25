package com.br.marketing.client.shunfeng.input;

import lombok.Data;

/**
 * @author zhen.li1
 */
@Data
public class BussinessInfoReq {

    /**
     * 企业名称
     */
    private String company_name;

    /**
     * 企业信用代码
     * 企业信用代码与企业名二选一切必须有一个
     */
    private String credit_code;

    /**
     * 地址筛选标识
     * 包括空、“存在”、“不存在”
     * 三种情况
     * “存在”：只获取有地址信息的数据
     * “不存在”：只获取没有地址信息的数据
     * 不传或者传空，代表不启用该筛选条件
     */
    private String address_flag;

    /**
     * 电话筛选标识
     * 包括空、“存在”、“不存在”
     * 三种情况
     * “存在”：只获取有联系电话的数据
     * “不存在”：只获取没有联系电话的数据
     * 不传或者传空，代表不启用该筛选条件
     */
    private String contact_flag;


}
