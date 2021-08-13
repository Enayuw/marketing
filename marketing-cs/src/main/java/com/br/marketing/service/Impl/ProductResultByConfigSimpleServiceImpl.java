package com.br.marketing.service.Impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.entity.*;
import com.br.marketing.mapper.*;
import com.br.marketing.service.IProductResultSimpleService;
import com.br.marketing.vo.BaseHead;
import com.br.marketing.vo.BaseHeadConfigVO;
import com.br.marketing.vo.ConfigByApiCodeVO;
import com.br.marketing.vo.StrategyProductDetailVO;
import com.google.common.base.Joiner;
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

    final static String redisKeyConfigByApiCode = "customer:apicode:config";

    @Autowired
    GroupStrategyConfigMapper groupStrategyConfigMapper;

    @Autowired
    MarketingTaskExtendMapper marketingTaskExtendMapper;

    @Autowired
    ProductFlagScoreMapper flagScoreMapper;

    @Autowired
    MarketingCustomerMapper marketingCustomerMapper;

    public static List<String> flagScoreByinnerList;

    @Override
    public Result buildResult(JSONObject hxJson, Set<String> products, StringBuilder sb, Map<String, String> proFieldMap
            , String sep, String apiCode,String strategyId,JSONObject esResult) {
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
                    .filter(t -> t.getStrategyId().equals(strategyId))
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
            esResult.put(field,StringUtils.isNotBlank(fieldRes)?fieldRes:"");
        }
        sb.append(result);
        return new Result().setCode(ResultCode.SUCCESS.getValue());
    }

    @Override
    public BaseHeadConfigVO getOrderBaseHeadInfo(BaseHeadConfigVO vo) {
        List<BaseHead> baseHead = vo.getBaseHead();
        List<String> showBaseHead = vo.getShowBaseHead();
        List<BaseHead> orderHeads = new ArrayList<>();
        showBaseHead.forEach(t->{
            Optional<BaseHead> head = baseHead.stream().filter(k -> k.getName().equals(t)).findFirst();
            if(!head.isPresent()){
                BaseHead h = new BaseHead();
                h.setName(t);
                h.setType(0);
                orderHeads.add(h);
            }else{
                orderHeads.add(head.get());
            }
        });
        BaseHeadConfigVO oo = new BaseHeadConfigVO();
        oo.setBaseHead(orderHeads);
        oo.setShowBaseHead(showBaseHead);
        return oo;
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
    public Result<String> getCurrentBaseHeadInfoByTaskId(Long taskId) {
        MarketingTaskExtendExample taskExtendExample = new MarketingTaskExtendExample();
        taskExtendExample.createCriteria().andTaskIdEqualTo(taskId).andIsDelEqualTo(1);
        List<MarketingTaskExtend> marketingTaskExtends = marketingTaskExtendMapper.selectByExample(taskExtendExample);
        if(marketingTaskExtends.size()>0){
            MarketingTaskExtend taskExtend = marketingTaskExtends.get(0);
            if(StringUtils.isNotBlank(taskExtend.getExtendShowTitle())){
                return new Result<>().setCode(ResultCode.SUCCESS.getValue()).setDate(taskExtend.getExtendShowTitle());
            }
            return new Result<>().setCode(ResultCode.FAIL.getValue());
        }
        return new Result<>().setCode(ResultCode.FAIL.getValue());
    }

    @Override
    public Result<String> getBaseHeadInfo(String apiCode, String groupType) {
        Result<BaseHeadConfigVO> baseHeadConfig = this.getBaseHeadConfig(apiCode, groupType);
        if(ResultCode.SUCCESS.getValue().equals(baseHeadConfig.getCode())){
            return new Result<String>()
                    .setCode(ResultCode.SUCCESS.getValue())
                    .setDate(Joiner.on(",").join(baseHeadConfig.getData().getShowBaseHead()));
        }
        return new Result<String>()
                .setCode(ResultCode.FAIL.getValue())
                .setMessage(baseHeadConfig.getMessage());
    }

    @Override
    public Result<BaseHeadConfigVO> getBaseHeadConfig(String apiCode, String groupType) {
        try {
            GroupStrategyConfigExample groupStrategyConfigExample = new GroupStrategyConfigExample();
            groupStrategyConfigExample.createCriteria().andApiCodeEqualTo(apiCode)
                    .andGroupTypeEqualTo(groupType).andIsDelEqualTo(1);
            List<GroupStrategyConfig> groupStrategyConfigs = groupStrategyConfigMapper
                    .selectByExample(groupStrategyConfigExample);
            if (groupStrategyConfigs.size() > 0) {
                GroupStrategyConfig groupStrategyConfig = groupStrategyConfigs.get(0);
                if(StringUtils.isBlank(groupStrategyConfig.getBaseInfo())){
                    return new Result<BaseHeadConfigVO>()
                            .setCode(ResultCode.FAIL.getValue())
                            .setMessage("没有配置信息");
                }
                BaseHeadConfigVO configVO = JSON.parseObject(groupStrategyConfig.getBaseInfo()
                        , new TypeReference<BaseHeadConfigVO>() {
                        }.getType());
                return new Result<BaseHeadConfigVO>()
                        .setCode(ResultCode.SUCCESS.getValue())
                        .setDate(configVO);
            }
            StrategyProductConfigExample strategyProductConfigExample = new StrategyProductConfigExample();
            strategyProductConfigExample.createCriteria().andApiCodeEqualTo(apiCode)
                    .andIsDelEqualTo(1);
            List<StrategyProductConfig> strategyProductConfigs = strategyProductConfigMapper
                    .selectByExample(strategyProductConfigExample);
            if (strategyProductConfigs.size() > 0) {
                StrategyProductConfig strategyProductConfig = strategyProductConfigs.get(0);
                if(StringUtils.isBlank(strategyProductConfig.getBaseInfo())){
                    return new Result<BaseHeadConfigVO>()
                            .setCode(ResultCode.FAIL.getValue())
                            .setMessage("没有配置信息");
                }
                BaseHeadConfigVO configVO = JSON.parseObject(strategyProductConfig.getBaseInfo()
                        , new TypeReference<BaseHeadConfigVO>() {
                        }.getType());
                return new Result<BaseHeadConfigVO>()
                        .setCode(ResultCode.SUCCESS.getValue())
                        .setDate(configVO);
            }
        }catch (Exception ex){
            return new Result<BaseHeadConfigVO>().setCode(ResultCode.FAIL.getValue()).setMessage(ex.getMessage());
        }
        return new Result<BaseHeadConfigVO>().setCode(ResultCode.FAIL.getValue());
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
                                .filter(t -> strategyId.equals(t.getStrategyId())).findFirst();
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

    @Override
    public Result<List<String>> getFlagProduct() {
        if(flagScoreByinnerList!=null&&flagScoreByinnerList.size()>0){
            return new Result<List<String>>().setCode(ResultCode.SUCCESS.getValue()).setDate(flagScoreByinnerList);
        }
        ProductFlagScoreExample flagScoreExample = new ProductFlagScoreExample();
        flagScoreExample.createCriteria().andIsDelEqualTo(1);
        List<ProductFlagScore> productFlagScores = flagScoreMapper.selectByExample(flagScoreExample);
        if(productFlagScores.size()<=0){
            return new Result<List<String>>().setCode(ResultCode.FAIL.getValue());
        }else{
            flagScoreByinnerList = new ArrayList<>(Arrays.asList(productFlagScores.get(0).getFlagScoreProduct().split(",")));
            return new Result<>().setCode(ResultCode.SUCCESS.getValue())
                    .setDate(flagScoreByinnerList);
        }
    }

    @Override
    public Result<ConfigByApiCodeVO> getConfigByApiCode(String apiCode) {
        String key = redisKeyConfigByApiCode.concat(":").concat(apiCode);
        String s = redisChgService.get(key);
        if(StringUtils.isNotBlank(s)){
            ConfigByApiCodeVO o = JSON.parseObject(s, new TypeReference<ConfigByApiCodeVO>() {
            }.getType());
            return new Result<>().setCode(ResultCode.SUCCESS.getValue()).setDate(o);
        }else{
            MarketingCustomerExample customerExample = new MarketingCustomerExample();
            customerExample.createCriteria()
                    .andApiCodeEqualTo(apiCode);
            List<MarketingCustomer> marketingCustomers = marketingCustomerMapper.selectByExample(customerExample);
            if(marketingCustomers.size()>0){
                MarketingCustomer marketingCustomer = marketingCustomers.get(0);
                if(StringUtils.isNotBlank(marketingCustomer.getExtendConfigInfo())) {
                    ConfigByApiCodeVO o = JSON.parseObject(marketingCustomer.getExtendConfigInfo(), new TypeReference<ConfigByApiCodeVO>() {
                    }.getType());
                    redisChgService.setex(key, marketingCustomer.getExtendConfigInfo(), 60 * 60);
                    return new Result<>().setCode(ResultCode.SUCCESS.getValue()).setDate(o);
                }else{
                    return new Result<>().setCode(ResultCode.FAIL.getValue());
                }
            }
        }
        return new Result<>().setCode(ResultCode.FAIL.getValue());
    }
}
