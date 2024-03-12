package com.br.marketing.service;


import com.br.marketing.common.commondto.Result;

import java.util.LinkedHashMap;
import java.util.TreeMap;

/**
 * 宜信转化数据推决策主流程
 * @author GuangChao.Zhang
 * @version 1.0
 * @date 2023/6/16 17:19
 */
public interface YiXinToJueCeProcessService {
    /**
     * 宜信推决策do方法
     */
    void doProcess(LinkedHashMap<String, String> actionTypeLink);

}
