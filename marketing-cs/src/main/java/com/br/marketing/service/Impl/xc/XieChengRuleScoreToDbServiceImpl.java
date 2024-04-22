package com.br.marketing.service.Impl.xc;

import com.br.marketing.entity.StraHisFile;
import com.br.marketing.entity.StraHisFileExample;
import com.br.marketing.mapper.StraHisFileMapper;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

/**
 * @Description XieChengRuleScoreToDbServiceImpl
 * @Author hong.chen
 * @CreateTime 2024/04/22
 */
@Service
@Slf4j
public class XieChengRuleScoreToDbServiceImpl implements XieChengRuleScoreToDbService{
    @Resource
    private MarketingCommonConfig marketingCommonConfig;
    @Resource
    StraHisFileMapper straHisFileMapper;
    @Override
    public void process() {
        marketingCommonConfig.getXieChengCollidingDataProcessApiCodes().forEach(apiCode -> {
            // XieChengRuleScoreToDbJob查询跑分记录表，条件：api_code=3710058 && stra_his_file.status=2 && stra_his_file.create_time>当前日期-3天
            LocalDate createTimeStartLocalDate = LocalDate.now().minusDays(marketingCommonConfig.getXieChengRuleScoreToDbLastDays());
            Date createTimeStartDate = Date.from(createTimeStartLocalDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());

            StraHisFileExample straHisFileExample = new StraHisFileExample();
            straHisFileExample.createCriteria().andApiCodeEqualTo(apiCode).andStatusEqualTo(2).andCreateTimeGreaterThanOrEqualTo(createTimeStartDate);
            List<StraHisFile> straHisFiles = straHisFileMapper.selectByExample(straHisFileExample);
            straHisFiles.forEach(straHisFile -> {

            });
        });


    }
}
