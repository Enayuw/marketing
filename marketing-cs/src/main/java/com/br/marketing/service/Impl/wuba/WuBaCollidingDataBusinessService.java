package com.br.marketing.service.Impl.wuba;

import com.br.marketing.entity.LocalFile;
import com.br.marketing.entity.WubaCollidingDataFront;

import java.util.List;

public interface WuBaCollidingDataBusinessService {
    void insertToRobAndUpdateFront(List<WubaCollidingDataFront> wubaCollidingDataFronts, LocalFile localFile);

    /**
     * 非金融数据进入非周期
     * @param cells
     * @param apiCode
     */
    void saveLoopAnddeleteRob(List<String> cells, String apiCode);

    /**
     * 金融数据进入非金融
     * @param cells
     * @param apiCode
     */
    void saveLoopAnddeleteSecondLoop(List<String> cells, String apiCode);

    /**
     * 非金融数据进入非周期
     * @param data
     * @param apiCode
     */
    void deleteLoopAndSaveRob(List<String> data, String apiCode);

    /**
     * 金融数据进入非周期
     * @param cells
     * @param apiCode
     */
    void saveSecondLoopAnddeleteRob(List<String> cells, String apiCode);

    /**
     * 非金融数据进入金融
     * @param cells
     * @param apiCode
     */
    void saveSecondLoopAnddeleteLoop(List<String> cells, String apiCode);

    /**
     * 非金融数据进入非周期
     * @param data
     * @param apiCode
     */
    void deleteSecondLoopAndSaveRob(List<String> data, String apiCode);

    /**
     * 非金融数据进入非金融status=-2撞库包
     * @param cells
     * @param apiCode
     * @param packageId
     */
    void deleteLoopAndSaveReavedIntoRob(List<String> cells, String apiCode, Long packageId);

    /**
     * 金融数据进入金融status=-2撞库包
     * @param cells
     * @param apiCode
     * @param packageId
     */
    void deleteSecondLoopAndSaveReavedIntoRob(List<String> cells, String apiCode, Long packageId);
}
