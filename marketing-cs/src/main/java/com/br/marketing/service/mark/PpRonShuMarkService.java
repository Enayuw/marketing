package com.br.marketing.service.mark;

import com.br.marketing.common.commondto.Result;
import com.br.marketing.entity.DataMarkConfig;
import com.br.marketing.entity.FlagData;

import java.util.List;

/**
 * @ClassName PpRonShuMarkService
 * @Author hong.chen
 * @Date 2025/2/19 18:14
 */
public interface PpRonShuMarkService {

    Result<Boolean> createCleanTask(Long localId);

    void markAndUpdateFlagStatus(List<FlagData> flagData, String apiCode, List<DataMarkConfig> dataMarkConfigs);
}
