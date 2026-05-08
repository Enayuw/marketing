package com.br.marketing.service.Impl;

import com.br.marketing.client.RedisChgService;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.constants.rediskey.RedisKeyConstant;
import com.br.marketing.common.utils.Constants;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.entity.*;
import com.br.marketing.es.bean.QueryBaseBean;
import com.br.marketing.es.service.impl.MarketingHistoryEsServiceImpl;
import com.br.marketing.mapper.ScoreSearchConditionMapper;
import com.br.marketing.mapper.ScoreSearchConditionMappingMapper;
import com.br.marketing.mapper.StraHisFileMapper;
import com.br.marketing.service.IScoreResultService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.util.EsNewIndexRuleUtils;
import com.google.common.base.Joiner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class ScoreResultServiceImpl implements IScoreResultService {

    @Autowired
    RedisChgService redisChgService;

    @Resource
    ScoreSearchConditionMapper scoreSearchConditionMapper;

    @Resource
    ScoreSearchConditionMappingMapper scoreSearchConditionMappingMapper;

    @Resource
    StraHisFileMapper straHisFileMapper;

    @Autowired
    MarketingHistoryEsServiceImpl marketingHistoryEsService;

    @Autowired
    MarketingCommonConfig marketingCommonConfig;

    @Override
    public Result<String> isFilterScoreByTransfer(String apiCode, String ruleLabel) {
        String key = RedisKeyConstant.transferRuleCondition
                .concat(":").concat(apiCode)
                .concat(":").concat(ruleLabel);
        String s = redisChgService.get(key);
        if (StringUtils.isBlank(s)) {
            ScoreSearchConditionMappingExample mappingExample = new ScoreSearchConditionMappingExample();
            mappingExample.createCriteria()
                    .andApiCodeEqualTo(apiCode)
                    .andRuleLabelEqualTo(ruleLabel)
                    .andIsDelEqualTo(Constants.DATA_VALID);
            List<ScoreSearchConditionMapping> scoreSearchConditionMappings = scoreSearchConditionMappingMapper.selectByExample(mappingExample);
            if (scoreSearchConditionMappings.size() <= 0) {
                return new Result<>().setCode(ResultCode.FAIL.getValue());
            }
            ScoreSearchConditionMapping scoreSearchConditionMapping = scoreSearchConditionMappings.get(0);
            ScoreSearchCondition scoreSearchCondition = scoreSearchConditionMapper.selectByPrimaryKey(scoreSearchConditionMapping.getConditionId());
            if (!new Integer(1).equals(scoreSearchCondition.getIsDel())) {
                return new Result<>().setCode(ResultCode.FAIL.getValue());
            }
            String content = scoreSearchCondition.getContent();
            redisChgService.set(key, content);
            redisChgService.expire(key,60*5);
            return new Result<String>().setCode(ResultCode.SUCCESS.getValue()).setDate(content);
        }

        return new Result<>().setCode(ResultCode.SUCCESS.getValue()).setDate(s);
    }

    @Override
    public Result<String> filterScoreResByTransfer(String apiCode, String custNum, String content) {
        StraHisFileExample fileExample = new StraHisFileExample();
        fileExample.createCriteria()
                .andApiCodeEqualTo(apiCode)
                .andScoreStatusEqualTo(2);
        fileExample.setOrderByClause(" create_time desc limit 1");
        List<StraHisFile> straHisFiles = straHisFileMapper.selectByExample(fileExample);
        if (straHisFiles.size() <= 0) {
            return new Result<>().setCode(ResultCode.FAIL.getValue());
        }
        StraHisFile straHisFile = straHisFiles.get(0);
        String batchNumber = straHisFile.getBatchNumber();
        Long id = straHisFile.getId();
        String s = content.replaceAll("\\{\\{custNum\\}\\}", custNum);
        QueryBaseBean queryBaseBean = new QueryBaseBean();
        queryBaseBean.setApiCode(apiCode);
        queryBaseBean.setBatchNumbers(batchNumber);
        queryBaseBean.setFileIds(id.toString());
        queryBaseBean.setJsonData(s);
        queryBaseBean.setUseNewIndexRule(EsNewIndexRuleUtils.resolveAsMap(straHisFiles, marketingCommonConfig));
        int total = marketingHistoryEsService.builderMarketingWithTotal(queryBaseBean);
        if(total>0){
            return new Result<>().setCode(ResultCode.SUCCESS.getValue());
        }
        return new Result<>().setCode(ResultCode.FAIL.getValue());
    }
}
