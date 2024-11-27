package com.br.marketing.client.shunfeng.input;

import lombok.Data;

/**
 * @author zhen.li1
 */
@Data
public class BusinessListReq {

    /**
     * 分页编码，默认为 1
     */
    private int  page;

    /**
     * 每页大小最大值 50
     */
    private int  pageSize;

    /**
     * 公司所在省份
     */
    private String  province;

    /**
     * 公司所在城市
     */
    private String  city;

    /**
     * 公司所属一类行业
     */
    private String  first_category;

    /**
     * 公司所属二类行业
     */
    private String  second_category;

    /**
     * 地址筛选标识，包括空、“存在”、“不
     * 存在”三种情况
     * “存在”：只获取有地址信息的数据
     * “不存在”：只获取没有地址信息的数据
     * 不传或者传空，代表不启用该筛选条件
     */
    private String  address_flag;

    /**
     * 电话筛选标识，包括空、“存在”、“不
     * 存在”三种情况
     * “存在”：只获取有联系电话的数据
     * “不存在”：只获取没有联系电话的数据
     * 不传或者传空，代表不启用该筛选条件
     */
    private String  contact_flag;






}
