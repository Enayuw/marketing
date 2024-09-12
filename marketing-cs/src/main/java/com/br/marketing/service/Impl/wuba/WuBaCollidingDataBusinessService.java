package com.br.marketing.service.Impl.wuba;

import com.br.marketing.entity.LocalFile;
import com.br.marketing.entity.WubaCollidingDataFront;

import java.util.List;

public interface WuBaCollidingDataBusinessService {
    void insertToRobAndUpdateFront(List<WubaCollidingDataFront> wubaCollidingDataFronts, LocalFile localFile);

    void saveLoopAnddeleteRob(List<String> cells, String apiCode);
    void saveLoopAnddeleteSecondLoop(List<String> cells, String apiCode);
    void deleteLoopAndSaveRob(List<String> data, String apiCode);

    void saveSecondLoopAnddeleteRob(List<String> cells, String apiCode);
    void saveSecondLoopAnddeleteLoop(List<String> cells, String apiCode);
    void deleteSecondLoopAndSaveRob(List<String> data, String apiCode);
}
