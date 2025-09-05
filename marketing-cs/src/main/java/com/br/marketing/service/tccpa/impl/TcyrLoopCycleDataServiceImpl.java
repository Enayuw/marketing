package com.br.marketing.service.tccpa.impl;

import com.br.common.log.AlertLog;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.dto.tccpa.TcyrCpaSuccessMqDTO;
import com.br.marketing.entity.MarketingTcyrCpaDataLog;
import com.br.marketing.entity.MarketingTcyrCpaLoopCycle;
import com.br.marketing.entity.MarketingTcyrCpaRob;
import com.br.marketing.entity.MarketingTcyrCpaSuccessData;
import com.br.marketing.enums.TcCpaIsDelEnum;
import com.br.marketing.mapper.MarketingTcyrCpaDataLogMapper;
import com.br.marketing.mapper.MarketingTcyrCpaLoopCycleMapper;
import com.br.marketing.mapper.MarketingTcyrCpaRobMapper;
import com.br.marketing.mapper.MarketingTcyrCpaSuccessDataMapper;
import com.br.marketing.service.tccpa.TcyrLoopCycleDataService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import javax.annotation.Resource;
import java.util.Date;

/**
 * 同程易融CPA 撞库成功实现类
 */
@Slf4j
@Service
public class TcyrLoopCycleDataServiceImpl implements TcyrLoopCycleDataService {

    private final static String TITLE = "【同程易融CPA-colliding周期剔除任务】";

    @Resource
    private MarketingTcyrCpaSuccessDataMapper successDataMapper;

    @Resource
    private MarketingTcyrCpaRobMapper robMapper;

    @Resource
    private MarketingTcyrCpaLoopCycleMapper loopCycleMapper;

    @Resource
    private MarketingTcyrCpaDataLogMapper dataLogMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Boolean> process(TcyrCpaSuccessMqDTO tcyrCpaSuccessMqDTO) {
        Result<Boolean> result = new Result<>().setCode(ResultCode.SUCCESS.getValue());
        Long dataId = tcyrCpaSuccessMqDTO.getDataId();
        String requestId = tcyrCpaSuccessMqDTO.getRequestId();
        String sourceType =null;
        Long packageId= null;
        boolean lockResult = false;
        try {
            MarketingTcyrCpaSuccessData cpaSuccessData = successDataMapper.selectByPrimaryKey(dataId);
            if (cpaSuccessData != null) {
                if (cpaSuccessData.getStatus() == 1 && cpaSuccessData.getCell() != null) {
                    MarketingTcyrCpaRob tcyrCpaRob = robMapper.selectByUserKey(cpaSuccessData.getUserKey(), TcCpaIsDelEnum.DEL_NO.getValue());
                    if (tcyrCpaRob != null) {
                        sourceType = "F";
                        packageId = tcyrCpaRob.getPackageId();
                        robMapper.updateDelStatusById(tcyrCpaRob.getId(),TcCpaIsDelEnum.DEL_YES.getValue());
                        saveTcyrCpaLoopCyle(cpaSuccessData,packageId,sourceType,requestId);
                    } else {
                        sourceType = "T";
                        MarketingTcyrCpaLoopCycle oldLoopCycle = loopCycleMapper.selectByUserKey(cpaSuccessData.getUserKey(),TcCpaIsDelEnum.DEL_NO.getValue());
                        if (oldLoopCycle != null) {
                            loopCycleMapper.updateInfoById(oldLoopCycle.getId(),cpaSuccessData.getEndDate(),sourceType,cpaSuccessData.getExtend());
                        }else {
                            saveTcyrCpaLoopCyle(cpaSuccessData,null,sourceType,requestId);
                        }
                    }
                    lockResult = true;
                }
                saveCollidingLog(cpaSuccessData,requestId,sourceType,packageId,lockResult);
            }
            result.setDate(Boolean.FALSE);
        } catch (DuplicateKeyException e) {
//            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TONGCHENG_CPA_SERVICEERROR.getCode(),
//                    "同程cpa撞库成功周期剔除数据重入异常,requestId:" + requestId +e.getMessage(), TITLE), e);
            log.warn("TITLE:{},同程cpa撞库成功周期剔除数据重入异常,requestId:{}",TITLE, requestId  , e);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            result.setDate(Boolean.FALSE);
        } catch (Exception e) {
            log.warn("TITLE:{},同程cpa撞库成功周期剔除数据异常,requestId:{}", TITLE,requestId, e);//            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TONGCHENG_CPA_SERVICEERROR.getCode(),
//                    "同程cpa撞库成功周期剔除数据异常,requestId:" + requestId +e.getMessage(), TITLE), e);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            result.setDate(Boolean.TRUE);
        }
        return  result;
    }



    private void saveTcyrCpaLoopCyle(MarketingTcyrCpaSuccessData cpaSuccessData, Long packageId, String sourceType,String requestId) {
        Date nowDate = new Date();
        MarketingTcyrCpaLoopCycle loopCycle = new MarketingTcyrCpaLoopCycle();
        loopCycle.setApiCode(cpaSuccessData.getApiCode());
        loopCycle.setPackageId(packageId);
        loopCycle.setDataSourceType(sourceType);
        loopCycle.setDataId(cpaSuccessData.getId());
        loopCycle.setUserKey(cpaSuccessData.getUserKey());
        loopCycle.setCell(cpaSuccessData.getCell());
        loopCycle.setReleaseTime(cpaSuccessData.getEndDate());
        loopCycle.setReceiveTime(nowDate);
        loopCycle.setCreateTime(nowDate);
        loopCycle.setUpdateTime(nowDate);
        loopCycle.setIsDel(TcCpaIsDelEnum.DEL_NO.getValue());
        loopCycle.setExtend(cpaSuccessData.getExtend());
        loopCycle.setRequestId(requestId);
        loopCycleMapper.insertSelective(loopCycle);
    }

    private void saveCollidingLog(MarketingTcyrCpaSuccessData cpaSuccessData,String requestId, String sourceType, Long packageId,Boolean lockResult) {
        Date nowDate = new Date();
        MarketingTcyrCpaDataLog marketingTcyrCpaDataLog = new MarketingTcyrCpaDataLog();
        marketingTcyrCpaDataLog.setCpaCollidingDataId(cpaSuccessData.getId());
        marketingTcyrCpaDataLog.setPackageId(packageId);
        marketingTcyrCpaDataLog.setDataSourceType(sourceType);
        marketingTcyrCpaDataLog.setUserKey(cpaSuccessData.getUserKey());
        marketingTcyrCpaDataLog.setCell(cpaSuccessData.getCell());
        marketingTcyrCpaDataLog.setReleaseTime(cpaSuccessData.getEndDate());
        marketingTcyrCpaDataLog.setReceiveTime(nowDate);
        marketingTcyrCpaDataLog.setResult(lockResult);
        marketingTcyrCpaDataLog.setOriginText(cpaSuccessData.getOriginText());
        marketingTcyrCpaDataLog.setExtend(cpaSuccessData.getExtend());
        marketingTcyrCpaDataLog.setIsDel(TcCpaIsDelEnum.DEL_NO.getValue());
        marketingTcyrCpaDataLog.setCreateTime(nowDate);
        marketingTcyrCpaDataLog.setUpdateTime(nowDate);
        marketingTcyrCpaDataLog.setRequestId(requestId);
        dataLogMapper.insertSelective(marketingTcyrCpaDataLog);
    }
}
