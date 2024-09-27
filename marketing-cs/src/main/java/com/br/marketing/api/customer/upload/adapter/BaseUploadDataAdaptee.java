package com.br.marketing.api.customer.upload.adapter;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;


/**
 * 上传数据适配器
 * @param <T> the parameter of the class
 * @author senyang.zheng
 * @date 2024/08/07
 */
@Getter
@Setter
public abstract class BaseUploadDataAdaptee<T> implements Serializable {


    private static final long serialVersionUID = 2295254868340373170L;
    private String apiCode;
    private String jsonData;

    protected abstract T adapteeRequest(String apiCode, String jsonData);

}
