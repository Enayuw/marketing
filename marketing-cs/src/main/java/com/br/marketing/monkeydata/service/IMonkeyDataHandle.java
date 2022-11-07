package com.br.marketing.monkeydata.service;

import com.br.marketing.common.commondto.Result;
import com.br.marketing.monkeydata.entity.InputData;
import com.br.marketing.monkeydata.entity.OutputData;

import java.util.List;

public interface IMonkeyDataHandle {

    /**
     * 获取输入数据
     * @return
     */
    List<InputData> getInputData();

    /**
     * 数据过程处理
     * @param inList
     * @return
     */
    List<OutputData> processData(List<InputData> inList);



}
