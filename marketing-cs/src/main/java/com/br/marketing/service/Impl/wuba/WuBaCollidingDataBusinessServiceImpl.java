package com.br.marketing.service.Impl.wuba;

import com.br.common.log.AlertLog;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.entity.LocalFile;
import com.br.marketing.entity.WubaCollidingDataFront;
import com.br.marketing.mapper.WubaCollidingDataFrontMapper;
import com.br.marketing.mapper.WubaCollidingDataLoopCycleMapper;
import com.br.marketing.mapper.WubaCollidingDataRobMapper;
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
        wubaCollidingDataLoopCycleMapper.batchSaveData(cells, apiCode);
        wubaCollidingDataRobMapper.batchDeleteByCell(cells, apiCode);
    }

    /**
     * 不可营销数据从周期表删除，并保存到非周期表
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteLoopAndSaveRob(List<String> cells, String apiCode) {
        wubaCollidingDataLoopCycleMapper.batchDeleteByCell(cells, apiCode);
        wubaCollidingDataRobMapper.batchSaveTrueToFalseData(cells, apiCode);
    }
}