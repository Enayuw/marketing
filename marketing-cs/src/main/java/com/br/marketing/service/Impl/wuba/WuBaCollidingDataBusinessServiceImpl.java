package com.br.marketing.service.Impl.wuba;

import com.br.common.log.AlertLog;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.entity.LocalFile;
import com.br.marketing.entity.WubaCollidingDataFront;
import com.br.marketing.entity.WubaCollidingDataFrontExample;
import com.br.marketing.mapper.WubaCollidingDataDelayLoopCycleMapper;
import com.br.marketing.mapper.WubaCollidingDataFrontMapper;
import com.br.marketing.mapper.WubaCollidingDataLoopCycleMapper;
import com.br.marketing.mapper.WubaCollidingDataRobMapper;
import com.br.marketing.mapper.WubaCollidingDataSecondLoopCycleMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

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
    @Resource
    WubaCollidingDataDelayLoopCycleMapper wubaCollidingDataDelayLoopCycleMapper;
    @Resource
    WuBaCollidingDataSynchronismService wuBaCollidingDataSynchronismService;

    @Transactional(rollbackFor = Exception.class)
    public void insertToRobAndUpdateFront(List<WubaCollidingDataFront> wubaCollidingDataFronts, LocalFile localFile) {
        try {
            wubaCollidingDataRobMapper.batchSaveData(wubaCollidingDataFronts, localFile.getApiCode());
            wubaCollidingDataFrontMapper.batchUpdatePushStatusByCell(wubaCollidingDataFronts, localFile.getId(), localFile.getApiCode());
        } catch (Exception e) {
            String subject = "58同步撞库数据作业，数据入库，子线程处理异常！";
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.EXCEPTION_WUBA.getCode(), e.getMessage()
                    , subject), e);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void saveLoopAnddeleteRob(List<String> cells, String apiCode, String dataSourceType) {
        wubaCollidingDataLoopCycleMapper.batchSaveData(cells, apiCode, dataSourceType);
        wubaCollidingDataRobMapper.batchDeleteByCell(cells, apiCode);
    }

    @Transactional(rollbackFor = Exception.class)
    public void saveSecondLoopAnddeleteRob(List<String> cells, String apiCode, String dataSourceType) {
        wubaCollidingDataSecondLoopCycleMapper.batchSaveData(cells, apiCode, dataSourceType);
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

    @Transactional(rollbackFor = Exception.class)
    public void saveSecondLoopAnddeleteDelay(List<String> cells, String apiCode) {
        wubaCollidingDataSecondLoopCycleMapper.batchSaveData(cells, apiCode, "D");
        wubaCollidingDataDelayLoopCycleMapper.batchDeleteByCell(cells, apiCode);
    }

    @Transactional(rollbackFor = Exception.class)
    public void saveLoopAnddeleteDelay(List<String> cells, String apiCode) {
        wubaCollidingDataLoopCycleMapper.batchSaveData(cells, apiCode, "D");
        wubaCollidingDataDelayLoopCycleMapper.batchDeleteByCell(cells, apiCode);
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
     * 不可营销数据从延期表删除，并保存到非周期表
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteDelayAndSaveRob(List<String> cells, String apiCode) {
        wubaCollidingDataDelayLoopCycleMapper.batchDeleteByCell(cells, apiCode);
        wubaCollidingDataRobMapper.batchSaveTrueToFalseData(cells, apiCode, "D");
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
    public void deleteLoopAndSaveReavedIntoRob(List<String> cells, String apiCode, Long packageId) {
        wubaCollidingDataLoopCycleMapper.batchDeleteByCell(cells, apiCode);
        List<String> reavedCellsExcludeHighValue = getReavedCellsExcludeHighValue(apiCode, cells);
        if (CollectionUtils.isEmpty(reavedCellsExcludeHighValue)) {
            return;
        }
        wubaCollidingDataRobMapper.batchSaveReavedDataInToRob(reavedCellsExcludeHighValue, apiCode, "T", packageId);
    }

    /**
     * 撞回status=-2数据从延期表删除，并保存到非金融-2撞库包
     */
    public void deleteDelayAndSaveReavedIntoRob(List<String> cells, String apiCode, Long packageId) {
        wubaCollidingDataDelayLoopCycleMapper.batchDeleteByCell(cells, apiCode);
        List<String> reavedCellsExcludeHighValue = getReavedCellsExcludeHighValue(apiCode, cells);
        if (CollectionUtils.isEmpty(reavedCellsExcludeHighValue)) {
            return;
        }
        wubaCollidingDataRobMapper.batchSaveReavedDataInToRob(reavedCellsExcludeHighValue, apiCode, "D", packageId);
    }

    /**
     * 撞回status=-2数据从金融周期表删除，并保存到金融-2撞库包
     */
    public void deleteSecondLoopAndSaveReavedIntoRob(List<String> cells, String apiCode, Long packageId) {
        wubaCollidingDataSecondLoopCycleMapper.batchDeleteByCell(cells, apiCode);
        List<String> reavedCellsExcludeHighValue = getReavedCellsExcludeHighValue(apiCode, cells);
        if (CollectionUtils.isEmpty(reavedCellsExcludeHighValue)) {
            return;
        }
        wubaCollidingDataRobMapper.batchSaveReavedDataInToRob(reavedCellsExcludeHighValue, apiCode, "S", packageId);
    }

    /**
     * 撞回status=-2的补包数据，进入补包status=-2撞库包
     */
    public void saveReavedIntoRob(List<String> cells, String apiCode, Long packageId, String sourceType) {
        wubaCollidingDataRobMapper.batchSaveReavedDataInToRob(cells, apiCode, sourceType, packageId);
    }

    /**
     * 撞得status=-2中剔除高价值数据
     */
    private List<String> getReavedCellsExcludeHighValue(String apiCode, List<String> reavedCells) {
        List<Long> highValueIdList = wuBaCollidingDataSynchronismService.getHighValueFileIds(apiCode);
        if (Objects.isNull(highValueIdList)) {
            return reavedCells;
        }

        WubaCollidingDataFrontExample example = new WubaCollidingDataFrontExample();
        example.createCriteria().andApiCodeEqualTo(apiCode).andIsDeletedEqualTo(0)
                .andLocalIdIn(highValueIdList).andCellIn(reavedCells);
        List<WubaCollidingDataFront> highValueReavedDatas = wubaCollidingDataFrontMapper.selectByExample(example);
        List<String> highValueReavedCells = highValueReavedDatas.stream().map(WubaCollidingDataFront::getCell).collect(Collectors.toList());

        return reavedCells.stream().filter((String reavedCell) -> !highValueReavedCells.contains(reavedCell)).collect(Collectors.toList());
    }
}