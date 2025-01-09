package com.br.marketing.service.Impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.entity.*;
import com.br.marketing.enums.ScoreThreeKeyEncryptEnum;
import com.br.marketing.mapper.*;
import com.br.marketing.service.IProductResultSimpleService;
import com.br.marketing.service.MarketingTaskExtendService;
import com.br.marketing.vo.BaseHead;
import com.br.marketing.vo.BaseHeadConfigVO;
import com.br.marketing.vo.ConfigByApiCodeVO;
import com.br.marketing.vo.StrategyProductDetailVO;
import com.google.common.base.Joiner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ProductResultByConfigSimpleServiceImpl implements IProductResultSimpleService {

    @Resource
    StrategyProductConfigMapper strategyProductConfigMapper;

    @Autowired
    RedisChgService redisChgService;

    final static String redisKeyStrategyProduct = "strategyProductConfig:apiCode:batchNumber";

    final static String redisKeyConfigByApiCode = "customer:apicode:config";

    final static String redisKeyFlagScore = "flagscore:product";

    @Resource
    GroupStrategyConfigMapper groupStrategyConfigMapper;

    @Resource
    MarketingTaskExtendMapper marketingTaskExtendMapper;

    @Resource
    ProductFlagScoreMapper flagScoreMapper;

    @Resource
    MarketingCustomerMapper marketingCustomerMapper;
    @Resource
    MarketingTaskMapper marketingTaskMapper;
    @Resource
    MarketingTaskExtendService marketingTaskExtendService;

    public static List<String> flagScoreByinnerList;


    @Override
    public Result buildResult(JSONObject hxJson, StringBuilder sb,String sep, MarketingUser user,JSONObject esResult) {
        StringBuilder result=new StringBuilder();
        String strategyProductConfigStr = getStrategyProductConfigStr(user.getApiCode(),user.getBatchNumber());
        if(StringUtils.isEmpty(strategyProductConfigStr)){
            return new Result().setCode(ResultCode.FAIL.getValue());
        }
        List<StrategyProductDetailVO> strategyProductDetailVOs = JSON.parseObject(strategyProductConfigStr
                , new TypeReference<List<StrategyProductDetailVO>>() {
        }.getType());
        StrategyProductDetailVO strategyProductDetailVO = null;
        if(strategyProductDetailVOs.size()>0){
            strategyProductDetailVO=strategyProductDetailVOs.get(0);
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
    public Result<String> getCurrentBaseHeadInfoByTaskId(Long taskId,String sep) {
        MarketingTaskExtendExample taskExtendExample = new MarketingTaskExtendExample();
        taskExtendExample.createCriteria().andTaskIdEqualTo(taskId).andIsDelEqualTo(1);
        List<MarketingTaskExtend> marketingTaskExtends = marketingTaskExtendMapper.selectByExample(taskExtendExample);
        if(marketingTaskExtends.size()>0){
            MarketingTaskExtend taskExtend = marketingTaskExtends.get(0);
            if(StringUtils.isNotBlank(taskExtend.getExtendShowTitle())){
                BaseHeadConfigVO o = JSON.parseObject(taskExtend.getExtendShowTitle(), new TypeReference<BaseHeadConfigVO>() {
                }.getType());
                return new Result<>().setCode(ResultCode.SUCCESS.getValue()).setDate(Joiner.on(sep).join(o.getShowBaseHead()));
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
    public Result<String> getFieldsStrInfo(String apiCode,String batchNumber,String sep) {
        Result<List<String>> fieldsInfo = this.getFieldsInfo(apiCode,batchNumber);
        if(ResultCode.SUCCESS.getValue().equals(fieldsInfo.getCode())){
            if(CollectionUtils.isEmpty(fieldsInfo.getData())){
                return new Result<String>().setCode(ResultCode.FAIL.getValue());
            }
            return new Result<String>().setCode(fieldsInfo.getCode())
                    .setDate(Joiner.on(sep).join(fieldsInfo.getData()));
        }else{
            return new Result<String>().setCode(fieldsInfo.getCode())
                    .setMessage(fieldsInfo.getMessage());
        }
    }

    @Override
    public Result<List<String>> getFieldsInfo(String apiCode,String batchNumber) {
        String strategyProductConfigStr = this.getStrategyProductConfigStr(apiCode,batchNumber);
        if(StringUtils.isNotBlank(strategyProductConfigStr)){
            StrategyProductDetailVO strategyProductDetailVO = JSON.parseObject(strategyProductConfigStr
                    , new TypeReference<StrategyProductDetailVO>() {
                    }.getType());
            if(strategyProductDetailVO!=null){
                return new Result<>().setCode(ResultCode.SUCCESS.getValue()).setDate(strategyProductDetailVO.getFields());
            }else{
               return new Result<>().setCode(ResultCode.FAIL.getValue());

            }
        }
        return new Result<>().setCode(ResultCode.FAIL.getValue());
    }

    @Override
    public String getStrategyProductConfigStr(String apiCode,String batchNumber){
       MarketingTask task = marketingTaskMapper.queryBlt(batchNumber);
        if(task !=null){
            MarketingTaskExtend marketingTaskExtend = marketingTaskExtendService.getMarketingTaskExtend(task.getId());
            if(marketingTaskExtend !=null&&StringUtils.isNotBlank(marketingTaskExtend.getStrategyProductJson())) {
                return marketingTaskExtend.getStrategyProductJson();
            }
        }
       return "";
    }

    @Override
    public Result<List<String>> getFlagProduct() {
        String s = redisChgService.get(redisKeyFlagScore);
        if(StringUtils.isNotBlank(s)){
            return new Result<List<String>>().setCode(ResultCode.SUCCESS.getValue())
                    .setDate(new ArrayList<>(Arrays.asList(s.split(","))));
        }
        ProductFlagScoreExample flagScoreExample = new ProductFlagScoreExample();
        flagScoreExample.createCriteria().andIsDelEqualTo(1);
        List<ProductFlagScore> productFlagScores = flagScoreMapper.selectByExample(flagScoreExample);
        if(productFlagScores.size()<=0){
            return new Result<List<String>>().setCode(ResultCode.FAIL.getValue());
        }else{
            flagScoreByinnerList = new ArrayList<>(Arrays.asList(productFlagScores.get(0).getFlagScoreProduct().split(",")));
            redisChgService.set(redisKeyFlagScore,productFlagScores.get(0).getFlagScoreProduct());
            return new Result<>().setCode(ResultCode.SUCCESS.getValue())
                    .setDate(flagScoreByinnerList);
        }
    }

    @Override
    public Result<String> getFlagProductStr() {
        ProductFlagScoreExample flagScoreExample = new ProductFlagScoreExample();
        flagScoreExample.createCriteria().andIsDelEqualTo(1);
        List<ProductFlagScore> productFlagScores = flagScoreMapper.selectByExample(flagScoreExample);
        if(productFlagScores.size()<=0){
            return new Result<String>().setCode(ResultCode.SUCCESS.getValue()).setDate("");
        }else{
            return new Result<>().setCode(ResultCode.SUCCESS.getValue())
                    .setDate(productFlagScores.get(0).getFlagScoreProduct());
        }
    }

    @Override
    public Result<String> updateFlagProduct(String productStr) {
        ProductFlagScoreExample flagScoreExample = new ProductFlagScoreExample();
        flagScoreExample.createCriteria().andIsDelEqualTo(1);
        List<ProductFlagScore> productFlagScores = flagScoreMapper.selectByExample(flagScoreExample);
        if(productFlagScores.size()<=0){
            ProductFlagScore productFlagScore = new ProductFlagScore();
            productFlagScore.setFlagScoreProduct(productStr);
            flagScoreMapper.insertSelective(productFlagScore);
        }else{
            ProductFlagScore productFlagScore = productFlagScores.get(0);
            productFlagScore.setFlagScoreProduct(productStr);
            flagScoreMapper.updateByPrimaryKeySelective(productFlagScore);
        }
        if(redisChgService.exists(redisKeyFlagScore)){
            redisChgService.del(redisKeyFlagScore);
        }
        return new Result<>().setCode(ResultCode.SUCCESS.getValue());
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

    @Override
    public void offLineHeadComplete(List<String> showHeads, List<BaseHead> heads) {
        if (!showHeads.contains("name")) {
            showHeads.add(0, "name");
            if(heads !=null){
                heads.add(0,new BaseHead().setName("name").setType(1).setThreekEncryptType(ScoreThreeKeyEncryptEnum.init.getValue()));
            }
        }
        if (!showHeads.contains("id") && !showHeads.contains("idcard")) {
            showHeads.add(0, "id");
            if(heads !=null) {
                heads.add(0,new BaseHead().setName("id").setType(1).setThreekEncryptType(ScoreThreeKeyEncryptEnum.init.getValue()));
            }
        }
        if (!showHeads.contains("cell")) {
            showHeads.add(0, "cell");
            if(heads !=null) {
                heads.add(0,new BaseHead().setName("cell").setType(1).setThreekEncryptType(ScoreThreeKeyEncryptEnum.init.getValue()));
            }
        }
    }

    @Override
    public void initHead(StringBuilder head, String sep, MarketingTask task) {

        boolean isOffLine = new Integer(2).equals(task.getIsOnline());

        Result<String> baseHeadInfoByTaskId = getCurrentBaseHeadInfoByTaskId(Long.valueOf(task.getId().toString()),sep);
        String baseHeadInfo = "";
        if(ResultCode.SUCCESS.getValue().equals(baseHeadInfoByTaskId.getCode())){
            baseHeadInfo = baseHeadInfoByTaskId.getData();
        }
        if(isOffLine){
            List<String> heads = Arrays.stream(baseHeadInfo.split(sep)).collect(Collectors.toList());
            offLineHeadComplete(heads,null);
            baseHeadInfo = Joiner.on(sep).join(heads);
        }

        head.append("request_time").append(sep).append("batch_number").append(sep).append("cus_num")
                .append(sep).append("strategy_id").append(sep).append("version").append(sep);
        if(StringUtils.isNotBlank(baseHeadInfo.trim())){
            head.append(baseHeadInfo).append(sep);
        }
        String dataInfo = "";
        Integer taskType=task.getTaskType();
        if((taskType.compareTo(new Integer(0))==0 ||taskType.compareTo(new Integer(2))==0)
                && !isOffLine){
            Result<String> fieldsInfo = getFieldsStrInfo(task.getApiCode(), task.getBatchNumber(), sep);
            if(ResultCode.SUCCESS.getValue().equals(fieldsInfo.getCode())){
                dataInfo = fieldsInfo.getData();
            }
        }
        if(StringUtils.isNotBlank(dataInfo)){
            head.append(dataInfo).append(sep);
        }
    }
}
