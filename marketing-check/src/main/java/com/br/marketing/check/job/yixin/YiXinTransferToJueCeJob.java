package com.br.marketing.check.job.yixin;


import com.br.marketing.mapper.MarketingTransferInfoMapper;
import com.br.marketing.service.Impl.TableCreateServiceImpl;
import com.br.marketing.service.YiXinToJueCeProcessService;
import com.br.marketing.service.ZnkfPushService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * @Author 张广超
 * @Date 2023/6/16 17:16
 * @Description: 宜信转化数据推决策
 **/
@Component
@Slf4j
public class YiXinTransferToJueCeJob extends AbstractSimpleElasticJob {
    /**
     * actionType  A
     * type  13 23 8
     */
    private static final TreeMap<String, String> actonTypeTree = new TreeMap<String, String>() {{
        put("A", null);
        put("B", "12");
        put("C", "13");
        put("D", "23");
        put("E", "20");
        put("F", "21");
        put("G", "8");
        put("H", "15");
        put("I", "6");
    }};

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
        String apiCodeTransfer = checkApiCode();
        // 黑名单接口是否推送完成
        // 当前时间是否>11 点 2 者满足其一就推送
        Boolean pushBlackPhoneEnd = znkfPushService.isPushBlackPhoneEnd(apiCodeTransfer, LocalDate.now().toString());
        // 判断当天转化数据是否传输完成
        if (marketingTransferInfoMapper.countByApiCodAndLastOne(apiCodeTransfer, LocalDate.now().toString(), "1") > 0) {
            if (pushBlackPhoneEnd || LocalDateTime.now().getHour() >= 11) {
                yiXinToJueCeProcessService.doProcess(actonTypeTree, tableCreateService.getTcId(apiCodeTransfer));
            }
        } else {
            log.error("宜信转化数据没有上传完成！");
        }

    }

    private String checkApiCode() {
        String apiCodeTransfer = marketingCommonConfig.getYiXinGetTransferToJueCeApiCode();
        if (StringUtils.isBlank(apiCodeTransfer)) {
            log.error("宜信推送决策未配置apiCode");
        }
        return apiCodeTransfer;
    }
}

