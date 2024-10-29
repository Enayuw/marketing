package com.br.marketing.client.shunfeng.output;

import lombok.Data;

import java.util.List;

/**
 * @author zhen.li1
 */
@Data
public class BusinessListResponse {

    /**
     * 当前页码数
     */
    private int  page;

    /**
     * 每页大小
     */
    private int  pageSize;


    /**
     * 总条数
     */
    private int  total;



    private List<CompanyData> list;



}
