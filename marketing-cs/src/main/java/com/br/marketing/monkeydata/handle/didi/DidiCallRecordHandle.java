package com.br.marketing.monkeydata.handle.didi;

import com.br.marketing.client.RedisChgService;
import com.br.marketing.client.didi.output.DiDiResponseTO;
import com.br.marketing.client.marketingapi.input.UploadDataDTO;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.constants.rediskey.RedisKeyConstant;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.entity.DidiCallRecord;
import com.br.marketing.entity.DidiCallRecordExample;
import com.br.marketing.entity.MarketingSyncUser;
import com.br.marketing.entity.MarketingTransferSyncUser;
import com.br.marketing.mapper.DidiCallRecordMapper;
import com.br.marketing.service.TransferDataValidityPeriodService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.strategy.MethodRetryHandlerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.CharSequenceUtils;
import org.apache.poi.ss.formula.functions.Now;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
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


    private final MethodRetryHandlerService methodRetryHandlerService;
    private final static String JOB = "job";

    public void pushDidiCallRecord(List<String> pushDate, String sourceType) {
        //sourceType ="job" 是定时任务   "mq" 是实时发送
        if (JOB.equals(sourceType)) {
            // 创建线程池
            ThreadPoolExecutor didiCallRecordThread = BrExecutors.getThreadPool(marketingCommonConfig.getDidiCallRecordThread(), marketingCommonConfig.getDidiCallRecordThread());
            pushDate.forEach(date -> {
                Long minId = null;


                while ( marketingCommonConfig.isDidiCallRecordSwitch()){
                    DidiCallRecordExample didiCallRecordExample = new DidiCallRecordExample();
                    didiCallRecordExample.setOrderByClause("id asc limit 2000");
                    DidiCallRecordExample.Criteria criteria = didiCallRecordExample.createCriteria();
                    criteria.andCreateDateEqualTo(Integer.valueOf(date)).andStatusEqualTo(0);
                    if(minId!=null){
                        criteria.andIdGreaterThan(minId);
                    }
                    List<DidiCallRecord> didiCallRecords = didiCallRecordMapper.selectByExample(didiCallRecordExample);
                    if(didiCallRecords.size()==0){
                        break;
                    }
                    // 更新minId 为当前集合最大的id
                    minId = didiCallRecords.get(didiCallRecords.size() - 1).getId();
                    for (DidiCallRecord didiCallRecord : didiCallRecords) {
                        didiCallRecordThread.submit(() ->   methodRetryHandlerService.didiPushData(didiCallRecord,0));
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
}
