package com.br.marketing.service.rulecenter.impl;

import java.util.*;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.exception.KnowException;
import com.br.marketing.common.utils.Constants;
import com.br.marketing.dto.rulecenter.XieChengCollidingFilterDTO;
import com.br.marketing.entity.*;
import com.br.marketing.mapper.CustomerInfoPushBatchMapper;
import com.br.marketing.mapper.CustomerInfoPushMainMapper;
import com.br.marketing.mapper.ScoreSearchConditionMapper;
import com.br.marketing.mapper.StraHisFileMapper;
import com.br.marketing.service.rulecenter.IRuleCenterFilterTemplateService;
import com.br.marketing.service.rulecenter.enums.RuleCenterDataSourceEnum;
import com.br.marketing.service.rulecenter.enums.RuleCenterPushTargetEnum;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.util.xiecheng.XieChengEsJsonHandler;
import com.google.common.base.Joiner;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ScoreFilterTimplateServiceImpl implements IRuleCenterFilterTemplateService {

    @Resource
    StraHisFileMapper straHisFileMapper;

    @Resource
    CustomerInfoPushBatchMapper customerInfoPushBatchMapper;

    @Resource
    CustomerInfoPushMainMapper customerInfoPushMainMapper;

    @Resource
    ScoreXieChengServiceImpl scoreXieChengService;
    @Autowired
    private MarketingCommonConfig marketingCommonConfig;

    @Override
    public String getSource(List<String> sources) {
        if (sources.size() <= 0) {
            return "";
        }
        String fileIds = sources.stream().collect(Collectors.joining(","));
        return fileIds;
    }

    @Override
    public Result autoBuildSource(CustomerInfoPushMain main, ScoreSearchCondition scoreSearchCondition) {
        String prefix = "筛选模板";
        try {
            String[] fileIdStrs = scoreSearchCondition.getSourceCondition().split(",");
            if (fileIdStrs.length > 0) {
                List<Long> fileIds = Arrays.asList(fileIdStrs).stream().map(t -> Long.valueOf(t)).collect(Collectors.toList());
                StraHisFileExample fileExample = new StraHisFileExample();
                fileExample.createCriteria().andIdIn(fileIds);
                List<StraHisFile> straHisFiles = straHisFileMapper.selectByExample(fileExample);
                if (straHisFiles.size() <= 0) {
                    return new Result()
                            .failure()
                            .setMessage(String.format(prefix.concat(",获取不到跑分记录【%s】")
                                    , scoreSearchCondition.getId()));
                }
                StringBuilder batchNumberStr = new StringBuilder();
                ArrayList<String> batchList = new ArrayList<>();
                for (StraHisFile straHisFile : straHisFiles) {
                    batchNumberStr.append(straHisFile.getBatchNumber() + ",");
                    CustomerInfoPushBatch customerInfoPushBatch = new CustomerInfoPushBatch();
                    customerInfoPushBatch.setmApiCode(straHisFile.getApiCode());
                    customerInfoPushBatch.setmBatchNumber(straHisFile.getBatchNumber());
                    customerInfoPushBatch.setmFileId(straHisFile.getId());
                    customerInfoPushBatch.setIsDel(Constants.DATA_VALID);
                    customerInfoPushBatch.setCreateTime(main.getCreateTime());
                    customerInfoPushBatch.setUpdateTime(main.getCreateTime());
                    customerInfoPushBatch.setmId(main.getId());
                    customerInfoPushBatchMapper.insertSelective(customerInfoPushBatch);
                    batchList.add(straHisFile.getBatchNumber());
                }
                String batchs = batchNumberStr.toString().substring(0, batchNumberStr.toString().length() - 1);
                main.setmCusBatchNumberList(batchs);
                if (scoreXieChengService.isXieCheng(main.getmApiCode(), scoreSearchCondition.getContent())) {
                    JSONObject jsonObject = JSON.parseObject(scoreSearchCondition.getContent());
                    XieChengCollidingFilterDTO collidingFilterDTO = new XieChengCollidingFilterDTO();
                    XieChengEsJsonHandler.handlerJson(jsonObject, collidingFilterDTO);
                    main.setFilterType(1);
                    main.setExtend(scoreXieChengService.cycleDataQuery(jsonObject, batchList, collidingFilterDTO));
                }
                customerInfoPushMainMapper.updateByPrimaryKeySelective(main);
                return new Result().success();
            } else {
                return new Result()
                        .failure()
                        .setMessage(String.format(prefix.concat(",绑定的数据源有问题【%s】")
                                , scoreSearchCondition.getId()));
            }
        } catch (Exception ex) {
            log.error(ex.getMessage(),ex);
            return new Result()
                    .failure()
                    .setMessage(String.format(prefix.concat(",未知异常【%s】")
                            , ex.getMessage()));
        }
    }


    @Override
    public RuleCenterDataSourceEnum sourceLabel() {
        return RuleCenterDataSourceEnum.SCORE;
    }
}
