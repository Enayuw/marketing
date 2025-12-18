package com.br.marketing.service.Impl;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.google.common.collect.Lists;

import java.io.IOException;
import java.time.ZoneId;
import java.util.Date;

import cn.hutool.core.util.ObjectUtil;
import com.br.common.log.AlertLog;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.common.constants.rediskey.RedisKeyConstant;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.dto.report.IntervalRangeDTO;
import com.br.marketing.dto.report.RefreshReportRequestDTO;
import com.br.marketing.entity.auth.MarketingUserDetail;
import com.br.marketing.service.bi.ReportStatisticService;
import com.br.marketing.vo.bi.IntervalTemplateVO;
import com.br.marketing.vo.bi.param.BiReportStatisticTransferParam;
import com.fasterxml.jackson.core.type.TypeReference;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.common.enums.ServiceResultEnum;
import com.br.marketing.common.exception.KnowException;
import com.br.marketing.common.utils.Constants;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.commonentity.PageResultReturn;
import com.br.marketing.entity.*;
import com.br.marketing.enums.report.BiReportTypeEnum;
import com.br.marketing.enums.report.ReportTaskTypeEnum;
import com.br.marketing.mapper.*;
import com.br.marketing.service.ReportScoreRuleService;
import com.br.marketing.service.bi.AnalysisReportService;
import com.br.marketing.service.bi.impl.ZhongAnControlGroupServiceImpl;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.vo.CustomerBatchNumVO;
import com.br.marketing.vo.ScoreDetailVo;
import com.br.marketing.vo.StrategyProductDetailVO;
import com.br.marketing.vo.TaskInfoVO;
import com.br.marketing.vo.bi.AxisWrapVO;
import com.br.marketing.vo.bi.BiReportTaskVO;
import com.br.marketing.vo.bi.ReportTaskVO;
import com.br.marketing.vo.bi.param.BiReportTaskParam;
import com.br.marketing.vo.bi.param.ReportTaskParam;
import com.br.marketing.vo.zhongan.ZhongAnCustomInfoVO;
import com.br.marketing.vo.zhongan.param.ZhongAnControlGroupParam;
import com.br.marketing.vo.zhongan.param.ZhongAnCustomInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.pagehelper.PageHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
    private ReportStatisticsScoreMapper reportStatisticsScoreMapper;

    @Resource
    private ReportIntervalConfigMapper reportIntervalConfigMapper;

    @Resource
    private ReportIntervalModelMapper reportIntervalModelMapper;

    @Resource
    private ScoreStatisticsDetailMapper scoreStatisticsDetailMapper;

    @Resource
    private CustomIntervalStatisticsImpl customIntervalStatistics;

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

    @Resource
    private AnalysisReportService analysisReportService;

    @Resource
    ZhongAnControlGroupServiceImpl zhongAnControlGroupService;

    @Resource
    ReportStatisticTransferMapper reportStatisticTransferMapper;

    @Resource
    ReportStatisticService reportStatisticService;

    @Autowired
    RedisChgService redisChgService;

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
            // 添加画像分布配置到前缀集合中
            addImageDistribution(reportScorePrefixSet);
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

    public void addImageDistribution(Set<String> reportScorePrefixSet) {
        String imageDistribution = marketingCommonConfig.getImageDistribution();
        String[] distributions = imageDistribution.split(",");
        reportScorePrefixSet.addAll(Arrays.asList(distributions));
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
            ZhongAnControlGroupParam param = new ZhongAnControlGroupParam();
            param.setReportDate(LocalDate.now().toString());
            ObjectMapper objectMapper = new ObjectMapper();
            try {
                String jsonData1 = "[{\"constituencies\":1,\"totalNum\":0,\"incomingNum\":0,\"approversNum\":0}," +
                        "{\"constituencies\":2,\"totalNum\":0,\"incomingNum\":0,\"approversNum\":0}]";
                String jsonData7 = "[{\"constituencies\":3,\"payPassRate\":0,\"lendersSucAmount\":0}," +
                        "{\"constituencies\":4,\"totalNum\":0,\"loginRate\":0,\"incomingNum\":0,\"approversNum\":0," +
                        "\"approvalAvailable\":0,\"applyPayNum\":0,\"payPassRate\":0,\"lendersSucNum\":0,\"lendersSucAmount\":0}]";
                String jsonData8 = "[{\"constituencies\":5,\"totalNum\":0,\"incomingNum\":0,\"approversNum\":0}]";

                param.setUserType1(objectMapper.readValue(jsonData1, new TypeReference<List<ZhongAnCustomInfo>>() {}));
                param.setUserType7(objectMapper.readValue(jsonData7, new TypeReference<List<ZhongAnCustomInfo>>() {}));
                param.setUserType8(objectMapper.readValue(jsonData8, new TypeReference<List<ZhongAnCustomInfo>>() {}));
                zhongAnControlGroupService.saveCustomInfo(param);
            } catch (Exception e) {
                log.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.YINGXIAO_SERVICEERROR.getCode(),
                                "默认统计生成报表配置错误"), e);
            }

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
        reportTask.setTemplateId(reportTaskParam.getStatisticsId() == null ? null: String.valueOf(reportTaskParam.getStatisticsId()));
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
     * @description 根据评分分布名称和跑分文件id筛选评分分布列表
     * @param name
     * @param ids
     * @return List<ReportTaskVO>
     * @author hedongshuo
     * @date 2024/10/24 16:01
     **/
    @Override
    public List<ReportTaskVO> getReportTaskListForScore(String name, String ids) {
        List<Long> fileIds = Arrays.stream(ids.split(",")).map(t->Long.valueOf(t)).collect(Collectors.toList());
        StraHisFileExample straHisFileExample = new StraHisFileExample();
        straHisFileExample.createCriteria().andIdIn(fileIds);
        List<StraHisFile> straHisFiles = straHisFileMapper.selectByExample(straHisFileExample);
        List<String> batchNumbers = straHisFiles.stream().map(t -> t.getBatchNumber()).collect(Collectors.toList());
        if (batchNumbers.size() == 0) {
            return null;
        }
        List<Map<String, Object>> reportIdData =  reportTaskScoreSourceMapper.selectReportIdByBatchNumberstikv_(batchNumbers, batchNumbers.size());
        if (reportIdData.size() == 0) {
            return null;
        }
        List<String> reportIds = reportIdData.stream().map((Map each) -> each.get("reportId").toString()).collect(Collectors.toList());
        List<ReportTaskVO> reportTaskVOS = reportTaskMapper.selectDataByIds(reportIds, name);
        return buildReportTask(reportTaskVOS);
    }

    private List<ReportTaskVO> buildReportTask(List<ReportTaskVO> reportTaskVOS) {
        return reportTaskVOS.stream()
                .peek(reportTaskVO -> {
                    List<AxisWrapVO> reportDetailsByTaskId = analysisReportService.getReportDetailsByTaskId(reportTaskVO.getId());
                    reportTaskVO.setAxisWrapVOS(reportDetailsByTaskId);
                })
                .collect(Collectors.toList());
    }

    /**
     * 更新报表名称
     * @param id
     * @param reportName
     * @return
     */
    @Override
    public ApiResult<Boolean> updateReportName(Long id, String reportName) {
        try {
            ReportTask reportTask = reportTaskMapper.selectByPrimaryKey(id);
            if(reportTask == null){
                return new ApiResult<Boolean>().fail(false, "未找到该报表！");
            }
            ReportTaskExample taskExample = new ReportTaskExample();
            taskExample.createCriteria()
                    .andReportNameEqualTo(reportName)
                    .andIsDelEqualTo(Constants.DATA_VALID);
            int l = reportTaskMapper.countByExample(taskExample);
            if(l>0){
                return new ApiResult<Boolean>().fail(false, "报表名称重复，请重新输入");
            }
            ReportTask reportTask1 = new ReportTask();
            reportTask1.setId(id);
            reportTask1.setReportName(reportName);
            reportTaskMapper.updateByPrimaryKeySelective(reportTask1);
        }catch (Exception e){
            log.warn("更新报表名称报错，id：{}, reportName：{}", id,reportName);
        }
        return new ApiResult<Boolean>().success(true);
    }

    /**
     * 报表删除
     * @param id
     * @return
     */
    @Override
    public ApiResult<Boolean> deleteReport(Long id) {
        try {
            ReportTask reportTask = reportTaskMapper.selectByPrimaryKey(id);
            if(reportTask == null){
                return new ApiResult<Boolean>().fail(false, "未找到该报表！");
            }
            ReportTask reportTask1 = new ReportTask();
            reportTask1.setId(id);
            reportTask1.setIsDel(9);
            reportTaskMapper.updateByPrimaryKeySelective(reportTask1);
        }catch (Exception e){
            log.warn("报表删除有误，id：{}", id);
        }
        return new ApiResult<Boolean>().success(true);
    }

    @Override
    public Result<Boolean> refreshCustomIntervalReport(RefreshReportRequestDTO requestDTO) {
        if (requestDTO.getReportId() == null || CollectionUtils.isEmpty(requestDTO.getCustomIntervals())) {
            return new Result<>().setCode(ResultCode.FAIL.getValue()).setMessage("reportId和customIntervals不能为空");
        }
        // 遍历需要刷新的统计配置
        for (RefreshReportRequestDTO.CustomIntervalConfigDTO configDTO : requestDTO.getCustomIntervals()) {
            try {
                refreshSingleStatistics(configDTO);
            } catch (Exception e) {
                return new Result<>().setCode(ResultCode.FAIL.getValue()).setMessage("刷新统计配置失败, statisticsId: " + configDTO.getStatisticsId() + ", 错误: " + e.getMessage());
            }
        }
        // 刷新报告文件并上传至fastdfs
        try {
            analysisReportService.uploadReportToFastDfs(requestDTO.getReportId());
        } catch (IOException e) {
            return new Result<>().setCode(ResultCode.FAIL.getValue()).setMessage("刷新报告文件并上传至fastdfs失败, taskId: " + requestDTO.getReportId() + ", 错误: " + e.getMessage());
        }
        return new Result<Boolean>().setCode(ResultCode.SUCCESS.getValue());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Boolean> saveIntervalTemplate(RefreshReportRequestDTO requestDTO, MarketingUserDetail user) {
        // 参数校验
        if (StringUtils.isEmpty(requestDTO.getTemplateName())) {
            return new Result<>().setCode(ResultCode.FAIL.getValue()).setMessage("模板名称不能为空");
        }
        if (CollectionUtils.isEmpty(requestDTO.getCustomIntervals())) {
            return new Result<>().setCode(ResultCode.FAIL.getValue()).setMessage("自定义区间配置不能为空");
        }

        try {
            // 检查模板名称是否重复
            if (isTemplateNameExists(requestDTO.getTemplateName())) {
                return new Result<>().setCode(ResultCode.FAIL.getValue()).setMessage("模板名称重复: " + requestDTO.getTemplateName());
            }

            // 保存配置主表
            ReportIntervalConfig config = createIntervalConfig(requestDTO, user);
            int configResult = reportIntervalConfigMapper.insertSelective(config);
            if (configResult == 0) {
                return new Result<>().setCode(ResultCode.FAIL.getValue()).setMessage("自定义区间配置保存失败: " + requestDTO.getTemplateName());
            }

            // 批量保存模型配置
            saveIntervalModels(config.getId(), requestDTO.getCustomIntervals());
            return new Result<Boolean>().setCode(ResultCode.SUCCESS.getValue());
        } catch (Exception e) {
            return new Result<>().setCode(ResultCode.FAIL.getValue()).setMessage("保存评分分布模板失败: " + e.getMessage());
        }
    }

    @Override
    public Result<List<IntervalTemplateVO>> getIntervalTemplate(String apiCode) {
        // 参数校验
        if (StringUtils.isEmpty(apiCode)) {
            return new Result<>().setCode(ResultCode.FAIL.getValue()).setMessage("apiCode不能为空");
        }
        List<String> apiCodes = new ArrayList<>(Arrays.asList(apiCode.split(",")));
        try {
            List<IntervalTemplateVO> list = new ArrayList<>();
            ReportIntervalConfigExample example = new ReportIntervalConfigExample();
            example.createCriteria().andApiCodeIn(apiCodes)
                    .andStatusEqualTo(Constants.DATA_VALID).andIsDelEqualTo(Constants.DATA_VALID);
            List<ReportIntervalConfig> reportIntervalConfigs = reportIntervalConfigMapper.selectByExample(example);

            for (ReportIntervalConfig config : reportIntervalConfigs) {

                IntervalTemplateVO intervalTemplateVO = new IntervalTemplateVO();
                intervalTemplateVO.setId(config.getId());
                intervalTemplateVO.setApiCode(config.getApiCode());
                intervalTemplateVO.setReportId(config.getReportId());
                intervalTemplateVO.setTemplateName(config.getTemplateName());
                intervalTemplateVO.setTemplateNumber(config.getTemplateNumber());

                ReportIntervalModelExample reportIntervalModelExample = new ReportIntervalModelExample();
                reportIntervalModelExample.createCriteria().andConfigIdEqualTo(config.getId()).andIsDelEqualTo(Constants.DATA_VALID);
                List<ReportIntervalModel> reportIntervalModels = reportIntervalModelMapper.selectByExample(reportIntervalModelExample);

                List<IntervalTemplateVO.IntervalModelsVO> intervalModelsVOS = getIntervalModelsVOS(reportIntervalModels);
                intervalTemplateVO.setIntervalModels(intervalModelsVOS);
                list.add(intervalTemplateVO);
            }
            return new Result<List<IntervalTemplateVO>>().setCode(ResultCode.SUCCESS.getValue()).setDate(list);
        }catch (Exception e){
            return new Result<>().setCode(ResultCode.FAIL.getValue()).setMessage("评分分布查询规则模板异常: " + e.getMessage());
        }
    }

    @Override
    public Result<String> getImageDistribution() {
        return new Result<String>().setCode(ResultCode.SUCCESS.getValue()).setDate(marketingCommonConfig.getImageDistribution());
    }

    private static List<IntervalTemplateVO.IntervalModelsVO> getIntervalModelsVOS(List<ReportIntervalModel> reportIntervalModels) {
        List<IntervalTemplateVO.IntervalModelsVO> intervalModelsVOS = new ArrayList<>();
        for (ReportIntervalModel reportIntervalModel : reportIntervalModels){
            IntervalTemplateVO.IntervalModelsVO intervalModelsVO = new IntervalTemplateVO.IntervalModelsVO();
            intervalModelsVO.setId(reportIntervalModel.getId());
            intervalModelsVO.setConfigId(reportIntervalModel.getConfigId());
            intervalModelsVO.setAxisType(reportIntervalModel.getAxisType());
            intervalModelsVO.setXModelName(reportIntervalModel.getxModelName());
            intervalModelsVO.setYModelName(reportIntervalModel.getyModelName());
            intervalModelsVO.setXIntervalList(reportIntervalModel.getxIntervalList());
            intervalModelsVO.setYIntervalList(reportIntervalModel.getyIntervalList());
            intervalModelsVO.setOrder(reportIntervalModel.getOrder());
            intervalModelsVOS.add(intervalModelsVO);
        }
        return intervalModelsVOS;
    }

    /**
     * 检查模板名称是否存在
     */
    private boolean isTemplateNameExists(String templateName) {
        ReportIntervalConfigExample example = new ReportIntervalConfigExample();
        example.createCriteria()
                .andTemplateNameEqualTo(templateName)
                .andStatusEqualTo(Constants.DATA_VALID)
                .andIsDelEqualTo(Constants.DATA_VALID);
        return reportIntervalConfigMapper.countByExample(example) > 0;
    }

    /**
     * 创建区间配置对象
     */
    private ReportIntervalConfig createIntervalConfig(RefreshReportRequestDTO requestDTO, MarketingUserDetail user) {
        ReportIntervalConfig config = new ReportIntervalConfig();
        config.setApiCode(requestDTO.getApiCode());
        config.setReportId(requestDTO.getReportId());
        config.setTemplateName(requestDTO.getTemplateName());
        config.setTemplateNumber(generateTemplateNumber(requestDTO.getApiCode()));
        config.setOptUserId(Long.valueOf(user.getId()));
        config.setOptUserName(user.getUserName());
        config.setStatus(Constants.DATA_VALID);
        config.setIsDel(Constants.DATA_VALID);
        config.setCreateTime(new Date());
        config.setUpdateTime(new Date());
        return config;
    }

    /**
     * 生成模板编号
     */
    String generateTemplateNumber(String apiCode) {
        String yyyyMMdd = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String key = RedisKeyConstant.intervalNumber.concat(":").concat(yyyyMMdd);
        Long incr = redisChgService.incr(key);
        redisChgService.expire(key, getKeyExpiration());
        String s = incr.toString();
        int length = s.length();
        for (int i = 3; i > length; i--) {
            s = "0" + s;
        }
        return yyyyMMdd.concat("_").concat(apiCode).concat("_").concat(s);
    }

    /**
     * 获取当前时间到第二天凌晨的秒
     *
     */
    private int getKeyExpiration() {
        final LocalDateTime now = LocalDateTime.now();
        // 当前毫秒数
        long l = now.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        LocalDateTime localDateTime = now.plusDays(1);
        // 第二天凌晨毫秒数
        long l1 = localDateTime.toLocalDate().atStartOfDay().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        return (int) (l1 - l) / 1000;
    }

    /**
     * 批量保存区间模型配置
     */
    private void saveIntervalModels(Long configId, List<RefreshReportRequestDTO.CustomIntervalConfigDTO> customIntervals) {
        List<ReportIntervalModel> list = new ArrayList<>();
        for (RefreshReportRequestDTO.CustomIntervalConfigDTO dto : customIntervals) {
            ReportIntervalModel model = createIntervalModel(configId, dto);
            list.add(model);
        }
        int i = reportIntervalModelMapper.batchSaveIntervalModel(list);
        if (i == 0) {
            throw new RuntimeException("区间模型配置保存失败");
        }
    }

    /**
     * 创建区间模型对象
     */
    private ReportIntervalModel createIntervalModel(Long configId, RefreshReportRequestDTO.CustomIntervalConfigDTO dto) {
        ReportIntervalModel model = new ReportIntervalModel();
        model.setConfigId(configId);
        model.setAxisType(String.valueOf(dto.getReportScoreType()));
        model.setxModelName(dto.getFieldX());
        model.setyModelName(dto.getFieldY());
        model.setxIntervalList(JSON.toJSONString(dto.getXIntervalList()));
        model.setyIntervalList(dto.getReportScoreType().equals(2) ? JSON.toJSONString(dto.getYIntervalList()) : null);
        model.setOrder(dto.getOrder() != null ? String.valueOf(dto.getOrder()) : "1");
        model.setIsDel(Constants.DATA_VALID);
        model.setCreateTime(new Date());
        model.setUpdateTime(new Date());
        return model;
    }

    /**
     * 刷新单个统计配置
     */
    public void refreshSingleStatistics(RefreshReportRequestDTO.CustomIntervalConfigDTO customConfig) throws Exception {
        Long statisticsId = customConfig.getStatisticsId();

        // 先删除旧的统计详情数据
        ScoreStatisticsDetailExample example = new ScoreStatisticsDetailExample();
        example.createCriteria().andStatisticsIdEqualTo(statisticsId).andIsDelEqualTo(Constants.DATA_VALID);

        ScoreStatisticsDetail scoreStatisticsDetail = new ScoreStatisticsDetail();
        scoreStatisticsDetail.setIsDel(Constants.DATA_DEL);
        scoreStatisticsDetail.setUpdateTime(new Date());
        scoreStatisticsDetailMapper.updateByExampleSelective(scoreStatisticsDetail, example);

        // 更新统计配置的区间范围
        ReportStatisticsScore updateScore = new ReportStatisticsScore();
        updateScore.setId(statisticsId);
        updateScore.setFieldXRange(JSON.toJSONString(customConfig.getXIntervalList()));
        if (customConfig.getReportScoreType().equals(2)) {
            updateScore.setFieldYRange(JSON.toJSONString(customConfig.getYIntervalList()));
        }
        updateScore.setUpdateTime(new Date());
        reportStatisticsScoreMapper.updateByPrimaryKeySelective(updateScore);

        // 重新执行统计计算
        ReportStatisticsScore refreshedScore = reportStatisticsScoreMapper.selectByPrimaryKey(statisticsId);
        if (refreshedScore.getReportScoreType().equals(1)) {
            // 单模型自定义区间统计
            executeCustomIntervalCount(refreshedScore, customConfig.getXIntervalList(), null, "刷新单模型");
        } else {
            // 多模型自定义区间统计
            executeCustomIntervalCount(refreshedScore, customConfig.getXIntervalList(), customConfig.getYIntervalList(), "刷新多模型");
        }
        // 更新统计状态为成功
        updateReportScore(refreshedScore, 1, null);
    }

    /**
     * 执行自定义区间统计（统一的统计逻辑）
     */
    private void executeCustomIntervalCount(ReportStatisticsScore statisticsScore, 
                                           List<IntervalRangeDTO> xIntervalList, 
                                           List<IntervalRangeDTO> yIntervalList,
                                           String logPrefix) {
        List<String> batchNumberList = customIntervalStatistics.getBatchNumberKey(statisticsScore);
        customIntervalStatistics.executeCustomIntervalCount(
                statisticsScore.getId(), 
                statisticsScore.getFieldX(), 
                statisticsScore.getFieldY(), 
                batchNumberList, 
                xIntervalList, 
                yIntervalList, 
                logPrefix);
    }

    private void updateReportScore(ReportStatisticsScore statisticsScore, Integer status, String errorDesc) {
        statisticsScore.setStatus(status);
        statisticsScore.setUpdateTime(new Date());
        statisticsScore.setStatisticsDesc(errorDesc);
        reportStatisticsScoreMapper.updateByPrimaryKey(statisticsScore);
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

    /**
     * 更新报表统计记录
     * @param param
     * @return
     */
    @Override
    public Boolean updateReportRecords(BiReportStatisticTransferParam param) {
        String reportDate = param.getReportDate();
        if (StringUtils.isEmpty(reportDate)) {
            reportDate = LocalDate.now().toString();
        }
        String reportTypeName = param.getReportTypeName();
        Integer reportType = BiReportTypeEnum.getEnumByTypeName(reportTypeName).getType();
        String reportDateStr = "%" + reportDate;

        // 众安上线后刷记录
        ReportTaskExample example = new ReportTaskExample();
        example.createCriteria().andReportNameLike(reportDateStr).andReportTypeEqualTo(reportType);
        List<ReportTask> reportTasks = reportTaskMapper.selectByExample(example);
        if (ObjectUtil.isEmpty(reportTasks)) {
            String string = "23:59:59.999";
            String dateNow = reportDate + " " + string;
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
            LocalDateTime localDateTime = LocalDateTime.parse(dateNow, formatter).plusDays(1);
            reportStatisticService.action(localDateTime, reportType);
            return true;
        }

        // 更新逻辑
        try {
            ReportTaskExample reportTaskExample = new ReportTaskExample();
            reportTaskExample.createCriteria()
                    .andReportNameLike(reportDateStr)
                    .andReportTypeEqualTo(reportType);

            ReportTask reportTask = new ReportTask();
            reportTask.setStatus(0);
            reportTask.setUpdateTime(new Date());
            boolean a = reportTaskMapper.updateByExampleSelective(reportTask, reportTaskExample) == 1;
            ReportStatisticTransferExample reportStatisticTransferExample = new ReportStatisticTransferExample();

            switch (reportType) {
                case 12:
                    if (ObjectUtil.isNotEmpty(reportDate)) {
                        reportStatisticTransferExample.createCriteria()
                                .andReportDateEqualTo(reportDate)
                                .andReportStatusEqualTo("0")
                                .andReportTypeEqualTo(ReportTaskTypeEnum.BUSINESS_ANALYSIS_ONE_TYPE.getValue().toString());
                        ReportStatisticTransfer statisticTransfer = new ReportStatisticTransfer();
                        statisticTransfer.setReportStatus("3");
                        statisticTransfer.setUpdateTime(new Date());
                        boolean b = reportStatisticTransferMapper.updateByExampleSelective(statisticTransfer,
                                reportStatisticTransferExample) == 1;
                        if (a && b) {
                            return true;
                        }
                    }
                    break;
                case 13:
                    if (ObjectUtil.isNotEmpty(reportDate)) {
                        reportStatisticTransferExample.createCriteria()
                                .andReportDateEqualTo(reportDate)
                                .andReportStatusEqualTo("0")
                                .andReportTypeEqualTo(ReportTaskTypeEnum.BUSINESS_ANALYSIS_SEVEN_TYPE.getValue().toString());
                        ReportStatisticTransfer statisticTransfer = new ReportStatisticTransfer();
                        statisticTransfer.setReportStatus("3");
                        statisticTransfer.setUpdateTime(new Date());
                        boolean b = reportStatisticTransferMapper.updateByExampleSelective(statisticTransfer,
                                reportStatisticTransferExample) == 1;
                        if (a && b) {
                            return true;
                        }
                    }
                    break;
                case 14:
                    if (ObjectUtil.isNotEmpty(reportDate)) {
                        reportStatisticTransferExample.createCriteria()
                                .andReportDateEqualTo(reportDate)
                                .andReportStatusEqualTo("0")
                                .andReportTypeEqualTo(ReportTaskTypeEnum.BUSINESS_ANALYSIS_EIGHT_TYPE.getValue().toString());
                        reportStatisticTransferExample.setOrderByClause("create_time desc");
                        ReportStatisticTransfer statisticTransfer = new ReportStatisticTransfer();
                        statisticTransfer.setReportStatus("3");
                        statisticTransfer.setUpdateTime(new Date());
                        boolean b = reportStatisticTransferMapper.updateByExampleSelective(statisticTransfer,
                                reportStatisticTransferExample) == 1;
                        if (a && b) {
                            return true;
                        }
                    }
                    break;
                default:
                    break;
            }
            return false;
        } catch (Exception e) {
            log.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.YINGXIAO_SERVICEERROR.getCode(),
                    "更新报表统计记录异常"), e);
            return false;
        }

    }
}
