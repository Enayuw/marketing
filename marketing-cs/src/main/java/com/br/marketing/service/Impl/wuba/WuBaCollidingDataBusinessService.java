package com.br.marketing.service.Impl.wuba;

import com.br.marketing.entity.LocalFile;
import com.br.marketing.entity.WubaCollidingDataFront;
import com.br.marketing.entity.WubaCollidingDataSyncClean;

import java.util.List;

public interface WuBaCollidingDataBusinessService {
    void insertToRobAndUpdateFront(List<WubaCollidingDataFront> wubaCollidingDataFronts, LocalFile localFile);
    void saveLoopAnddeleteRob(List<String> cells, String apiCode);
    void deleteLoopAndSaveRob(List<WubaCollidingDataSyncClean> data, String apiCode);
}
