package com.br.marketing.service.tccpa;

import com.br.marketing.dto.tccpa.TcCpaDeleteRuleExecuteInfoDTO;
import com.br.marketing.entity.TcyrCpaCollidingTask;
import java.io.IOException;
import java.util.List;

public interface TcCpaCommonService {

    void updateVolumeByTask(TcyrCpaCollidingTask collidingTask) throws IOException;

    Integer calculateVolume(List<TcCpaDeleteRuleExecuteInfoDTO> executeInfos);

    String getDeleteSqlFrag(String deleteRuleIdStr) throws IOException;
}
