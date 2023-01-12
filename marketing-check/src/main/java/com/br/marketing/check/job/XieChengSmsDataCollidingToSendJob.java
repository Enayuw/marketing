package com.br.marketing.check.job;

import com.br.marketing.client.AlarmApiClient;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.entity.LocalFile;
import com.br.marketing.entity.LocalFileExample;
import com.br.marketing.entity.XieChengSmsCollidingData;
import com.br.marketing.entity.XieChengSmsCollidingDataExample;
import com.br.marketing.mapper.LocalFileMapper;
import com.br.marketing.mapper.XieChengDataMapper;
import com.br.marketing.mapper.XieChengSmsCollidingDataMapper;
import com.br.marketing.mapper.XiechengSmsQuitDataMapper;
import com.br.marketing.rabbitmq.RabbitMqProducter;
import com.br.marketing.service.PushDataService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author guangchao.zhang
 * @Classname CallingToSendJob
 * @Description 回调第三方接口发送不打信息
 * @Date 2022/2/16 10:02 AM
 */
@Component
@Slf4j
public class XieChengSmsDataCollidingToSendJob extends AbstractSimpleElasticJob {

    private final static String XIECHENGSMSCOLLIDING = "xiechengsmscolliding";

    @Autowired
    RabbitMqProducter producter;
    @Resource
    private LocalFileMapper localFileMapper;
    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Resource
    private XieChengSmsCollidingDataMapper xieChengSmsCollidingDataMapper;

    @Resource
    private PushDataService pushDataService;

    @Resource
    private AlarmApiClient alarmClient;
    @Override
    public void process(JobExecutionMultipleShardingContext jobExecutionMultipleShardingContext) {
        // 创建线程池
        ThreadPoolExecutor xieChengSmsCollidingThread = BrExecutors.getThreadPool(marketingCommonConfig.getXieChengSmsCollidingThread(), marketingCommonConfig.getXieChengSmsCollidingThread());
//        ThreadPoolExecutor xieChengSmsCollidingThread = BrExecutors.getThreadPool(5,5);

        Boolean actionMark = true;
        Date endTime = getTimeDay(marketingCommonConfig.getXieChengSmsCollidingDays());
        long startTime = endTime.getTime() - (60 * 60 * 1000);
        // 根据id 进行数据查询 每批次查询 1.5w
        Long minId = null;
        AtomicInteger failNum = new AtomicInteger(0);
        while (actionMark) {
            List<XieChengSmsCollidingData> xieChengSmsCollidingDataList = xieChengSmsCollidingDataMapper.selectById(minId,new Date(startTime),endTime);
            if (xieChengSmsCollidingDataList.size() == 0) {
                actionMark = false;
                continue;
            }
            // 更新minId 为当前集合最大的id
            minId = xieChengSmsCollidingDataList.get(xieChengSmsCollidingDataList.size() - 1).getId();
            // 将查询出来的明细数据进行分组，每组50个数据
            List<List<XieChengSmsCollidingData>> xieChengSmsCollidingDataPartitions = Lists.partition(xieChengSmsCollidingDataList, 50);
            for (int i = 0; i < xieChengSmsCollidingDataPartitions.size(); i++) {
                List<XieChengSmsCollidingData> xieChengSmsCollidingDataListPartition = xieChengSmsCollidingDataPartitions.get(i);
                xieChengSmsCollidingThread.submit(() -> pushDataService.pushXieChengSmsCollidingData(xieChengSmsCollidingDataListPartition, failNum));
            }
        }
        xieChengSmsCollidingThread.shutdown();
        try {
            while (!xieChengSmsCollidingThread.awaitTermination(10L, TimeUnit.SECONDS)) {
            }
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        }
        xieChengSendAlarm(failNum, "携程短信轮询撞库接口推送异常，请检查");
    }
    private void xieChengSendAlarm(AtomicInteger failNum,String title){
        if (failNum.get() > 0) {
            try {
                alarmClient.sendAlarm("推送失败条数=" + failNum.get(), title, AlarmSendCodeEnum.EXCEPTION_URGENT.getCode());
            } catch (Exception ex) {
                log.error(ex.getMessage(), ex);
            }
        }
    }
    public static Date getTimeDay(int index) {
        TimeZone tz = TimeZone.getTimeZone("Asia/Shanghai");
        TimeZone.setDefault(tz);
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        calendar.add(Calendar.DAY_OF_MONTH, -index);
        return  calendar.getTime();
    }
    /**
     * 通过时间秒毫秒数判断两个时间的间隔
     *
     * @param date1
     * @param date2
     * @return
     */
    public static boolean differentDaysByMillisecond(Date date1, Date date2, int hours) {
        int days = ((int) ((date2.getTime() - date1.getTime()) / (1000 * 3600)));
        return days / hours > 0 && days % hours == 0;
    }

}
