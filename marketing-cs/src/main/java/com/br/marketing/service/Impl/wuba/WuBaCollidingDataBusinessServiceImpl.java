package com.br.marketing.service.Impl.wuba;

import com.br.common.log.AlertLog;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.entity.LocalFile;
import com.br.marketing.entity.WubaCollidingDataFront;
import com.br.marketing.mapper.WubaCollidingDataFrontMapper;
import com.br.marketing.mapper.WubaCollidingDataLoopCycleMapper;
import com.br.marketing.mapper.WubaCollidingDataRobMapper;
import com.br.marketing.mapper.WubaCollidingDataSecondLoopCycleMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

/**
 * @Description WuBaCollidingDataBusinessService
 * @Author hong.chen
 * @CreateTime 2024/07/11
 */
@Service
@Slf4j
public class WuBaCollidingDataBusinessServiceImpl implements WuBaCollidingDataBusinessService {
    @Resource
    WubaCollidingDataFrontMapper wubaCollidingDataFrontMapper;
    @Resource
    WubaCollidingDataRobMapper wubaCollidingDataRobMapper;
    @Resource
    WubaCollidingDataLoopCycleMapper wubaCollidingDataLoopCycleMapper;
    @Resource
    WubaCollidingDataSecondLoopCycleMapper wubaCollidingDataSecondLoopCycleMapper;

    @Transactional(rollbackFor = Exception.class)
    public void insertToRobAndUpdateFront(List<WubaCollidingDataFront> wubaCollidingDataFronts, LocalFile localFile) {
        try {
            wubaCollidingDataRobMapper.batchSaveData(wubaCollidingDataFronts, localFile.getApiCode());
            wubaCollidingDataFrontMapper.batchUpdatePushStatusByCell(wubaCollidingDataFronts, localFile.getId(), localFile.getApiCode());
        } catch (Exception e) {
            String subject = "58同步撞库数据作业，子线程处理异常！";
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.EXCEPTION_WUBA.getCode(), e.getMessage()
                    , subject), e);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void saveLoopAnddeleteRob(List<String> cells, String apiCode) {
        wubaCollidingDataLoopCycleMapper.batchSaveData(cells, apiCode, "F");
        wubaCollidingDataRobMapper.batchDeleteByCell(cells, apiCode);
    }

    @Transactional(rollbackFor = Exception.class)
    public void saveSecondLoopAnddeleteRob(List<String> cells, String apiCode) {
        wubaCollidingDataSecondLoopCycleMapper.batchSaveData(cells, apiCode,"F");
        wubaCollidingDataRobMapper.batchDeleteByCell(cells, apiCode);
    }

    @Transactional(rollbackFor = Exception.class)
    public void saveSecondLoopAnddeleteLoop(List<String> cells, String apiCode) {
        wubaCollidingDataSecondLoopCycleMapper.batchSaveData(cells, apiCode, "T");
        wubaCollidingDataLoopCycleMapper.batchDeleteByCell(cells, apiCode);
    }

    @Transactional(rollbackFor = Exception.class)
    public void saveLoopAnddeleteSecondLoop(List<String> cells, String apiCode) {
        wubaCollidingDataLoopCycleMapper.batchSaveData(cells, apiCode, "S");
        wubaCollidingDataSecondLoopCycleMapper.batchDeleteByCell(cells, apiCode);
    }

    /**
     * 不可营销数据从非金融周期表删除，并保存到非周期表
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteLoopAndSaveRob(List<String> cells, String apiCode) {
        wubaCollidingDataLoopCycleMapper.batchDeleteByCell(cells, apiCode);
        wubaCollidingDataRobMapper.batchSaveTrueToFalseData(cells, apiCode, "T");
    }

    /**
     * 不可营销数据从金融周期表删除，并保存到非周期表
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteSecondLoopAndSaveRob(List<String> cells, String apiCode) {
        wubaCollidingDataSecondLoopCycleMapper.batchDeleteByCell(cells, apiCode);
        wubaCollidingDataRobMapper.batchSaveTrueToFalseData(cells, apiCode, "S");
    }

    /**
     * 撞回status=-2数据从非金融周期表删除，并保存到非金融-2撞库包
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteLoopAndSaveReavedIntoRob(List<String> cells, String apiCode, Long packageId) {
        wubaCollidingDataLoopCycleMapper.batchDeleteByCell(cells, apiCode);
        wubaCollidingDataRobMapper.batchSaveReavedDataInToRob(cells, apiCode, "T", packageId);
    }

    /**
     * 撞回status=-2数据从金融周期表删除，并保存到金融-2撞库包
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteSecondLoopAndSaveReavedIntoRob(List<String> cells, String apiCode, Long packageId) {
        wubaCollidingDataSecondLoopCycleMapper.batchDeleteByCell(cells, apiCode);
        wubaCollidingDataRobMapper.batchSaveReavedDataInToRob(cells, apiCode, "S", packageId);
    }
}