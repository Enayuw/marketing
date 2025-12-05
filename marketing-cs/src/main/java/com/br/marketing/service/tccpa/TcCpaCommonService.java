package com.br.marketing.service.tccpa;

import com.br.marketing.entity.TcyrCpaCollidingTask;

public interface TcCpaCommonService {

    void updateVolume();

    void updateVolumeByTask(TcyrCpaCollidingTask collidingTask);

    void updateVolumeByTaskId(Long taskId);
}
