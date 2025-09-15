package com.br.marketing.service.datagroup.rulecenter;

import com.br.marketing.common.commondto.Result;
import com.br.marketing.dto.PushCustomerDTO;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface RuleCenterLabelService {
    Result<Set<String>> getLabelNames(String apiCode);

    Result saveLabelTask(PushCustomerDTO dto);

    Result<Boolean> getScoreMergeMark(String batchNumbers,String apiCode);

    Result<Map<String, Integer>> getScoreMergeNum(String batchNumbers, String apiCode);


    Integer scoreMergePreCalculate(PushCustomerDTO dto);

    String scoreMergeAssemble(PushCustomerDTO dto);

    String scoreMergeFieldMapping(String sqlCondition, List<String> batchNumberList, String apiCode);

}
