package com.br.marketing.service.tccpa;

import com.br.marketing.dto.tccpa.TcCpaDeleteRuleExecuteInfoDTO;
import com.br.marketing.entity.TcyrCpaCollidingTask;
import com.br.marketing.entity.TcyrCpaDeleteRule;

import java.util.List;

public interface TcCpaCommonService {

    void updateVolume();

    void updateVolumeByTask(TcyrCpaCollidingTask collidingTask);

    void updateVolumeByTaskId(Long taskId);

    Integer calculateVolume(List<TcCpaDeleteRuleExecuteInfoDTO> executeInfos);

    Integer convertFailMsgToLockBelong(Integer failMsg);

    Integer convertLockBelongToFailMsg(Integer lockBelong);
}
