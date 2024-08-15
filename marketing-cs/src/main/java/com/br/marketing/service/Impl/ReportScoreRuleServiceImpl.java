package com.br.marketing.service.Impl;

import com.alibaba.fastjson.JSONObject;
import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.common.enums.TaskTypeEnum;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.entity.*;
import com.br.marketing.mapper.MarketingTaskExtendMapper;
import com.br.marketing.mapper.ReportTaskMapper;
import com.br.marketing.mapper.ReportTaskScoreSourceMapper;
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
    @Resource
    ReportTaskMapper reportTaskMapper;
    @Resource
    ReportTaskScoreSourceMapper reportTaskScoreSourceMapper;

    @Override
    public Map getProducts(String ids) {
        Map map = new HashMap();
        Set<String> fieldsList = new HashSet<>();
        // 给前端提示产品在不同跑分文件中差异结果<产品,跑分文件>
        Map<String,String> fieldsNoScoreMap = new HashMap();
        // 给前端提示产品在不同跑分文件中差异结果<跑分文件,产品Set>
        List<Map<String,Set>> batchSwiftAndScoreSetList = new ArrayList<>();
        List<Long> fileIds = Arrays.stream(ids.split(",")).map(t->Long.valueOf(t)).collect(Collectors.toList());
        StraHisFileExample straHisFileExample = new StraHisFileExample();
        straHisFileExample.createCriteria().andIdIn(fileIds);
        List<StraHisFile> straHisFiles = straHisFileMapper.selectByExample(straHisFileExample);
        if(straHisFiles == null || straHisFiles.size()<1){
            return map;
        }
        List<String> batchNumbers = straHisFiles.stream().map(t -> t.getBatchNumber()).collect(Collectors.toList());
        Assert.notEmpty(batchNumbers,"没有匹配到批次号");
        List<TaskInfoVO> products = marketingTaskExtendMapper.getProducts(batchNumbers);
        for (TaskInfoVO product : products) {
            String batchNumber = product.getBatchNumber();
            Integer taskType = product.getTaskType();
            if(!TaskTypeEnum.PRODUCTDATA.getValue().equals(taskType)){
                log.warn("batchNumber:[{}]不属于产品跑分类型", batchNumber);
                continue;
            }
            String strategyProductJson = product.getStrategyProductJson();
            if(StringUtils.isNotBlank(strategyProductJson)){
                StrategyProductDetailVO strategyProductDetailVO = JSONObject.parseObject(strategyProductJson,StrategyProductDetailVO.class);
                List<String> fields = strategyProductDetailVO.getFields();
                Set<String> productSet = new HashSet<>(fields);
                Map<String,Set> scoreAndBatchSwiftMap = new HashMap<>();
                scoreAndBatchSwiftMap.put(batchNumber, productSet);
                batchSwiftAndScoreSetList.add(scoreAndBatchSwiftMap);
                if(fields != null && fields.size() > 0){
                    fieldsList.addAll(fields);
                }
            }
        }
        getFieldsNoScore(fieldsNoScoreMap, batchSwiftAndScoreSetList);
        map.put("fields",fieldsList);
        map.put("fieldsNoScore",fieldsNoScoreMap);
        return map;
    }

    /**
     * 循环对比跑分文件并获取 fieldsNoScore
     * @Author yu.xia@brgroup.com
     * @Date 2024/8/15 18:32
     * @param fieldsNoScoreMap 比较结果存放的结果集
     * @param batchSwiftAndScoreSetList
     */
    private void getFieldsNoScore(Map<String, String> fieldsNoScoreMap, List<Map<String, Set>> batchSwiftAndScoreSetList) {
        for (int i = 0; i < batchSwiftAndScoreSetList.size(); i++) {
            Map<String, Set> stringSetMapI = batchSwiftAndScoreSetList.get(i);
            String batchNumberI = "";
            Set<String> productFromBatchSwiftSetI = new HashSet<>();
            for(Map.Entry<String, Set> e : stringSetMapI.entrySet()){
                batchNumberI = e.getKey();
                productFromBatchSwiftSetI = e.getValue();
            }
            for (int j = 1+i; j < batchSwiftAndScoreSetList.size(); j++) {
                Map<String, Set> stringSetMapJ = batchSwiftAndScoreSetList.get(j);
                String batchNumberJ = "";
                Set<String> productFromBatchSwiftSetJ = new HashSet<>();;
                for(Map.Entry<String, Set> e : stringSetMapJ.entrySet()){
                    batchNumberJ = e.getKey();
                    productFromBatchSwiftSetJ = e.getValue();
                }
                Set<String> finalProductFromBatchSwiftSetJ = productFromBatchSwiftSetJ;
                Set<String> difference0 = productFromBatchSwiftSetI.stream()
                        .filter((String item) -> !finalProductFromBatchSwiftSetJ.contains(item))
                        .collect(Collectors.toSet());
                for (String scoreProduct : difference0) {
                    String batchNumberString = fieldsNoScoreMap.get(scoreProduct);
                    if(StringUtils.isNotBlank(batchNumberString)){
                        if(!batchNumberString.contains(batchNumberJ)){
                            fieldsNoScoreMap.put(scoreProduct,batchNumberString+","+batchNumberJ);
                        }
                    }else{
                        fieldsNoScoreMap.put(scoreProduct,batchNumberJ);
                    }
                }
                Set<String> finalProductFromBatchSwiftSetI = productFromBatchSwiftSetI;
                Set<String> difference = finalProductFromBatchSwiftSetJ.stream()
                        .filter((String item) -> !finalProductFromBatchSwiftSetI.contains(item))
                        .collect(Collectors.toSet());
                for (String scoreProduct : difference) {
                    String batchNumberString = fieldsNoScoreMap.get(scoreProduct);
                    if(StringUtils.isNotBlank(batchNumberString)){
                        if(!batchNumberString.contains(batchNumberI)){
                            fieldsNoScoreMap.put(scoreProduct,batchNumberString+","+batchNumberI);
                        }
                    }else{
                        fieldsNoScoreMap.put(scoreProduct,batchNumberI);
                    }
                }
            }
        }
    }

    @Override
    public ApiResult<Boolean> addReportTask(ReportTaskVO reportTaskVO) {
        String ids = reportTaskVO.getIds();
        String cid = reportTaskVO.getCid();
        String reportName = reportTaskVO.getReportName();
        String rules = reportTaskVO.getRules();
        // 给写入b_report_task表拼装数据
        ReportTask reportTask = new ReportTask();
        reportTask.setReportName(reportName);
        reportTask.setReportRules(rules);
        reportTask.setStatus(0);
        reportTask.setReportType(1);
        reportTask.setIsDel(1);
        reportTask.setCreateTime(new Date());
        reportTask.setUpdateTime(new Date());
        reportTaskMapper.insertSelective(reportTask);
        Long reportId = reportTask.getId();
        List<Long> fileIds = Arrays.stream(ids.split(",")).map(t->Long.valueOf(t)).collect(Collectors.toList());
        StraHisFileExample straHisFileExample = new StraHisFileExample();
        straHisFileExample.createCriteria().andIdIn(fileIds);
        List<StraHisFile> straHisFiles = straHisFileMapper.selectByExample(straHisFileExample);
        List<ReportTaskScoreSource> list = new ArrayList<>();
        for (int i = 0; i < straHisFiles.size(); i++) {
            StraHisFile straHisFile = straHisFiles.get(i);
            // 给写入b_report_task_score_source表拼装数据
            ReportTaskScoreSource reportTaskScoreSource = new ReportTaskScoreSource();
            reportTaskScoreSource.setReportId(reportId);
            reportTaskScoreSource.setCid(cid);
            reportTaskScoreSource.setApiCode(straHisFile.getApiCode());
            reportTaskScoreSource.setBatchNumber(straHisFile.getBatchNumber());
            reportTaskScoreSource.setCreateTime(new Date());
            reportTaskScoreSource.setUpdateTime(new Date());
            list.add(reportTaskScoreSource);
        }
        if(list.size()>0){
            reportTaskScoreSourceMapper.insertBatch(list);
        }
        return new ApiResult<Boolean>().success(true);
    }
}
