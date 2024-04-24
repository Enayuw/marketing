package com.br.marketing.service.Impl;

import com.alibaba.fastjson.JSONObject;
import com.br.marketing.common.enums.TaskTypeEnum;
import com.br.marketing.entity.MarketingTaskExtend;
import com.br.marketing.entity.MarketingTaskExtendExample;
import com.br.marketing.entity.StraHisFile;
import com.br.marketing.entity.StraHisFileExample;
import com.br.marketing.mapper.MarketingTaskExtendMapper;
import com.br.marketing.mapper.StraHisFileMapper;
import com.br.marketing.service.MarketingTaskExtendService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.vo.BaseHead;
import com.br.marketing.vo.BaseHeadConfigVO;
import com.br.marketing.vo.StrategyProductDetailVO;
import com.br.marketing.vo.TaskInfoVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;


@Service
@Slf4j
public class MarketingTaskExtendServiceImpl implements MarketingTaskExtendService {

    @Resource
    MarketingTaskExtendMapper marketingTaskExtendMapper;

    @Resource
    StraHisFileMapper straHisFileMapper;

    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Override
    public MarketingTaskExtend getMarketingTaskExtend(Long taskId) {
        MarketingTaskExtendExample extendExample = new MarketingTaskExtendExample();
        extendExample.createCriteria().andIsDelEqualTo(Integer.valueOf(1)).andTaskIdEqualTo(taskId);
        List<MarketingTaskExtend> extendList = marketingTaskExtendMapper.selectByExample(extendExample);
        if(extendList.size()>0){
            return extendList.get(0);
        }
        return null;
    }

    @Override
    public Map getProducts(String ids) {
        Map map = new HashMap();
        Set<String> baseHeadList = new HashSet<>();
        Set<String> fieldsList = new HashSet<>();
        //region 数据准备
        List<Long> fileIds = Arrays.stream(ids.split(",")).map(t->Long.valueOf(t)).collect(Collectors.toList());
        StraHisFileExample straHisFileExample = new StraHisFileExample();
        straHisFileExample.createCriteria().andIdIn(fileIds);
        List<StraHisFile> straHisFiles = straHisFileMapper.selectByExample(straHisFileExample);
        List<String> batchNumbers = straHisFiles.stream().map(t -> t.getBatchNumber()).collect(Collectors.toList());
        Assert.notEmpty(batchNumbers,"没有匹配到批次号");
        List<String> apiCodes = straHisFiles.stream().map(t -> t.getApiCode()).collect(Collectors.toList());
        List<String>xieChengApiCodes= marketingCommonConfig.getXieChengCollidingDataProcessApiCodes();
        xieChengApiCodes.retainAll(apiCodes);
        //添加携程撞库基础字段
        if (!CollectionUtils.isEmpty(xieChengApiCodes)) {
            baseHeadList.add("result");
            baseHeadList.add("release_time");
            baseHeadList.add("clean_time");
        }
        List<TaskInfoVO> products = marketingTaskExtendMapper.getProducts(batchNumbers);
        //endregion

        boolean isScore = products.stream().anyMatch(t ->
                TaskTypeEnum.STRATYGYDATA.getValue().equals(t.getTaskType())
                        ||TaskTypeEnum.PRODUCTDATA.getValue().equals(t.getTaskType()));

        if(isScore){
            baseHeadList.add("request_time");
            baseHeadList.add("strategy_id");
            baseHeadList.add("cus_num");
        }

        for (TaskInfoVO product : products) {
            String extendShowTitle = product.getExtendShowTitle();
            String strategyProductJson = product.getStrategyProductJson();
            if(strategyProductJson!=null && !"".equals(strategyProductJson)){
                StrategyProductDetailVO strategyProductDetailVO= JSONObject.parseObject(strategyProductJson,StrategyProductDetailVO.class);
                List<String> fields = strategyProductDetailVO.getFields();
                if(fields!=null && fields.size()>0){
                    fieldsList.addAll(fields);
                }
            }
            if(extendShowTitle!=null && !"".equals(extendShowTitle)){
                BaseHeadConfigVO baseHeadConfigVO= JSONObject.parseObject(extendShowTitle,BaseHeadConfigVO.class);
                List<BaseHead> baseHead = baseHeadConfigVO.getBaseHead();
                if(baseHead!=null && baseHead.size()>0){
                    for(BaseHead single : baseHead){
                        String convert = ifConvert(single.getName());
                        baseHeadList.add(convert);
                    }
                }
            }
        }
        map.put("showBaseHead",baseHeadList);
        map.put("fields",fieldsList);
        return map;
    }

    public String ifConvert(String s){
        String lowerCase = s.toLowerCase();
        //usertype->user_type,idcard->id_card,strategyId->strategy_id，taskid->task_id
        switch (lowerCase){
            case "usertype": return "user_type";
            case "idcard": return "id_card";
            case "id": return "id_card";
            case "strategyId": return "strategy_id";
            case "taskid": return "task_id";
            case "custnum": return "cus_num";
            default:return s;
        }
    }
}
