package com.br.marketing.check.service.Impl;

import com.br.marketing.client.RedisChgService;
import com.br.marketing.common.constants.rediskey.RedisKeyConstant;
import com.br.marketing.common.utils.Constants;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.entity.ScorePushCustomerConfig;
import com.br.marketing.entity.ScorePushCustomerConfigExample;
import com.br.marketing.entity.StraHisFile;
import com.br.marketing.enums.CallBackPushStatusEnum;
import com.br.marketing.mapper.ScorePushCustomerConfigMapper;
import com.br.marketing.mapper.StraHisFileMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Service
public class CleanScoreCustomerServiceImpl {

    @Resource
    ScorePushCustomerConfigMapper scorePushCustomerConfigMapper;

    @Resource
    StraHisFileMapper straHisFileMapper;

    @Autowired
    RedisChgService redisChgService;

    public void cleanScoreCustomerBigKey(){
        Date createTime = Date.from(LocalDate.now().minusDays(1).atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
        ScorePushCustomerConfigExample scorePushCustomerConfigExample=new ScorePushCustomerConfigExample();
        scorePushCustomerConfigExample.createCriteria().andIsDelEqualTo(Constants.DATA_VALID);
        List<ScorePushCustomerConfig> scorePushCustomerConfigs = scorePushCustomerConfigMapper.selectByExample(scorePushCustomerConfigExample);
        for (ScorePushCustomerConfig scorePushCustomerConfig : scorePushCustomerConfigs) {
            String ruleNumber = StringUtils.isNotBlank(scorePushCustomerConfig.getScoreRuleShortName())
                    ? scorePushCustomerConfig.getScoreRuleShortName()
                    : "";
            List<StraHisFile> fileByRule = straHisFileMapper.getFileByRule(createTime, ruleNumber
                    , Arrays.asList(CallBackPushStatusEnum.GETFAIL.getValue()
                            ,CallBackPushStatusEnum.SUCCESS.getValue()
                    ,CallBackPushStatusEnum.SORTFAIL.getValue()
                    ,CallBackPushStatusEnum.CALLBACKFAIL.getValue()),1);
            for (StraHisFile straHisFile : fileByRule) {
                for (int i = 0; i < 4; i++) {
                    String key = RedisKeyConstant.SCORE_TO_CUSTOMER_SORT_KEY
                            .concat(":").concat(straHisFile.getId().toString())
                            .concat(":").concat("" + i);
                    redisChgService.delBigHash(key, 3000);
                }
            }
        }
    }
}
