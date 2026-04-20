/**
  * Copyright 2021 bejson.com 
  */
package com.br.marketing.vo;

import lombok.Data;

/**
 * Auto-generated: 2021-08-06 18:10:20
 *
 * @author bejson.com (i@bejson.com)
 * @website http://www.bejson.com/java2pojo/
 */
public class BaseHead {

    /**
     * 字段名称
     */
    private String name;

    /**
     * 字段类型 0-该字段值未空字符串；1-基础字段；2-扩展字段
     */
    private int type;

    /**
     * 加密类型 1-md5；2-sha256
     */
    private Integer threekEncryptType;


    public String getName() {
        return name;
    }

    public BaseHead setName(String name) {
        this.name = name;
        return this;
    }

    public int getType() {
        return type;
    }

    public BaseHead setType(int type) {
        this.type = type;
        return this;
    }

    public Integer getThreekEncryptType() {
        return threekEncryptType;
    }

    public BaseHead setThreekEncryptType(Integer threekEncryptType) {
        this.threekEncryptType = threekEncryptType;
        return this;
    }
}