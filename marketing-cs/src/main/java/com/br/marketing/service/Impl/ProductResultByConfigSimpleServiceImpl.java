package com.br.marketing.service.Impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.entity.*;
import com.br.marketing.mapper.GroupStrategyConfigMapper;
import com.br.marketing.mapper.MarketingTaskExtendMapper;
import com.br.marketing.mapper.MarketingTaskMapper;
import com.br.marketing.mapper.StrategyProductConfigMapper;
import com.br.marketing.service.IProductResultSimpleService;
import com.br.marketing.vo.StrategyProductDetailVO;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ProductResultByConfigSimpleServiceImpl implements IProductResultSimpleService {

    @Autowired
    StrategyProductConfigMapper strategyProductConfigMapper;

    @Autowired
    RedisChgService redisChgService;

    final static String redisKeyStrategyProduct = "strategyProductConfig:apiCode";

    @Autowired
    GroupStrategyConfigMapper groupStrategyConfigMapper;

    @Autowired
    MarketingTaskExtendMapper marketingTaskExtendMapper;

    @Override
    public Result buildResult(JSONObject hxJson, Set<String> products, StringBuilder sb, Map<String, String> proFieldMap
            , String sep, String apiCode,String strategyId) {
        StringBuilder result=new StringBuilder();
        String strategyProductConfigStr = getStrategyProductConfigStr(apiCode);
        if(StringUtils.isEmpty(strategyProductConfigStr)){
            return new Result().setCode(ResultCode.FAIL.getValue());
        }
        List<StrategyProductDetailVO> strategyProductDetailVOs = JSON.parseObject(strategyProductConfigStr
                , new TypeReference<List<StrategyProductDetailVO>>() {
        }.getType());
        StrategyProductDetailVO strategyProductDetailVO = null;
        if(strategyProductDetailVOs.size()>1){
            Optional<StrategyProductDetailVO> first = strategyProductDetailVOs.stream()
                    .filter(t -> t.getStrategy_id().equals(strategyId))
                    .findFirst();
            if(first.isPresent()){
                strategyProductDetailVO = first.get();
            }
        }else{
            strategyProductDetailVO = strategyProductDetailVOs.get(0);
        }

        if(strategyProductDetailVO == null){
            return new Result().setCode(ResultCode.FAIL.getValue());
        }
        for (int i = 0; i < strategyProductDetailVO.getFields().size(); i++) {
            String field = strategyProductDetailVO.getFields().get(i);
            String fieldRes = hxJson.getString(field);
            result.append(StringUtils.isNotBlank(fieldRes)?fieldRes:"").append(sep);
        }
        sb.append(result);
        return new Result().setCode(ResultCode.SUCCESS.getValue());
    }

    @Override
    public Result<String> getBaseHeadInfo(String apiCode, String groupType) {
        GroupStrategyConfigExample groupStrategyConfigExample = new GroupStrategyConfigExample();
        groupStrategyConfigExample.createCriteria().andApiCodeEqualTo(apiCode)
                .andGroupTypeEqualTo(groupType).andIsDelEqualTo(1);
        List<GroupStrategyConfig> groupStrategyConfigs = groupStrategyConfigMapper
                .selectByExample(groupStrategyConfigExample);
        if(groupStrategyConfigs.size()>0){
            GroupStrategyConfig groupStrategyConfig = groupStrategyConfigs.get(0);
            return new Result<>().setCode(ResultCode.SUCCESS.getValue())
                    .setDate(groupStrategyConfig.getBaseInfo());
        }

        StrategyProductConfigExample strategyProductConfigExample = new StrategyProductConfigExample();
        strategyProductConfigExample.createCriteria().andApiCodeEqualTo(apiCode)
                .andIsDelEqualTo(1);
        List<StrategyProductConfig> strategyProductConfigs = strategyProductConfigMapper
                .selectByExample(strategyProductConfigExample);
        if(strategyProductConfigs.size()>0){
            StrategyProductConfig strategyProductConfig = strategyProductConfigs.get(0);
            return new Result<>().setCode(ResultCode.SUCCESS.getValue())
                    .setDate(strategyProductConfig.getBaseInfo());
        }

        return new Result<>().setCode(ResultCode.FAIL.getValue());
    }

    @Override
    public Result<String> getBaseHeadInfoByTaskId(Long taskId) {
        MarketingTaskExtendExample taskExtendExample = new MarketingTaskExtendExample();
        taskExtendExample.createCriteria().andTaskIdEqualTo(taskId).andIsDelEqualTo(1);
        List<MarketingTaskExtend> marketingTaskExtends = marketingTaskExtendMapper.selectByExample(taskExtendExample);
        if(marketingTaskExtends.size()<=0){
            return new Result<>().setCode(ResultCode.FAIL.getValue());
        }
        MarketingTaskExtend taskExtend = marketingTaskExtends.get(0);
        Result<String> baseHeadInfo = this.getBaseHeadInfo(taskExtend.getApiCode(), taskExtend.getGroupType());
        if(ResultCode.SUCCESS.getValue().equals(baseHeadInfo.getCode())){
            return new Result<String>().setCode(ResultCode.SUCCESS.getValue()).setDate(baseHeadInfo.getData());
        }
        return new Result<>().setCode(ResultCode.FAIL.getValue());
    }

    @Override
    public Result<String> getFieldsStrInfo(String apiCode, String strategyId) {
        Result<List<String>> fieldsInfo = this.getFieldsInfo(apiCode, strategyId);
        if(ResultCode.SUCCESS.getValue().equals(fieldsInfo.getCode())){
            return new Result<String>().setCode(fieldsInfo.getCode())
                    .setDate(Joiner.on(",").join(fieldsInfo.getData()));
        }else{
            return new Result<String>().setCode(fieldsInfo.getCode())
                    .setMessage(fieldsInfo.getMessage());
        }
    }

    @Override
    public Result<List<String>> getFieldsInfo(String apiCode, String strategyId) {
        String strategyProductConfigStr = this.getStrategyProductConfigStr(apiCode);
        if(StringUtils.isNotBlank(strategyProductConfigStr)){
            List<StrategyProductDetailVO> strategyProductDetailVOs = JSON.parseObject(strategyProductConfigStr
                    , new TypeReference<List<StrategyProductDetailVO>>() {
                    }.getType());
            if(strategyProductDetailVOs.size()==1){
                StrategyProductDetailVO strategyProductDetailVO = strategyProductDetailVOs.get(0);
                return new Result<>()
                        .setCode(ResultCode.SUCCESS.getValue())
                        .setDate(strategyProductDetailVO.getFields());
            }else{
                Optional<StrategyProductDetailVO> first =
                        strategyProductDetailVOs.stream()
                                .filter(t -> strategyId.equals(t.getStrategy_id())).findFirst();
                if(first.isPresent()){
                    return new Result<>().setCode(ResultCode.SUCCESS.getValue())
                            .setDate(first.get().getFields());
                }else{
                    return new Result<>().setCode(ResultCode.FAIL.getValue());
                }
            }
        }
        return new Result<>().setCode(ResultCode.FAIL.getValue());
    }

    String getStrategyProductConfigStr(String apiCode){
        String key = redisKeyStrategyProduct.concat(":").concat(apiCode);
        String s = redisChgService.get(key);
        if(StringUtils.isNotBlank(s)){
            return s;
        }
        StrategyProductConfigExample productConfigExample= new StrategyProductConfigExample();
        productConfigExample.createCriteria().andApiCodeEqualTo(apiCode).andIsDelEqualTo(1);
        List<StrategyProductConfig> strategyProductConfigs = strategyProductConfigMapper.selectByExample(productConfigExample);
        if(strategyProductConfigs.size()<=0){
            return "";
        }
        StrategyProductConfig strategyProductConfig = strategyProductConfigs.get(0);
        redisChgService.set(key,strategyProductConfig.getStrategyProductJson());
        redisChgService.expire(key,60*60*24);
        return strategyProductConfig.getStrategyProductJson();
    }
}
