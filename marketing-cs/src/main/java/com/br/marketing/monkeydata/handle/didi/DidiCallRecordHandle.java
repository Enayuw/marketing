package com.br.marketing.monkeydata.handle.didi;

import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.entity.DidiCallRecord;
import com.br.marketing.entity.DidiCallRecordExample;
import com.br.marketing.mapper.DidiCallRecordMapper;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.strategy.MethodRetryHandlerService;
import com.br.marketing.util.ThreadPoolAdjustmentUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author GuangChao.Zhang
 * @version 1.0
 * @Description: 滴滴触达逻辑说明：滴滴触达需要判断数据推送当天是否有效，并判断数据当天是否推送过，
 * 由于是并发处理，数据去重需要加Redis 锁。
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
                    ThreadPoolAdjustmentUtil.adjustThreadPoolSize(didiCallRecordThread, marketingCommonConfig.getDidiCallRecordThread());
                    DidiCallRecordExample didiCallRecordExample = new DidiCallRecordExample();
                    didiCallRecordExample.setOrderByClause("id asc limit 2000");
                    DidiCallRecordExample.Criteria criteria = didiCallRecordExample.createCriteria();
                    criteria.andCreateDateEqualTo(Integer.valueOf(date)).andStatusEqualTo(1);
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
