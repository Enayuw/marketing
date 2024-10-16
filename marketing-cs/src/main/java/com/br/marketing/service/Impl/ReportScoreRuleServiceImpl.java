package com.br.marketing.service.Impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.common.enums.ServiceResultEnum;
import com.br.marketing.common.exception.KnowException;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.commonentity.PageResultReturn;
import com.br.marketing.entity.*;
import com.br.marketing.enums.report.BiReportTypeEnum;
import com.br.marketing.enums.report.ReportTaskTypeEnum;
import com.br.marketing.mapper.*;
import com.br.marketing.service.ReportScoreRuleService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.vo.CustomerBatchNumVO;
import com.br.marketing.vo.ScoreDetailVo;
import com.br.marketing.vo.StrategyProductDetailVO;
import com.br.marketing.vo.TaskInfoVO;
import com.br.marketing.vo.bi.BiReportTaskVO;
import com.br.marketing.vo.bi.ReportTaskVO;
import com.br.marketing.vo.bi.param.BiReportTaskParam;
import com.br.marketing.vo.bi.param.ReportTaskParam;
import com.br.marketing.vo.zhongan.ZhongAnCustomInfoVO;
import com.github.pagehelper.PageHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import shaded.com.google.common.collect.Lists;
import javax.annotation.Resource;
import java.time.LocalDate;
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
    private MarketingTaskExtendMapper marketingTaskExtendMapper;
    @Resource
    private StraHisFileMapper straHisFileMapper;
    @Resource
    private ReportTaskMapper reportTaskMapper;
    @Resource
    private ReportTaskScoreSourceMapper reportTaskScoreSourceMapper;

    @Resource
    private MarketingTaskMapper marketingTaskMapper;

    @Resource
    private MarketingTaskUserTypeMapper marketingTaskUserTypeMapper;

    @Resource
    private TableCreateServiceImpl tableCreateService;
    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Resource
    private ReportFieldDictMapper reportFieldDictMapper;

    @Resource
    private ReportFieldMappingMapper reportFieldMappingMapper;

    @Resource
    private BiReportMapper biReportMapper;

    @Autowired
    private ZhongAnControlGroupMapper zhongAnControlGroupMapper;

    @Override
    public Map getProducts(String ids, String fieldType) {
        Map map = new HashMap();
        Map<String, String> fieldsMap = new HashMap();
        // 给前端提示产品在不同跑分文件中差异结果<产品,跑分文件>
        Map<String, String> fieldsNoScoreMap = new HashMap();
        // 给前端提示产品在不同跑分文件中差异结果<跑分文件,产品Set>
        List<Map<String, Set>> batchSwiftAndScoreSetList = new ArrayList<>();
        // 需要的跑分产品前缀集合
        Set<String> reportScorePrefixSet = null;
        if ("all".equals(fieldType)) {
            reportScorePrefixSet = marketingCommonConfig.getReportScorePrefixSet();
        } else if ("score".equals(fieldType)) {
            reportScorePrefixSet = marketingCommonConfig.getReportScoreOnlyPrefixSet();
        } else if ("multPoint".equals(fieldType)) {
            reportScorePrefixSet = marketingCommonConfig.getReportmultPointPrefixSet();
        }
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
            String strategyProductJson = product.getStrategyProductJson();
            if (StringUtils.isNotBlank(strategyProductJson)) {
                StrategyProductDetailVO strategyProductDetailVO = JSONObject.parseObject(strategyProductJson, StrategyProductDetailVO.class);
                List<String> fieldAll = strategyProductDetailVO.getFields();
                List<String> fields = new ArrayList<>();
                // 从跑分文件对应表头产品中获取 score、al_、als_开头的产品
                for (String scoreProductPrefix : reportScorePrefixSet) {
                    for (String field : fieldAll) {
                        if(field.startsWith(scoreProductPrefix)){
                            fields.add(field);
                        }
                    }
                }
                if(fields.size() > 0){
                    Set<String> productSet = new HashSet<>(fields);
                    Map<String, Set> scoreAndBatchSwiftMap = new HashMap<>();
                    scoreAndBatchSwiftMap.put(batchNumber, productSet);
                    batchSwiftAndScoreSetList.add(scoreAndBatchSwiftMap);
                }
            }
        }
        if(batchSwiftAndScoreSetList.size()>0){
            // 将不同跑分文件中产品对应的跑分文件和跑分文件之间产品差异显示给前端
            getFieldsNoScore(batchSwiftAndScoreSetList, fieldsNoScoreMap, fieldsMap);
            map.put("fields", fieldsMap);
            map.put("fieldsNoScore", fieldsNoScoreMap);
        }
        return map;
    }

    /**
     * 循环对比跑分文件 将不同跑分文件中产品对应的跑分文件和跑分文件之间产品差异显示给前端
     * 方法处理前：
     * fieldsNoScoreMap=new HashMap();
     * fieldsMap=new HashMap();
     * batchSwiftAndScoreSetList结构：
     *
     * batchSwiftAndScoreSetList:
     *      [{
     * 		  7410908_20240730000000_3346 = [pd_cell_province, pd_cell_type, scorecust, flag_score]
     *        }, {
     * 		  7410908_20240813000000_5934 = [pd_cell_province, pd_cell_type, scorecust, flag_score]
     *      }, {
     * 		  7410908_20240813000000_5283 = [pd_cell_province, pd_cell_type, scorecust, flag_score]
     *      }]
     * 处理结束后：
     *
     * fieldsNoScoreMap:
     *  {
     *      pd_cell_province1 = 7410908_20240613000000_3779,7410908_20240613000000_6436,7410908_20240813000000_9817,
     *      pd_cell_province = 7410908_20240813000000_5283
     *  }
     * fieldsMap:
     *  {
     * 	pd_cell_province = 7410908_20240730000000_3346,7410908_20240813000000_5934,7410908_20240813000000_5283,
     * 	pd_cell_type = 7410908_20240730000000_3346,7410908_20240813000000_5934,7410908_20240813000000_5283,
     * 	scorecust = 7410908_20240730000000_3346,7410908_20240813000000_5934,7410908_20240813000000_5283,
     * 	flag_score = 7410908_20240730000000_3346,7410908_20240813000000_5934,7410908_20240813000000_5283
     * }
     * 
     * @Author yu.xia@brgroup.com
     * @Date 2024/8/15 18:32
     * @param batchSwiftAndScoreSetList 给前端提示产品在不同跑分文件中差异结果<跑分文件,产品Set>，每个跑分文件对应一个set
     * @param fieldsNoScoreMap 比较结果存放的结果集
     * @param fieldsMap fields对应的结果
     */
    private void getFieldsNoScore(List<Map<String, Set>> batchSwiftAndScoreSetList, Map<String, String> fieldsNoScoreMap
            , Map<String, String> fieldsMap) {
        for (int i = 0; i < batchSwiftAndScoreSetList.size(); i++) {
            Map<String, Set> stringSetMapI = batchSwiftAndScoreSetList.get(i);
            String batchNumberI = "";
            Set<String> productFromBatchSwiftSetI = new HashSet<>();
            // 每个stringSetMapI只含有一个batchNumber
            for (Map.Entry<String, Set> e : stringSetMapI.entrySet()) {
                batchNumberI = e.getKey();
                productFromBatchSwiftSetI = e.getValue();
                // 循环获取每个产品对应的 跑分文件（多个以逗号分隔）
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
    public ApiResult<Boolean> addReportTask(ReportTaskParam reportTaskParam){
        String ids = reportTaskParam.getIds();
        String cid = reportTaskParam.getCid();
        String reportTypeName = reportTaskParam.getReportTypeName();
        List<String> businessList = Lists.newArrayList(BiReportTypeEnum.BUSINESS_ANALYSIS_ONE_REPORT.getTypeName(),
                BiReportTypeEnum.BUSINESS_ANALYSIS_EIGHT_REPORT.getTypeName(),
                BiReportTypeEnum.BUSINESS_ANALYSIS_SEVEN_REPORT.getTypeName());

        if (businessList.contains(reportTypeName) && (!checkBusinessReportConfig(reportTaskParam))) {
            return new ApiResult<Boolean>().fail(false, "经营分析报表未配置报表配置，请检查");
        }
        Integer reportType = null;
        if (reportTypeName != null) {
            reportType = BiReportTypeEnum.getEnumByTypeName(reportTypeName).getType();
        }
        String reportName = reportTaskParam.getReportName();
        String rules = reportTaskParam.getRules();
        String productAndBatchNumber = reportTaskParam.getProductAndBatchNumber();
        ReportTaskExample example = new ReportTaskExample();
        example.createCriteria()
                .andIsDelEqualTo(1)
                .andReportNameEqualTo(reportName);
        List<ReportTask> reportTasks = reportTaskMapper.selectByExample(example);
        if(reportTasks.size() > 0){
            return new ApiResult<Boolean>().fail(false, ServiceResultEnum.SUCCESS_6);
        }
        // 给写入b_report_task表拼装数据
        ReportTask reportTask = new ReportTask();
        reportTask.setReportName(reportName);
        reportTask.setReportType(reportType == null ? ReportTaskTypeEnum.SCORE_MODEL_TYPE.getValue() : reportType);
        List<ReportFieldDict> reportFieldDicts = null;
        JSONObject json = new JSONObject();
        if (reportType == null) {
            json.put("rules", rules);
            json.put("productAndBatchNumber", productAndBatchNumber);
        } else {
            json = JSON.parseObject(rules);
            reportFieldDicts = processReportRules(json, reportTypeName);
        }
        reportTask.setReportRules(json.toJSONString());
        reportTask.setStatus(0);
        reportTask.setIsDel(1);
        reportTask.setCreateTime(new Date());
        reportTask.setUpdateTime(new Date());
        reportTaskMapper.insertSelective(reportTask);
        if (businessList.contains(reportTypeName)) {
            return new ApiResult<Boolean>().success(true);
        }

        Long reportId = reportTask.getId();
        if (!CollectionUtils.isEmpty(reportFieldDicts)) {
            addReportFieldMapping(reportFieldDicts, reportId);
        }
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

    private Boolean checkBusinessReportConfig(ReportTaskParam reportTaskParam) {
        JSONObject rulesJson = JSON.parseObject(reportTaskParam.getRules());
        JSONObject transferConfig = rulesJson.getJSONObject("transfer");
        LocalDate startDate = LocalDate.parse(transferConfig.getString("requestStartDate"));
        LocalDate endDate = LocalDate.parse(transferConfig.getString("requestEndDate"));
        List<String> dateList = getDatesBetween(startDate, endDate).stream().map(LocalDate::toString).collect(Collectors.toList());
        long dateBetween = (endDate.toEpochDay() - startDate.toEpochDay()) + 1;
        String reportTypeName = reportTaskParam.getReportTypeName();
        if (BiReportTypeEnum.BUSINESS_ANALYSIS_ONE_REPORT.getTypeName().equals(reportTypeName)) {
            List<ZhongAnCustomInfoVO> oneGroupList = zhongAnControlGroupMapper.selectConfigByGroupbI_(dateList, "1", "1");
            List<ZhongAnCustomInfoVO> twoGroupList = zhongAnControlGroupMapper.selectConfigByGroupbI_(dateList, "1", "2");
            return (oneGroupList.size() == dateBetween) && (twoGroupList.size() == dateBetween);
        }
        if (BiReportTypeEnum.BUSINESS_ANALYSIS_SEVEN_REPORT.getTypeName().equals(reportTypeName)) {
            List<ZhongAnCustomInfoVO> threeGroupList = zhongAnControlGroupMapper.selectConfigByGroupbI_(dateList, "7", "3");
            List<ZhongAnCustomInfoVO> fourGroupList = zhongAnControlGroupMapper.selectConfigByGroupbI_(dateList, "7", "4");
            return (threeGroupList.size() == dateBetween) && (fourGroupList.size() == dateBetween);
        }
        if (BiReportTypeEnum.BUSINESS_ANALYSIS_EIGHT_REPORT.getTypeName().equals(reportTypeName)) {
            List<ZhongAnCustomInfoVO> fiveGroupList = zhongAnControlGroupMapper.selectConfigByGroupbI_(dateList, "8", "5");
            return fiveGroupList.size() == dateBetween;
        }
        return Boolean.FALSE;
    }

    private List<LocalDate> getDatesBetween(LocalDate startDate, LocalDate endDate) {
        List<LocalDate> localDateList = new ArrayList<>();
        long length = endDate.toEpochDay() - startDate.toEpochDay();
        for (long i = length; i >= 0; i--) {
            LocalDate localDate = endDate.minusDays(i);
            localDateList.add(localDate);
        }
        return localDateList;
    }

    private void addReportFieldMapping(List<ReportFieldDict> reportFieldDicts, Long reportId) {
        for (ReportFieldDict reportFieldDict : reportFieldDicts) {
            ReportFieldMapping reportFieldMapping = new ReportFieldMapping();
            reportFieldMapping.setReportTaskId(reportId.toString());
            reportFieldMapping.setUserType(reportFieldDict.getUserType());
            reportFieldMapping.setItemShow(reportFieldDict.getItemShow());
            reportFieldMapping.setItemName(reportFieldDict.getItemName());
            reportFieldMapping.setItemOrder(reportFieldDict.getItemOrder());
            reportFieldMapping.setItemFormatType(reportFieldDict.getItemFormatType());
            reportFieldMapping.setCreateTime(new Date());
            reportFieldMapping.setUpdateTime(new Date());
            reportFieldMappingMapper.insertSelective(reportFieldMapping);
        }
    }

    /**
     * @param rulesJson
     * @param reportTypeName
     * @return List<ReportFieldDict>
     * @description 处理rules，并新增【b_report_field_mapping】
     * @author hedongshuo
     * @date 2024/9/23 17:22
     **/
    private List<ReportFieldDict> processReportRules(JSONObject rulesJson, String reportTypeName){
        //1.补充batchNumber
        JSONObject score = rulesJson.getJSONObject("score");
        if (!score.containsKey("batchNumber")) {
            score.put("batchNumber", "");
            rulesJson.put("score", score);
        }
        //2.补充dimensions_value
        String apiCode = rulesJson.getString("apiCode");
        JSONObject upload = rulesJson.getJSONObject("upload");
        String dimensionsField = upload.getString("dimensionsField");
        String userType = upload.getString("userType");
        HashMap<String, JSONObject> groupDictConfig = marketingCommonConfig.getBiReportGroupDictConfig();
        JSONObject apiCodeDictConfig = groupDictConfig.get(apiCode);
        JSONObject userTypeDictConfig = apiCodeDictConfig.getJSONObject(userType);
        if (userTypeDictConfig != null) {
            JSONArray dictConfig = userTypeDictConfig.getJSONArray(dimensionsField);
            upload.put("dimensionsValue", dictConfig);
        }
        BiReportTypeEnum biReportTypeEnum = BiReportTypeEnum.getEnumByTypeName(reportTypeName);
        //3.分组校验
        if (!"defaultNone".equals(dimensionsField)
                && (biReportTypeEnum == BiReportTypeEnum.MULTPOINT_REPORT
                    || biReportTypeEnum == BiReportTypeEnum.GROUP_SCORE_REPORT
                    || biReportTypeEnum == BiReportTypeEnum.TRANSFER_ANALYSIS_REPORT)) {
            String batchNumber = score.getString("batchNumber");
            Integer scoreDataCountBydimension = biReportMapper
                    .getScoreDataCountBydimensionbI_("b_score_" + batchNumber, apiCode, userType, dimensionsField);
            if (scoreDataCountBydimension == 0) {
                throw new KnowException("跑分批次" + batchNumber +"无法以该分组生成报表");
            }
        }
        //4.补充statistics_scene
        HashMap<String, String> biReportScenePrefixConfig = marketingCommonConfig.getBiReportScenePrefixConfig();
        String scenePrefix = biReportScenePrefixConfig.get(apiCode);
        String scene = "";
        if (Objects.equals(biReportTypeEnum.getType(), BiReportTypeEnum.BUSINESS_ANALYSIS_ONE_REPORT.getType())
                || Objects.equals(biReportTypeEnum.getType(), BiReportTypeEnum.BUSINESS_ANALYSIS_SEVEN_REPORT.getType())
                || Objects.equals(biReportTypeEnum.getType(), BiReportTypeEnum.BUSINESS_ANALYSIS_EIGHT_REPORT.getType())) {
            scene = String.format("%s(%s)_%s场景%s", scenePrefix, apiCode, userType, biReportTypeEnum.getStatName());
        } else {
            scene = String.format("%s(%s)_%s", scenePrefix, apiCode, biReportTypeEnum.getStatName());
        }
        rulesJson.put("upload", upload);
        rulesJson.put("statisticsScene", scene);
        //5.增加【b_report_field_mapping】
        ReportFieldDictExample dictExample = new ReportFieldDictExample();
        dictExample.createCriteria()
                .andApiCodeEqualTo(apiCode)
                .andReportTypeEqualTo(biReportTypeEnum.getType().toString())
                .andUserTypeEqualTo(userType)
                .andIsDelEqualTo(1);
        return reportFieldDictMapper.selectByExample(dictExample);
    }

    /**
     * 获取报告任务列表
     *
     * @param page 第页
     * @param pageSize 页面大小
     * @param name
     * @param apiCodes apiCodes
     * @return {@link PageResultReturn }
     * @author senyang.zheng
     * @date 2024/08/19
     */
    @Override
    public PageResultReturn getReportTaskList(int page, int pageSize, String name, List<String> apiCodes) {
        PageHelper.startPage(page, pageSize);
        try {
            List<ReportTaskVO> list = reportTaskMapper.findListtikv_(name, apiCodes);
            return PageResultReturn.setPageResult(list, page, pageSize);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    @Override
    public PageResultReturn<List<ScoreDetailVo>> getBatchInfoList(CustomerBatchNumVO batchNumVO) {
        PageHelper.startPage(batchNumVO.getCurrent(), batchNumVO.getSize()).setOrderBy(" scoreBeginTime desc,fileId desc ");
        List<ScoreDetailVo> scoreDetailVos = marketingTaskMapper.queryBatchList(batchNumVO);
        scoreDetailVos.forEach((ScoreDetailVo t) -> {
            List<String> batchNumberList = marketingTaskUserTypeMapper.queryUserTypeByBatchNumberAndApiCodetikv_(
                    t.getApiCode(), t.getBatchNumber());
            t.setCid(tableCreateService.getCId(t.getApiCode()));
            t.setUserType(String.join(",", batchNumberList));
        });
        return (PageResultReturn<List<ScoreDetailVo>>)PageResultReturn.setPageResult(scoreDetailVos, batchNumVO.getCurrent(), batchNumVO.getSize());
    }

    /**
     * Bi报表列表查看（众安）
     * @param reportTaskParam
     * @return
     */
    @Override
    public PageResultReturn getBiReportTaskList(BiReportTaskParam reportTaskParam) {
        int page = 1;
        int pageSize = 10;
        if (reportTaskParam != null) {
            page = reportTaskParam.getCurrent() == null ? page : reportTaskParam.getCurrent();
            pageSize = reportTaskParam.getSize() == null ? pageSize : reportTaskParam.getSize();
        }
        PageHelper.startPage(page, pageSize);
        try {
            convertReportType(reportTaskParam);
            List<BiReportTaskVO> list = reportTaskMapper.queryBiReportTaskListtikv_(reportTaskParam);
            processBiReportTaskVO(list);
            return PageResultReturn.setPageResult(list, page, pageSize);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    /**
     * 转化报表类型名称，解析reportRules
     * @param list
     */
    private void processBiReportTaskVO(List<BiReportTaskVO> list) {
        for (BiReportTaskVO biReportTaskVO : list) {
            biReportTaskVO.setReportTypeName(BiReportTypeEnum.getEnumByType(biReportTaskVO.getReportType()).getTypeName());
            String requestStartDate = biReportTaskVO.getRequestStartDate();
            String requestEndDate = biReportTaskVO.getRequestEndDate();
            if (!StringUtils.isEmpty(requestStartDate) && !StringUtils.isEmpty(requestEndDate)) {
                String transferDateTimeRange = requestStartDate + "~" + requestEndDate;
                biReportTaskVO.setTransferDateTimeRange(transferDateTimeRange);
            }
        }
    }

    /**
     * @description 将reportTypeName转为reportType
     * @return void
     * @author hedongshuo
     * @date 2024/9/23 14:03
     **/
    private void convertReportType(BiReportTaskParam reportTaskParam) {
        if (reportTaskParam == null || StringUtils.isEmpty(reportTaskParam.getReportTypeName())) {
            return;
        }
        reportTaskParam.setReportType(BiReportTypeEnum.getEnumByTypeName(reportTaskParam.getReportTypeName()).getType());
    }
}
