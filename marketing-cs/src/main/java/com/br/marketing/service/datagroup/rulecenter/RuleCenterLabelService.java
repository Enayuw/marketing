package com.br.marketing.service.datagroup.rulecenter;

import com.br.marketing.common.commondto.Result;
import com.br.marketing.dto.PushCustomerDTO;

import java.util.List;
import java.util.Set;

public interface RuleCenterLabelService {
    Result<Set<String>> getLabelNames(String apiCode);

    Result saveLabelTask(PushCustomerDTO dto);
}
