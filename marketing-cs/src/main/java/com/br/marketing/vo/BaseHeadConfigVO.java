/**
  * Copyright 2021 bejson.com 
  */
package com.br.marketing.vo;
import lombok.Data;

import java.util.List;

/**
 * Auto-generated: 2021-08-06 18:10:20
 *
 * @author bejson.com (i@bejson.com)
 * @website http://www.bejson.com/java2pojo/
 */
@Data
public class BaseHeadConfigVO {


    public BaseHeadConfigVO() {
    }

    public BaseHeadConfigVO(List<String> showBaseHead, List<BaseHead> baseHead) {
        this.showBaseHead = showBaseHead;
        this.baseHead = baseHead;
    }

    /**
     * 展示字段列表
     */
    private List<String> showBaseHead;

    /**
     * 字段详情列表
     */
    private List<BaseHead> baseHead;

}