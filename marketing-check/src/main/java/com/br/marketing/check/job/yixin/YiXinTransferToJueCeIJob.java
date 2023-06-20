package com.br.marketing.check.job.yixin;


import com.br.marketing.mapper.MarketingTransferInfoMapper;
import com.br.marketing.service.Impl.TableCreateServiceImpl;
import com.br.marketing.service.YiXinToJueCeProcessService;
import com.br.marketing.service.ZnkfPushService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * @Author 张广超
 * @Date 2023/6/16 17:16
 * @Description: 宜信转化数据推决策
 **/
@Component
@Slf4j
public class YiXinTransferToJueCeIJob extends AbstractSimpleElasticJob {

    private static final List<String> actonTypeList = new ArrayList<String>(){
        {
            add("A");
            add("B");
            add("C");
            add("D");
            add("E");
            add("F");
            add("G");
            add("H");
            add("I");
        }
    };
    @Resource
    private YiXinToJueCeProcessService yiXinToJueCeProcessService;

    @Resource
    private ZnkfPushService znkfPushService;

    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Resource
    private TableCreateServiceImpl tableCreateService;

    @Resource
    private MarketingTransferInfoMapper marketingTransferInfoMapper;
    @Override
    public void process(JobExecutionMultipleShardingContext context) {
        // 黑名单接口是否推送完成
        // 当前时间是否>11 点 2 者满足其一就推送
        Boolean pushBlackPhoneEnd = znkfPushService.isPushBlackPhoneEnd(marketingCommonConfig.getYiXinGetTransferBlackListToJueCeApiCode(),
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        int hour = LocalDateTime.now().getHour();
        String tcId = tableCreateService.getTcId(marketingCommonConfig.getYiXinTransferToJueCeApiCode());


        if (pushBlackPhoneEnd || hour >= 11) {
            actonTypeList.forEach(e-> yiXinToJueCeProcessService.doProcess(e,tcId));
        }
    }
}

