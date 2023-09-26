package com.br.marketing.service;

import com.alibaba.fastjson.JSONObject;
import com.br.marketing.entity.XieChengJudgeConvTypeValue;

import java.util.List;

/**
 * 描述：： 根据有效期框定数据范围
 * <p>
 * ------------------------------------
 * @program: marketing
 * @ClassName ValidityPeriodDataService
 * @author: chenh
 * @create: 2023-09-22 21:22
 * @Version 1.0
 * --------------------------------------
 **/
public interface XieChengJudgeConvTypeService {

    /**
     * 根据apiCode custNum 查询有效期内是否有convType=110、107、106
     */
    List<XieChengJudgeConvTypeValue> getJudgeConvType(String apiCode, String custNum);

    JSONObject getCondition(String apiCode);
}
