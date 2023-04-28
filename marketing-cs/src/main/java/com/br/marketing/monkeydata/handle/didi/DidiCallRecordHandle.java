package com.br.marketing.monkeydata.handle.didi;

import com.alibaba.fastjson.JSON;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.client.didi.DiDiClient;
import com.br.marketing.client.didi.input.DiDiReqVO;
import com.br.marketing.client.didi.output.DiDiResponseTO;
import com.br.marketing.common.annoation.RetryMethod;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.constants.rediskey.RedisKeyConstant;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.entity.*;
import com.br.marketing.mapper.DidiCallRecordMapper;
import com.br.marketing.mapper.MarketingSyncUserMapper;
import com.br.marketing.service.TransferDataValidityPeriodService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.strategy.MethodRetryHandlerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author GuangChao.Zhang
 * @version 1.0
 * @date 2023/4/27 15:41
 */

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class DidiCallRecordHandle {

    private final MarketingCommonConfig marketingCommonConfig;

    private final DidiCallRecordMapper didiCallRecordMapper;

    private final RedisChgService redisChgService;

    private final MarketingSyncUserMapper marketingSyncUserMapper;

    private final TransferDataValidityPeriodService transferDataValidityPeriodService;

    private final MethodRetryHandlerService methodRetryHandlerService;
    private final static String JOB = "job";

    public void pushDidiCallRecord(List<String> pushDate, String sourceType) {
        //sourceType ="job" 是定时任务   "mq" 是实时发送
        if (JOB.equals(sourceType)) {
            // 创建线程池
            ThreadPoolExecutor didiCallRecordThread = BrExecutors.getThreadPool(marketingCommonConfig.getDidiCollRecordThread(), marketingCommonConfig.getDidiCollRecordThread());
            pushDate.forEach(date -> {
                Long minId = null;
                DidiCallRecordExample didiCallRecordExample = new DidiCallRecordExample();
                didiCallRecordExample.setOrderByClause("id asc");
                DidiCallRecordExample.Criteria criteria = didiCallRecordExample.createCriteria();
                criteria.andCreateDateEqualTo(Integer.valueOf(date)).andStatusEqualTo(0);
                if(minId!=null){
                    criteria.andIdGreaterThan(minId);
                }
                criteria.andLimit(2000);
                List<DidiCallRecord> didiCallRecords = didiCallRecordMapper.selectByExample(didiCallRecordExample);

                while (didiCallRecords.size()>0) {
                    // 更新minId 为当前集合最大的id
                    minId = didiCallRecords.get(didiCallRecords.size() - 1).getId();
                    for (DidiCallRecord didiCallRecord : didiCallRecords) {
                        didiCallRecordThread.submit(() -> pushDidiCallRecordData(didiCallRecord));
                    }
                }
            });
            didiCallRecordThread.shutdown();
            try {
                while (!didiCallRecordThread.awaitTermination(10L, TimeUnit.SECONDS)) {
                }
            } catch (Exception ex) {
                log.error(ex.getMessage(), ex);
            }

        }
    }

    public void pushDidiCallRecordData(DidiCallRecord didiCallRecord) {


        try {
            String custNum = didiCallRecord.getCustNum();
            String apiCode = didiCallRecord.getApiCode();
            Integer createDate = didiCallRecord.getCreateDate();
            Date createTime = didiCallRecord.getCreateTime();
            // 获取redis 锁
            String key = RedisKeyConstant.pushDidiCollRecordLock.concat(":")
                    .concat(apiCode)
                    .concat(custNum);
            String value = UUID.randomUUID().toString();

            redisChgService.lock(key, value);

            //查询当天是否推送过
            DidiCallRecordExample didiCallRecordExample = new DidiCallRecordExample();
            didiCallRecordExample.createCriteria()
                    .andCustNumEqualTo(custNum)
                    .andStatusEqualTo(1)
                    .andCreateDateEqualTo(createDate);
            if (didiCallRecordMapper.countByExample(didiCallRecordExample)==0) {
                MarketingSyncUser marketingSyncUser = marketingSyncUserMapper.selectSynsUserByCustNumLast(apiCode, custNum);
                MarketingTransferSyncUser marketingTransferSyncUser = new MarketingTransferSyncUser();
                marketingTransferSyncUser.setApiCode(marketingSyncUser.getApiCode());
                marketingTransferSyncUser.setUserType(marketingSyncUser.getUserType());
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                // 判断是否有效
                MarketingSyncUser newValidityPeriodData = transferDataValidityPeriodService.getNewValidityPeriodData(marketingTransferSyncUser, sdf.format(new Date()));
                if(newValidityPeriodData!=null){
                    didiCallRecord.setCell(newValidityPeriodData.getCell());
                    // 调接口推送
                    Result<DiDiResponseTO> resResultResult = methodRetryHandlerService.didiPushData(custNum,0);
                    if(resResultResult.getCode().equals(ResultCode.SUCCESS.getValue())){
                        DiDiResponseTO diDiResponseTO = resResultResult.getData();
                        DiDiResponseTO.ResResult data = diDiResponseTO.getData();
                        Boolean result = data.getResult();
                        String errorMessage = diDiResponseTO.getErrorMessage();
                        String errorCode = diDiResponseTO.getErrorCode();
                        didiCallRecord.setStatus(1);
                        didiCallRecord.setResult(result);
                        didiCallRecord.setErrorCode(errorCode);
                        didiCallRecord.setErrorMessage(errorMessage);
                    }else {
                        didiCallRecord.setStatus(3);
                        didiCallRecord.setSysMessage("非200,20000异常");
                    }

                }else {
                    didiCallRecord.setStatus(2);
                    didiCallRecord.setSysMessage("数据失效");
                }
            }else {
                didiCallRecord.setStatus(2);
                didiCallRecord.setSysMessage("数据重复");
            }
            // 处理返回结果
            didiCallRecordMapper.updateByPrimaryKeySelective(didiCallRecord);
            // 解锁
            redisChgService.unlock(key, value);
        } catch (Exception e) {
            log.error("滴滴接口推送异常", e);
        }
    }


}
