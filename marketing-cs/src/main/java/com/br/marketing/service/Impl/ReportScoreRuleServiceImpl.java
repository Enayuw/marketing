package com.br.marketing.service.Impl;

import com.alibaba.fastjson.JSONObject;
import com.br.marketing.aspect.AuthDataPermission;
import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.common.enums.TaskTypeEnum;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.commonentity.PageResultReturn;
import com.br.marketing.context.ThreadContextInfo;
import com.br.marketing.entity.ReportTask;
import com.br.marketing.entity.ReportTaskScoreSource;
import com.br.marketing.entity.StraHisFile;
import com.br.marketing.entity.StraHisFileExample;
import com.br.marketing.mapper.*;
import com.br.marketing.service.ReportScoreRuleService;
import com.br.marketing.vo.CustomerBatchNumVO;
import com.br.marketing.vo.ScoreDetailVo;
import com.br.marketing.vo.StrategyProductDetailVO;
import com.br.marketing.vo.TaskInfoVO;
import com.br.marketing.vo.bi.ReportTaskVO;
import com.br.marketing.vo.bi.param.ReportTaskParam;
import com.github.pagehelper.PageHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 跑分模型分布 规则选择并保存任务记录具体实现类
 * 
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


    @Resource
    private MarketingTaskMapper marketingTaskMapper;

    @Resource
    private MarketingTaskUserTypeMapper marketingTaskUserTypeMapper;

    @Resource
    private TableCreateServiceImpl tableCreateService;

    @Override
    public Map getProducts(String ids) {
        Map map = new HashMap();
        Map<String, String> fieldsMap = new HashMap();
        // 给前端提示产品在不同跑分文件中差异结果<产品,跑分文件>
        Map<String, String> fieldsNoScoreMap = new HashMap();
        // 给前端提示产品在不同跑分文件中差异结果<跑分文件,产品Set>
        List<Map<String, Set>> batchSwiftAndScoreSetList = new ArrayList<>();
        List<Long> fileIds = Arrays.stream(ids.split(",")).map(t -> Long.valueOf(t)).collect(Collectors.toList());
        StraHisFileExample straHisFileExample = new StraHisFileExample();
        straHisFileExample.createCriteria().andIdIn(fileIds);
        List<StraHisFile> straHisFiles = straHisFileMapper.selectByExample(straHisFileExample);
        if (straHisFiles == null || straHisFiles.size() < 1) {
            return map;
        }
        List<String> batchNumbers = straHisFiles.stream().map(t -> t.getBatchNumber()).collect(Collectors.toList());
        Assert.notEmpty(batchNumbers, "没有匹配到批次号");
        List<TaskInfoVO> products = marketingTaskExtendMapper.getProducts(batchNumbers);
        for (TaskInfoVO product : products) {
            String batchNumber = product.getBatchNumber();
            Integer taskType = product.getTaskType();
            if (!TaskTypeEnum.PRODUCTDATA.getValue().equals(taskType)) {
                log.warn("batchNumber:[{}]不属于产品跑分类型", batchNumber);
                continue;
            }
            String strategyProductJson = product.getStrategyProductJson();
            if (StringUtils.isNotBlank(strategyProductJson)) {
                StrategyProductDetailVO strategyProductDetailVO = JSONObject.parseObject(strategyProductJson, StrategyProductDetailVO.class);
                List<String> fields = strategyProductDetailVO.getFields();
                Set<String> productSet = new HashSet<>(fields);
                Map<String, Set> scoreAndBatchSwiftMap = new HashMap<>();
                scoreAndBatchSwiftMap.put(batchNumber, productSet);
                batchSwiftAndScoreSetList.add(scoreAndBatchSwiftMap);
            }
        }
        // 将不同跑分文件中产品对应的跑分文件和跑分文件之间产品差异显示给前端
        getFieldsNoScore(fieldsNoScoreMap, batchSwiftAndScoreSetList, fieldsMap);
        map.put("fields", fieldsMap);
        map.put("fieldsNoScore", fieldsNoScoreMap);
        return map;
    }

    /**
     * 循环对比跑分文件 将不同跑分文件中产品对应的跑分文件和跑分文件之间产品差异显示给前端
     * 
     * @Author yu.xia@brgroup.com
     * @Date 2024/8/15 18:32
     * @param fieldsNoScoreMap 比较结果存放的结果集
     * @param batchSwiftAndScoreSetList 给前端提示产品在不同跑分文件中差异结果<跑分文件,产品Set>，每个跑分文件对应一个set
     * @param fieldsMap fields对应的结果
     */
    private void getFieldsNoScore(Map<String, String> fieldsNoScoreMap, List<Map<String, Set>> batchSwiftAndScoreSetList,
        Map<String, String> fieldsMap) {
        for (int i = 0; i < batchSwiftAndScoreSetList.size(); i++) {
            Map<String, Set> stringSetMapI = batchSwiftAndScoreSetList.get(i);
            String batchNumberI = "";
            Set<String> productFromBatchSwiftSetI = new HashSet<>();
            // 循环获取每个产品对应的 跑分文件（多个以逗号分隔）
            for (Map.Entry<String, Set> e : stringSetMapI.entrySet()) {
                batchNumberI = e.getKey();
                productFromBatchSwiftSetI = e.getValue();
                for (String product : productFromBatchSwiftSetI) {
                    String batchNumberString = fieldsMap.get(product);
                    if (StringUtils.isNotBlank(batchNumberString)) {
                        if (!batchNumberString.contains(batchNumberI)) {
                            fieldsMap.put(product, batchNumberString + "," + batchNumberI);
                        }
                    } else {
                        fieldsMap.put(product, batchNumberI);
                    }
                }
            }
            for (int j = 1 + i; j < batchSwiftAndScoreSetList.size(); j++) {
                Map<String, Set> stringSetMapJ = batchSwiftAndScoreSetList.get(j);
                String batchNumberJ = "";
                Set<String> productFromBatchSwiftSetJ = new HashSet<>();;
                for (Map.Entry<String, Set> e : stringSetMapJ.entrySet()) {
                    batchNumberJ = e.getKey();
                    productFromBatchSwiftSetJ = e.getValue();
                }
                // 嵌套循环，对比每两个文件之间的产品差异
                Set<String> finalProductFromBatchSwiftSetJ = productFromBatchSwiftSetJ;
                Set<String> difference0 = productFromBatchSwiftSetI.stream().filter((String item) -> !finalProductFromBatchSwiftSetJ.contains(item))
                    .collect(Collectors.toSet());
                for (String scoreProduct : difference0) {
                    String batchNumberString = fieldsNoScoreMap.get(scoreProduct);
                    if (StringUtils.isNotBlank(batchNumberString)) {
                        if (!batchNumberString.contains(batchNumberJ)) {
                            fieldsNoScoreMap.put(scoreProduct, batchNumberString + "," + batchNumberJ);
                        }
                    } else {
                        fieldsNoScoreMap.put(scoreProduct, batchNumberJ);
                    }
                }
                Set<String> finalProductFromBatchSwiftSetI = productFromBatchSwiftSetI;
                Set<String> difference = finalProductFromBatchSwiftSetJ.stream()
                    .filter((String item) -> !finalProductFromBatchSwiftSetI.contains(item)).collect(Collectors.toSet());
                for (String scoreProduct : difference) {
                    String batchNumberString = fieldsNoScoreMap.get(scoreProduct);
                    if (StringUtils.isNotBlank(batchNumberString)) {
                        if (!batchNumberString.contains(batchNumberI)) {
                            fieldsNoScoreMap.put(scoreProduct, batchNumberString + "," + batchNumberI);
                        }
                    } else {
                        fieldsNoScoreMap.put(scoreProduct, batchNumberI);
                    }
                }
            }
        }
    }

    @Override
    public ApiResult<Boolean> addReportTask(ReportTaskParam reportTaskParam) {
        String ids = reportTaskParam.getIds();
        String cid = reportTaskParam.getCid();
        String reportName = reportTaskParam.getReportName();
        String rules = reportTaskParam.getRules();
        String productAndBatchNumber = reportTaskParam.getProductAndBatchNumber();
        JSONObject json = new JSONObject();
        json.put("rules", rules);
        json.put("productAndBatchNumber", productAndBatchNumber);
        // 给写入b_report_task表拼装数据
        ReportTask reportTask = new ReportTask();
        reportTask.setReportName(reportName);
        reportTask.setReportRules(json.toJSONString());
        reportTask.setStatus(0);
        reportTask.setReportType(1);
        reportTask.setIsDel(1);
        reportTask.setCreateTime(new Date());
        reportTask.setUpdateTime(new Date());
        reportTaskMapper.insertSelective(reportTask);
        Long reportId = reportTask.getId();
        List<Long> fileIds = Arrays.stream(ids.split(",")).map(t -> Long.valueOf(t)).collect(Collectors.toList());
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
        if (list.size() > 0) {
            reportTaskScoreSourceMapper.insertBatch(list);
        }
        return new ApiResult<Boolean>().success(true);
    }

    /**
     * 获取报告任务列表
     *
     * @param page 第页
     * @param pageSize 页面大小
     * @param apiCodes apiCodes
     * @return {@link PageResultReturn }
     * @author senyang.zheng
     * @date 2024/08/19
     */
    @Override
    @AuthDataPermission
    public PageResultReturn getReportTaskList(int page, int pageSize, List<String> apiCodes) {
        PageHelper.startPage(page, pageSize);
        try {
            List<ReportTaskVO> list = reportTaskMapper.findList(apiCodes);
            return PageResultReturn.setPageResult(list, page, pageSize);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }


    @Override
    public PageResultReturn<List<ScoreDetailVo>> getBatchInfoList(CustomerBatchNumVO batchNumVO) {
        if (batchNumVO.getApiCodeSet() == null || batchNumVO.getApiCodeSet().size() == 0) {
            String apiCode = ThreadContextInfo.getUser().getApiCode();
            if (org.apache.commons.lang3.StringUtils.isNotBlank(apiCode)) {
                String[] split = apiCode.split(",");
                batchNumVO.setApiCodeSet(new HashSet<>(Arrays.asList(split)));
            }
        }
        PageHelper.startPage(batchNumVO.getCurrent(), batchNumVO.getSize()).setOrderBy(" scoreBeginTime desc,fileId desc ");
        List<ScoreDetailVo> scoreDetailVos = marketingTaskMapper.queryBatchList(batchNumVO);
        scoreDetailVos.forEach((ScoreDetailVo t) -> {
            List<String> batchNumberList = marketingTaskUserTypeMapper.queryUserTypeByBatchNumberAndApiCodetikv_(
                    t.getBatchNumber(), t.getApiCode());
            t.setCid(tableCreateService.getCId(t.getApiCode()));
            t.setUserType(String.join(",", batchNumberList));
        });
        return (PageResultReturn<List<ScoreDetailVo>>) PageResultReturn.setPageResult(scoreDetailVos, batchNumVO.getCurrent()
                , batchNumVO.getSize());
    }
}
