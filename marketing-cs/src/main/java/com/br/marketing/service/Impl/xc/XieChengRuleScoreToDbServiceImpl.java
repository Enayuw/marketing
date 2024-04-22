package com.br.marketing.service.Impl.xc;

import com.br.marketing.entity.StraHisFile;
import com.br.marketing.entity.StraHisFileExample;
import com.br.marketing.entity.XieChengRuleScoreRecord;
import com.br.marketing.entity.XieChengRuleScoreRecordExample;
import com.br.marketing.mapper.StraHisFileMapper;
import com.br.marketing.mapper.XieChengRuleScoreRecordMapper;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

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
    @Resource
    XieChengRuleScoreRecordMapper scoreRecordMapper;
    @Override
    public void process() {
        marketingCommonConfig.getXieChengCollidingDataProcessApiCodes().forEach(apiCode -> {
            LocalDate createTimeStartLocalDate = LocalDate.now().minusDays(marketingCommonConfig.getXieChengRuleScoreToDbLastDays());
            Date createTimeStartDate = Date.from(createTimeStartLocalDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());

            StraHisFileExample straHisFileExample = new StraHisFileExample();
            straHisFileExample.createCriteria().andApiCodeEqualTo(apiCode).andStatusEqualTo(2).andCreateTimeGreaterThanOrEqualTo(createTimeStartDate).andTypeEqualTo(2);
            List<StraHisFile> straHisFiles = straHisFileMapper.selectByExample(straHisFileExample);
            straHisFiles.forEach(straHisFile -> {
                XieChengRuleScoreRecordExample scoreRecordExample = new XieChengRuleScoreRecordExample();
                scoreRecordExample.createCriteria().andIsDeleteEqualTo(0).andBatchNumberEqualTo(straHisFile.getBatchNumber());
                List<XieChengRuleScoreRecord> xieChengRuleScoreRecords = scoreRecordMapper.selectByExample(scoreRecordExample);

                if (CollectionUtils.isEmpty(xieChengRuleScoreRecords)) {
                    XieChengRuleScoreRecord scoreRecord = new XieChengRuleScoreRecord();
                    scoreRecord.setApiCode(apiCode);
                    scoreRecord.setRecordStatus(1);
                    scoreRecord.setBatchNumber(straHisFile.getBatchNumber());
                    scoreRecord.setFileName(straHisFile.getFileName());
                    scoreRecord.setCreateTime(new Date());
                    scoreRecord.setUpdateTime(new Date());

                    scoreRecordMapper.insertSelective(scoreRecord);


                }
            });
        });


    }
}
