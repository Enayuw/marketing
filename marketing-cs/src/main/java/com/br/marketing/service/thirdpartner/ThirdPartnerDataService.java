package com.br.marketing.service.thirdpartner;

import com.br.marketing.common.commondto.Result;
import com.br.marketing.service.thirdpartner.dto.ThirdPartnerDataDTO;

import java.util.List;

/**
 * @Description 三方数据处理接口
 * @Author hong.chen
 * @CreateTime 2024/11/28
 */
public interface ThirdPartnerDataService {
    Result saveData(List<ThirdPartnerDataDTO> dataList, String accessNumber, String originalData);
}
