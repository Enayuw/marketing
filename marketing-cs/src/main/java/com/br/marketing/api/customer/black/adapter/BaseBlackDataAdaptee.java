package com.br.marketing.api.customer.black.adapter;

import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;


/**
 * 黑名单数据适配器
 *
 * @author senyang.zheng
 * @date 2024/10/30
 */
@Getter
@Setter
public abstract class BaseBlackDataAdaptee<T> implements Serializable {


    private static final long serialVersionUID = 2295254868340373170L;
    private String apiCode;
    private String jsonData;

    protected abstract T adapteeRequest(String apiCode, String jsonData);

}
