package com.br.marketing.service.Impl.wuba;

import com.br.marketing.entity.LocalFile;
import com.br.marketing.entity.WubaCollidingDataFront;

import java.util.List;

public interface WuBaCollidingDataBusinessService {
    void insertToRobAndUpdateFront(List<WubaCollidingDataFront> wubaCollidingDataFronts, LocalFile localFile);

    /**
     * 非周期据进入非金融
     * @param cells
     * @param apiCode
     */
    void saveLoopAnddeleteRob(List<String> cells, String apiCode, String dataSourceType);

    /**
     * 金融数据进入非金融
     * @param cells
     * @param apiCode
     */
    void saveLoopAnddeleteSecondLoop(List<String> cells, String apiCode);

    /**
     * 延期数据进入非金融周期表
     * @param cells
     * @param apiCode
     */
    void saveLoopAnddeleteDelay(List<String> cells, String apiCode);

    /**
     * 非金融数据进入非周期
     * @param data
     * @param apiCode
     */
    void deleteLoopAndSaveRob(List<String> data, String apiCode);

    /**
     * 延期数据进入非周期
     * @param data
     * @param apiCode
     */
    void deleteDelayAndSaveRob(List<String> data, String apiCode);

    /**
     * 非周期数据进入金融
     * @param cells
     * @param apiCode
     */
    void saveSecondLoopAnddeleteRob(List<String> cells, String apiCode, String dataSourceType);

    /**
     * 非金融数据进入金融
     * @param cells
     * @param apiCode
     */
    void saveSecondLoopAnddeleteLoop(List<String> cells, String apiCode);

    /**
     * 延期数据进入金融
     * @param cells
     * @param apiCode
     */
    void saveSecondLoopAnddeleteDelay(List<String> cells, String apiCode);

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
     * 延期数据进入非金融status=-2撞库包
     * @param cells
     * @param apiCode
     * @param packageId
     */
    void deleteDelayAndSaveReavedIntoRob(List<String> cells, String apiCode, Long packageId);

    /**
     * 金融数据进入金融status=-2撞库包
     * @param cells
     * @param apiCode
     * @param packageId
     */
    void deleteSecondLoopAndSaveReavedIntoRob(List<String> cells, String apiCode, Long packageId);

    /**
     * 补包数据，进入补包status=-2撞库包
     * @param cells
     * @param apiCode
     * @param packageId
     */
    void saveReavedIntoRob(List<String> cells, String apiCode, Long packageId, String sourceType);
}
