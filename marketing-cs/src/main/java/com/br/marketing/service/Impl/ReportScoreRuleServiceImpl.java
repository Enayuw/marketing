package com.br.marketing.service.Impl;

import com.alibaba.fastjson.JSONObject;
import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.common.enums.TaskTypeEnum;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.entity.ReportTaskVO;
import com.br.marketing.entity.StraHisFile;
import com.br.marketing.entity.StraHisFileExample;
import com.br.marketing.mapper.MarketingTaskExtendMapper;
import com.br.marketing.mapper.StraHisFileMapper;
import com.br.marketing.service.ReportScoreRuleService;
import com.br.marketing.vo.StrategyProductDetailVO;
import com.br.marketing.vo.TaskInfoVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 跑分模型分布 规则选择并保存任务记录具体实现类
 * @Author: yu.xia@brgroup.com
 * @Date: 2024-08-15
 */
@Slf4j
@Service
public class ReportScoreRuleServiceImpl implements ReportScoreRuleService {
    @Resource
    MarketingTaskExtendMapper marketingTaskExtendMapper;
    @Resource
    StraHisFileMapper straHisFileMapper;

    @Override
    public Map getProducts(String ids) {
        Map map = new HashMap();
        Set<String> fieldsList = new HashSet<>();
        Map<String,String> fieldsNoScoreMap = new HashMap();
        List<Long> fileIds = Arrays.stream(ids.split(",")).map(t->Long.valueOf(t)).collect(Collectors.toList());
        StraHisFileExample straHisFileExample = new StraHisFileExample();
        straHisFileExample.createCriteria().andIdIn(fileIds);
        List<StraHisFile> straHisFiles = straHisFileMapper.selectByExample(straHisFileExample);
        List<String> batchNumbers = straHisFiles.stream().map(t -> t.getBatchNumber()).collect(Collectors.toList());
        Assert.notEmpty(batchNumbers,"没有匹配到批次号");
//        List<String> apiCodes = straHisFiles.stream().map(t -> t.getApiCode()).collect(Collectors.toList());
        List<TaskInfoVO> products = marketingTaskExtendMapper.getProducts(batchNumbers);
        //endregion
        Set<String> allProductSet = new HashSet<>();
        for (TaskInfoVO product : products) {
            String batchNumber = product.getBatchNumber();
            Integer taskType = product.getTaskType();
            if(!TaskTypeEnum.PRODUCTDATA.getValue().equals(taskType)){
                log.warn("batchNumber:[{}]不属于产品跑分", batchNumber);
                continue;
            }
            String strategyProductJson = product.getStrategyProductJson();
            if(strategyProductJson != null && !"".equals(strategyProductJson)){
                StrategyProductDetailVO strategyProductDetailVO = JSONObject.parseObject(strategyProductJson,StrategyProductDetailVO.class);
                List<String> fields = strategyProductDetailVO.getFields();
                Set<String> productSet = new HashSet<>(fields);
                if(fields != null && fields.size() > 0){
                    fieldsList.addAll(fields);
                }
                for (String scoreProduct : productSet) {
                    boolean addFlag = allProductSet.add(scoreProduct);
                    if(addFlag){
                        String batchNumberString = fieldsNoScoreMap.get(scoreProduct);
                        if(StringUtils.isNotBlank(batchNumberString)){
                            fieldsNoScoreMap.put(scoreProduct,batchNumberString+","+batchNumber);
                        }else{
                            fieldsNoScoreMap.put(scoreProduct,batchNumber);
                        }
                    }
                }
            }
        }
        map.put("fields",fieldsList);
        map.put("fieldsNoScore",fieldsNoScoreMap);
        return map;
    }

    @Override
    public ApiResult<Boolean> addReportTask(ReportTaskVO reportTaskVO) {
        String ids = reportTaskVO.getIds();
        String cid = reportTaskVO.getCid();
        String reportName = reportTaskVO.getReportName();
        reportTaskVO.getRules();
        List<Long> fileIds = Arrays.stream(ids.split(",")).map(t->Long.valueOf(t)).collect(Collectors.toList());
        StraHisFileExample straHisFileExample = new StraHisFileExample();
        straHisFileExample.createCriteria().andIdIn(fileIds);
        List<StraHisFile> straHisFiles = straHisFileMapper.selectByExample(straHisFileExample);
        // 给写入b_report_task_score_source表拼装数据

        // 给写入b_report_task表拼装数据


        return new ApiResult<Boolean>().success(true);
    }
}
