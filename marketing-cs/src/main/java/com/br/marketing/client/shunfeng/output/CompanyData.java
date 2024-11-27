package com.br.marketing.client.shunfeng.output;

import lombok.Data;

@Data
public class CompanyData {

    /**
     * 公司所在省份
     */
    private String province;

    /**
     * 公司所在城市
     */
    private String city;

    /**
     * 公司所在区县
     */
    private String district;

    /**
     * 公司所属一类行业
     */
    private String first_category;

    /**
     * 公司所属二类行业
     */
    private String second_category;

    /**
     * 公司所属三类行业
     */
    private String third_category;

    /**
     * 公司的社会统一信用代码
     */
    private String creditcode;

    /**
     * 公司名称
     */
    private String companyname;


}
