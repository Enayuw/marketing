package com.br.marketing.service.rulecenter.impl;

import java.util.Date;

import com.alibaba.fastjson.JSONObject;
import com.br.marketing.common.exception.KnowException;
import com.br.marketing.entity.*;
import com.br.marketing.mapper.CustomerInfoPushBatchMapper;
import com.br.marketing.mapper.CustomerInfoPushMainMapper;
import com.br.marketing.mapper.ScoreSearchConditionMapper;
import com.br.marketing.mapper.StraHisFileMapper;
import com.br.marketing.service.rulecenter.IRuleCenterFilterTemplateService;
import com.br.marketing.service.rulecenter.enums.RuleCenterDataSourceEnum;
import com.google.common.base.Joiner;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Collectors;

@Service
public class ScoreFilterTimplateServiceImpl implements IRuleCenterFilterTemplateService {

    @Resource
    StraHisFileMapper straHisFileMapper;

    @Resource
    CustomerInfoPushBatchMapper customerInfoPushBatchMapper;

    @Resource
    CustomerInfoPushMainMapper customerInfoPushMainMapper;

    @Override
    public String getSource(List<String> sources) {
        if (sources.size() <= 0) {
            return "";
        }
        String fileIds = sources.stream().collect(Collectors.joining(","));
        return fileIds;
    }

    @Override
    public void autoBuildSource(CustomerInfoPushMain main, ScoreSearchCondition scoreSearchCondition) {
        String[] fileIdStrs = scoreSearchCondition.getSourceCondition().split(",");
        if (fileIdStrs.length > 0) {
            List<Long> fileIds = Arrays.asList(fileIdStrs).stream().map(t -> Long.valueOf(t)).collect(Collectors.toList());
            StraHisFileExample fileExample = new StraHisFileExample();
            fileExample.createCriteria().andIdIn(fileIds);
            List<StraHisFile> straHisFiles = straHisFileMapper.selectByExample(fileExample);
            StringBuilder batchNumberStr = new StringBuilder();
            for (StraHisFile straHisFile : straHisFiles) {
                batchNumberStr.append(straHisFile.getBatchNumber() + ",");
                CustomerInfoPushBatch customerInfoPushBatch = new CustomerInfoPushBatch();
                customerInfoPushBatch.setmApiCode(straHisFile.getApiCode());
                customerInfoPushBatch.setmBatchNumber(straHisFile.getBatchNumber());
                customerInfoPushBatch.setmFileId(straHisFile.getId());
                customerInfoPushBatch.setIsDel(0);
                customerInfoPushBatch.setCreateTime(main.getCreateTime());
                customerInfoPushBatch.setUpdateTime(main.getCreateTime());
                customerInfoPushBatch.setmId(main.getId());
                customerInfoPushBatchMapper.insertSelective(customerInfoPushBatch);
            }
            String batchs = batchNumberStr.toString().substring(0, batchNumberStr.toString().length() - 1);
            main.setmCusBatchNumberList(batchs);
            customerInfoPushMainMapper.updateByPrimaryKeySelective(main);
            return;
        }
        throw new KnowException("没有获取数据源");
    }

    @Override
    public RuleCenterDataSourceEnum sourceLabel() {
        return RuleCenterDataSourceEnum.SCORE;
    }
}
