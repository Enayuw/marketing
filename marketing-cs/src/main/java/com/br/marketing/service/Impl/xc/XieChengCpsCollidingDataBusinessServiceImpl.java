package com.br.marketing.service.Impl.xc;

import com.br.common.log.AlertLog;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.entity.LocalFile;
import com.br.marketing.entity.XieChengCpsCollidingDataFront;
import com.br.marketing.mapper.XieChengCpsCollidingDataFrontMapper;
import com.br.marketing.mapper.XieChengCpsCollidingDataRobMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

/**
 * 携程CPS撞库业务服务实现类
 * @Author chenh
 * @Date 2025-06-26
 */
@Service
@Slf4j
public class XieChengCpsCollidingDataBusinessServiceImpl implements XieChengCpsCollidingDataBusinessService {
    
    @Resource
    XieChengCpsCollidingDataFrontMapper xieChengCpsCollidingDataFrontMapper;
    
    @Resource
    XieChengCpsCollidingDataRobMapper xieChengCpsCollidingDataRobMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void insertToRobAndUpdateFront(List<XieChengCpsCollidingDataFront> frontList, LocalFile localFile) {
        try {
            // 批量保存到rob表
            xieChengCpsCollidingDataRobMapper.batchSaveData(frontList, localFile.getId());
            // 批量更新front表推送状态
            xieChengCpsCollidingDataFrontMapper.batchUpdatePushStatusByCell(frontList, localFile.getId());
        } catch (Exception e) {
            String subject = "携程CPS同步撞库数据作业，数据入库，子线程处理异常！";
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.XIECHENG_SERVICEERROR.getCode(), e.getMessage(), subject), e);
        }
    }
} 