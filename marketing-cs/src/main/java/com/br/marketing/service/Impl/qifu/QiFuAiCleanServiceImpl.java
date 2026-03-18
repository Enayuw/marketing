package com.br.marketing.service.Impl.qifu;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.br.common.log.AlertLog;
import com.br.marketing.client.qifu.callrealtime.CallRealTimeDTO;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.dto.MarketingPreUserDTO;
import com.br.marketing.dto.MarketingPreUserDetailDTO;
import com.br.marketing.dto.qifu.UpLoadCleanDTO;
import com.br.marketing.entity.BQifuUploadDataOriginal;
import com.br.marketing.mapper.BQifuUploadDataOriginalMapper;
import com.br.marketing.service.Impl.qifu.enums.QiFuProcessStatusEnum;
import com.br.marketing.service.PushInfoService;
import com.br.marketing.service.qifu.QiFuAiCleanService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.marketingkit.tracking.model.indicator.DataFlowDirection;
import com.marketingkit.tracking.service.TrackingService;
import com.marketingkit.tracking.util.TrackingContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.ListUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 奇富AI清洗Service实现（从b_qifu_upload_data_original表查询数据）
 */
@Slf4j
@Service
public class QiFuAiCleanServiceImpl implements QiFuAiCleanService {

    /**
     * 分页大小
     */
    private static final int PAGE_SIZE = 2000;

    @Resource
    private BQifuUploadDataOriginalMapper bQifuUploadDataOriginalMapper;

    @Resource
    private PushInfoService pushInfoService;

    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Resource
    private TrackingService trackingService;

    @Override
    public void aiCleanProcessFromOriginal() {
        log.warn("奇富ai清洗开始，查询b_qifu_upload_data_original数据");

        // 获取日期
        String todayDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        // 直接处理今天所有未处理的数据
        processDataForClean(todayDate);
    }

    /**
     * 处理清洗数据（基于今天的数据，分页查询并处理）
     */
    private void processDataForClean(String todayDate) {
        Long indexId = null;
        boolean hasMore = true;
        AtomicLong total = new AtomicLong(0L);
        String apiCode = "";
        while (hasMore) {
            // 查询今天需要清洗的数据（status=0 未处理）
            List<BQifuUploadDataOriginal> dataList = bQifuUploadDataOriginalMapper.selectDataForCleanByDate(
                    todayDate, PAGE_SIZE, indexId);
            if (dataList == null || dataList.isEmpty()) {
                log.warn("今天 {} 没有待处理的数据", todayDate);
                hasMore = false;
                break;
            }

            indexId = dataList.get(dataList.size() - 1).getId();

            try {
                total.addAndGet(dataList.size());
                apiCode = dataList.get(0).getApiCode();
            } catch (Exception ex) {
                log.warn(
                        AlertLog.buildWarnMessage(
                                AlarmSendCodeEnum.TRACKING_POINT_SERVICEERROR.getCode()
                                , ex.getMessage()
                                , "埋点异常")
                        , ex);
            }

            // 数据清洗：设置 status=处理中,批量更新数据库
            updateStatus(dataList);

            // 调用上传接口
            pushProcessForOriginal(dataList, "3");

            if (dataList.size() < PAGE_SIZE) {
                hasMore = false;
            }
        }

        try {
            if(total.get() > 0){
                JSONObject condition = new JSONObject();
                condition.put("todayDate", todayDate);
                trackingService.trackBusinessLog(DataFlowDirection.OUT
                        , apiCode
                        , "奇富ai清洗"
                        , "b_qifu_upload_data_original"
                        , JSON.toJSONString(condition)
                        , total.get()
                        , TrackingContext.generateBatchId());
            }
        } catch (Exception ex) {
            log.warn(
                    AlertLog.buildWarnMessage(
                            AlarmSendCodeEnum.TRACKING_POINT_SERVICEERROR.getCode()
                            , ex.getMessage()
                            , "埋点异常")
                    , ex);
        }

    }

    public void updateStatus(List<BQifuUploadDataOriginal> dataList) {
        // 数据清洗：设置 status=处理中
        List<BQifuUploadDataOriginal> updateRecords = dataList.stream()
                .map(record -> {
                    BQifuUploadDataOriginal updateRecord = new BQifuUploadDataOriginal();
                    updateRecord.setId(record.getId());
                    updateRecord.setStatus(QiFuProcessStatusEnum.PROCESSING.getCode());
                    return updateRecord;
                })
                .collect(Collectors.toList());

        // 批量更新status
        batchUpdateStatus(updateRecords);
    }

    /**
     * 批量更新status
     */
    private void batchUpdateStatus(List<BQifuUploadDataOriginal> records) {
        if (records == null || records.isEmpty()) {
            return;
        }

        // 分批更新，每批100条
        int batchSize = 100;
        List<List<BQifuUploadDataOriginal>> batches = ListUtils.partition(records, batchSize);
        for (List<BQifuUploadDataOriginal> batch : batches) {
            bQifuUploadDataOriginalMapper.batchUpdateStatus(batch);
        }
    }

    /**
     * 处理BQifuUploadDataOriginal数据的上传
     */
    @Override
    public void pushProcessForOriginal(List<BQifuUploadDataOriginal> dataList, String operateType) {
        if (CollectionUtils.isEmpty(dataList)) {
            return;
        }

        // 按 apiCode 分组
        Map<String, List<BQifuUploadDataOriginal>> apiCodeGroupMap = dataList.stream()
                .collect(Collectors.groupingBy(BQifuUploadDataOriginal::getApiCode));


        // 对每个 apiCode 组进行批量推送
        for (Map.Entry<String, List<BQifuUploadDataOriginal>> entry : apiCodeGroupMap.entrySet()) {
            String apiCode = entry.getKey();
            List<BQifuUploadDataOriginal> groupDataList = entry.getValue();

            if (CollectionUtils.isEmpty(groupDataList)) {
                continue;
            }

            // 按 batchNo 和 flowNo 分组
            Map<String, List<BQifuUploadDataOriginal>> batchFlowGroupMap = groupDataList.stream()
                    .collect(Collectors.groupingBy(record -> {
                        String batchNo = record.getBatchNo() != null ? record.getBatchNo() : "";
                        String flowNo = record.getFlowNo() != null ? record.getFlowNo() : "";
                        return batchNo + "|" + flowNo;
                    }));

            // 对每个 batchNo 和 flowNo 分组进行处理
            for (Map.Entry<String, List<BQifuUploadDataOriginal>> batchFlowEntry : batchFlowGroupMap.entrySet()) {
                List<BQifuUploadDataOriginal> batchFlowDataList = batchFlowEntry.getValue();

                if (CollectionUtils.isEmpty(batchFlowDataList)) {
                    continue;
                }

                // 获取 batchNo 和 flowNo
                BQifuUploadDataOriginal firstRecord = batchFlowDataList.get(0);
                String batchNo = firstRecord.getBatchNo();
                String flowNo = firstRecord.getFlowNo();

                // 构建批量推送对象
                Result<MarketingPreUserDTO> result = buildBatchPushDtoFromOriginal(batchFlowDataList, operateType, batchNo, flowNo);
                if (!ResultCode.SUCCESS.getValue().equals(result.getCode())) {
                    log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.QIFUAI_SERVICEERROR.getCode(),
                            "奇富360ai批量构建推送对象失败，apiCode: " + apiCode +
                                    ", batchNo: " + batchNo + ", flowNo: " + flowNo + "，错误信息: " + result.getMessage()));
                    continue;
                }

                // 推送
                UpLoadCleanDTO upLoadCleanDTO = new UpLoadCleanDTO();
                upLoadCleanDTO.setApiCode(apiCode);
                upLoadCleanDTO.setJsonData(JSON.toJSONString(result.getData()));
                Result<Boolean> pushResult = pushInfoService.pushUploadOfCleanRetry(upLoadCleanDTO, null);

                // 推送成功后，更新状态为"处理完成"
                if (pushResult != null && ResultCode.SUCCESS.getValue().equals(pushResult.getCode())) {
                    List<Long> idList = batchFlowDataList.stream().map(BQifuUploadDataOriginal::getId).collect(Collectors.toList());
                    updateStatusToCompleted(idList);
                    log.info("奇富360ai批量推送成功，apiCode: {}，batchNo: {}，flowNo: {}，数据条数: {}", apiCode, batchNo, flowNo, batchFlowDataList.size());
                } else {
                    String errorMsg = pushResult != null ? pushResult.getMessage() : "推送失败";
                    log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.QIFUAI_SERVICEERROR.getCode(),
                            "奇富360ai批量推送失败，apiCode: " + apiCode + ", batchNo: " + batchNo + ", flowNo: " + flowNo + "，错误信息: " + errorMsg));
                }
            }
        }
    }

    /**
     * 批量更新状态为"处理完成"
     */
    private void updateStatusToCompleted(List<Long> idList) {
        if (CollectionUtils.isEmpty(idList)) {
            return;
        }

        List<BQifuUploadDataOriginal> updateRecords = idList.stream()
                .map(id -> {
                    BQifuUploadDataOriginal updateRecord = new BQifuUploadDataOriginal();
                    updateRecord.setId(id);
                    updateRecord.setStatus(QiFuProcessStatusEnum.COMPLETED.getCode());
                    return updateRecord;
                })
                .collect(Collectors.toList());

        // 批量更新status
        batchUpdateStatus(updateRecords);
    }

    @Override
    public List<MarketingPreUserDetailDTO> buildListFromCallRealTimeDetails(List<CallRealTimeDTO> dataDetails) {
        if (CollectionUtils.isEmpty(dataDetails)) {
            return new ArrayList<>();
        }
        List<MarketingPreUserDetailDTO> list = new ArrayList<>();
        for (CallRealTimeDTO dto : dataDetails) {
            JSONObject detailJson = new JSONObject();
            detailJson.put("serialNo", dto.getSerialNo());
            detailJson.put("surname", dto.getSurname());
            detailJson.put("gender", dto.getGender());

            JSONObject extendJsonObject = new JSONObject();
            extendJsonObject.put("increaseCustomer", dto.getIncreaseCustomer());
            extendJsonObject.put("temporaryIncrease", dto.getTemporaryIncrease());
            extendJsonObject.put("rTotalAvailableAmt", dto.getRTotalAvailableAmt());
            extendJsonObject.put("rTaLastAdjustmentAmount", dto.getRTaLastAdjustmentAmount());
            extendJsonObject.put("rTaTemporaryAmountExpireDate", dto.getRTaTemporaryAmountExpireDate());
            extendJsonObject.put("rCouponInfo", dto.getRCouponInfo());
            extendJsonObject.put("pricingValidPeriod", dto.getPricingValidPeriod());
            extendJsonObject.put("pricingDiscount", dto.getPricingDiscount());
            extendJsonObject.put("pricingExpireDays", dto.getPricingExpireDays());

            JSONObject reserField1 = new JSONObject();
            MarketingPreUserDetailDTO detailDTO = buildListDto(detailJson, reserField1, extendJsonObject);
            list.add(detailDTO);
        }
        return list;
    }

    /**
     * 批量构建推送对象（从原始表）- 将同一apiCode的多条数据组装到一个MarketingPreUserDTO中
     */
    private Result<MarketingPreUserDTO> buildBatchPushDtoFromOriginal(List<BQifuUploadDataOriginal> dataList,
                                                                      String operateType, String batchNo, String flowNo) {
        Result<MarketingPreUserDTO> res = new Result<>();

        JSONObject qifuAiCleanConfig = marketingCommonConfig.getQifuAiCleanConfig();

        if (CollectionUtils.isEmpty(dataList)) {
            return res.setCode(ResultCode.FAIL.getValue()).setMessage("数据列表为空");
        }
        MarketingPreUserDTO marketingPreUserDTO = new MarketingPreUserDTO();
        List<MarketingPreUserDetailDTO> list = new ArrayList<>();
        try {
            // 遍历每条记录，构建 MarketingPreUserDetailDTO
            for (BQifuUploadDataOriginal record : dataList) {
                String batch;
                String strategyCode = "";
                String strategyName = "";
                String userType;
                String finalStrategyCode;
                String finalStrategyName;

                boolean isRealTime = (record.getIsReal() == 1);
                if (isRealTime) {
                    // 实时推送逻辑
                    LocalDate today = LocalDate.now();
                    String currentDate = today.format(DateTimeFormatter.ofPattern("yyyyMMdd"));

                    batch = currentDate + "_" + record.getApiCode() + "_实时推送";
                    strategyCode = qifuAiCleanConfig.getString("strategyCode");
                    strategyName = qifuAiCleanConfig.getString("strategyName");

                    // 处理templateNo，提取userType
                    String templateStr = record.getTemplateNo();
                    if (templateStr != null && templateStr.length() > 12) {
                        userType = templateStr.substring(0,templateStr.length() - 12);
                    } else {
                        userType = templateStr;
                    }

                    finalStrategyCode = strategyCode;
                    finalStrategyName = strategyName;

                } else {
                    // 非实时推送逻辑
                    batch = record.getReceiveDate().replaceAll("-", "")
                            .concat("_")
                            .concat(record.getApiCode());
                    userType = record.getUserType();

                    // 处理templateNo
                    String templateStr = record.getTemplateNo();
                    if (templateStr != null && templateStr.length() > 12) {
                        strategyCode = templateStr.substring(templateStr.length() - 12);
                        strategyName = strategyCode;
                    } else {
                        strategyCode = "";
                        strategyName = "";
                    }

                    // 非实时推送根据配置决定是否使用strategyCode
                    boolean flag = marketingCommonConfig.getQifuAiCleanStrategyCodeFlag();
                    if (flag) {
                        finalStrategyCode = "";
                        finalStrategyName = "";
                    } else {
                        finalStrategyCode = strategyCode;
                        finalStrategyName = strategyName;
                    }
                }

                // 设置公共的extendKey
                JSONObject extendKey = new JSONObject();
                extendKey.put("batchName", batch);
                extendKey.put("batchNumber", batch);
                extendKey.put("strategyCode", finalStrategyCode);
                extendKey.put("strategyName", finalStrategyName);
                extendKey.put("userType", userType);
                extendKey.put("flowNo", flowNo);

                if (StringUtils.isNotBlank(record.getOperateScene())) {
                    extendKey.put("customName", record.getOperateScene());
                    extendKey.put("customNameType", record.getOperateScene());
                }
                if (StringUtils.isNotBlank(record.getCallTimeRange())) {
                    extendKey.put("callTimeRange", record.getCallTimeRange());
                }
                if (StringUtils.isNotBlank(record.getCallType())) {
                    extendKey.put("callType", record.getCallType());
                }

                // 设置taskId和requestId
                String requestId = String.format("%s_%s_%s", batch, flowNo, System.currentTimeMillis());
                marketingPreUserDTO.setTaskId(batchNo);
                marketingPreUserDTO.setRequestId(requestId);
                String extend = record.getExtend();

                // 解析extend字段
                JSONObject extendJsonObject = null;
                if (StringUtils.isNotBlank(extend)) {
                    try {
                        extendJsonObject = JSON.parseObject(extend);
                    } catch (Exception e) {
                        log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.QIFUAI_SERVICEERROR.getCode(),
                                "extend解析错误: " + e.getMessage()), e);
                    }
                }

                // 构建单条数据
                JSONObject reserField1 = new JSONObject();
                extendKey.keySet().forEach(t -> reserField1.put(t, extendKey.get(t)));

                JSONObject detailJson = new JSONObject();
                detailJson.put("serialNo", record.getSerialNo());
                detailJson.put("phoneNoMd5", record.getPhoneNoMd5());
                detailJson.put("surname", record.getSurname());
                detailJson.put("gender", record.getGender());
                detailJson.put("operateType", operateType);
                if (!StringUtils.isEmpty(record.getEventType())) {
                    detailJson.put("eventType", record.getEventType());
                }

                MarketingPreUserDetailDTO marketingPreUserDetailDTO = buildListDto(detailJson, reserField1, extendJsonObject);
                list.add(marketingPreUserDetailDTO);
            }

            marketingPreUserDTO.setDataItems(list);

            return res.setCode(ResultCode.SUCCESS.getValue()).setDate(marketingPreUserDTO);
        } catch (Exception ex) {
            log.error(AlertLog.buildErrorMessage(AlarmSendCodeEnum.QIFUAI_SERVICEERROR.getCode(),
                    "奇富360ai批量构建推送对象异常: " + ex.getMessage()), ex);
            return res.setCode(ResultCode.FAIL.getValue()).setMessage(ex.getMessage());
        }
    }

    /**
     * 构建列表DTO
     */
    private MarketingPreUserDetailDTO buildListDto(JSONObject o1, JSONObject reserField1, JSONObject extendJsonObject) {
        MarketingPreUserDetailDTO marketingPreUserDetailDTO = new MarketingPreUserDetailDTO();
        for (String s : o1.keySet()) {
            switch (s) {
                case "serialNo":
                    marketingPreUserDetailDTO.setCustNum(o1.getString(s));
                    break;
                case "phoneNoMd5":
                    marketingPreUserDetailDTO.setCell(o1.getString(s));
                    break;
                case "operateType":
                    marketingPreUserDetailDTO.setOperateType(o1.getString(s));
                    break;
                case "surname":
                    reserField1.put("firstName", o1.getString(s));
                    break;
                case "gender":
                    String genderValue = o1.getString(s);
                    if ("F".equals(genderValue)) {
                        reserField1.put(s, "0");
                    } else if ("M".equals(genderValue)) {
                        reserField1.put(s, "1");
                    }
                    break;
                default:
                    reserField1.put(s, o1.getString(s));
                    break;
            }
        }
        buildNewListDto(extendJsonObject, reserField1);
        marketingPreUserDetailDTO.setReserveField1(JSONArray.toJSONString(reserField1));
        return marketingPreUserDetailDTO;
    }

    /**
     * 构建新的列表DTO（处理extend字段）
     */
    private void buildNewListDto(JSONObject extendJsonObject, JSONObject reserField1) {
        if (extendJsonObject == null || extendJsonObject.isEmpty()) {
            return;
        }

        if (reserField1 == null) {
            reserField1 = new JSONObject();
        }

        for (String key : extendJsonObject.keySet()) {
            Object valueObj = extendJsonObject.get(key);
            String value = valueObj != null ? valueObj.toString() : "";

            switch (key) {
                case "increaseCustomer":
                    reserField1.put("increaseCustomer", mapYesNo(value));
                    break;
                case "temporaryIncrease":
                    reserField1.put("temporaryIncrease", mapYesNo(value));
                    break;
                case "rTotalAvailableAmt":
                    reserField1.put("rTotalAvailableAmt", mapNumberToRange(value));
                    break;
                case "rTaLastAdjustmentAmount":
                    reserField1.put("rTaLastAdjustmentAmount", mapNumberToRange(value));
                    break;
                case "rTaTemporaryAmountExpireDate":
                    reserField1.put("rTaTemporaryAmountExpireDate", mapDateString(value));
                    break;
                case "rCouponInfo":
                    reserField1.put("rCouponInfo", value);
                    break;
                case "pricingValidPeriod":
                    reserField1.put("pricingValidPeriod", value);
                    break;
                case "pricingDiscount":
                    reserField1.put("pricingDiscount", value);
                    break;
                case "pricingExpireDays":
                    reserField1.put("pricingExpireDays", value);
                    break;
                case "productType":
                    reserField1.put("productType", value);
                    break;
                default:
                    break;
            }
        }

        String highAmountys = "highAmountys";
        String lowAmountys = "lowAmountys";
        String rTotalAvailableAmt = reserField1.getString("rTotalAvailableAmt");
        String rTaLastAdjustmentAmount = reserField1.getString("rTaLastAdjustmentAmount");
        String rTaTemporaryAmountExpireDate = reserField1.getString("rTaTemporaryAmountExpireDate");
        String rCouponInfo = reserField1.getString("rCouponInfo");
        // 计算新的字段值
        String oldLowAmountys = getAmount(null, lowAmountys, rTaLastAdjustmentAmount);
        String newHighAmountys = getAmount(highAmountys, null, rTotalAvailableAmt);
        String changeAmountys = calculateDifference(newHighAmountys, oldLowAmountys);
        String remainDayys = calculateDaysDifference(rTaTemporaryAmountExpireDate);
        String changeIncrease = calculateIncreaseRate(newHighAmountys, oldLowAmountys);
        String couponDerived = processCouponInfo(rCouponInfo);

        reserField1.put("highAmount_derived", newHighAmountys);
        reserField1.put("lowAmount_derived", oldLowAmountys);
        reserField1.put("changeAmount_derived", changeAmountys);
        reserField1.put("remainDayys_derived", remainDayys);
        reserField1.put("changeIncrease_derived", changeIncrease);
        reserField1.put("coupon_derived", couponDerived);
    }

    /**
     * 处理优惠券信息
     */
    private String processCouponInfo(String rCouponInfo) {
        if (StringUtils.isBlank(rCouponInfo)) {
            return "";
        }

        try {
            JSONArray coupons = JSON.parseArray(rCouponInfo);
            if (coupons == null || coupons.isEmpty()) {
                return "";
            }

            // 只保留清洗逻辑，取清洗后的第一个券
            String firstCouponName = coupons.getJSONObject(0).getString("couponName");
            return cleanCouponName(firstCouponName);
        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.QIFUAI_SERVICEERROR.getCode(),
                    "处理优惠券信息时发生错误，错误信息：" + e.getMessage()), e);
            return "";
        }
    }

    /**
     * 清洗券名称
     */
    private String cleanCouponName(String couponName) {
        if (StringUtils.isBlank(couponName)) {
            return "";
        }

        String[] keywordsToClean = {"智信", "超级会员", "专属"};
        String cleanedName = couponName;
        for (String keyword : keywordsToClean) {
            cleanedName = cleanedName.replace(keyword, "");
        }
        return cleanedName.trim();
    }

    /**
     * Y/N映射
     */
    private String mapYesNo(String input) {
        if (input == null || input.isEmpty()) {
            return "";
        }

        switch (input.toUpperCase()) {
            case "Y":
                return "是";
            case "N":
                return "否";
            default:
                return input;
        }
    }

    /**
     * 数字映射到范围
     */
    private String mapNumberToRange(String input) {
        if (input == null || input.isEmpty() || "0".equals(input)) {
            return "";
        }
        try {
            int num = Integer.parseInt(input);
            if (num < 0 || num > 1001) {
                log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.QIFUAI_SERVICEERROR.getCode(),
                        "奇富AI 额度枚举输入非法！"));
                return "";
            }

            Integer qiFuConfigNum = ObjectUtils.isEmpty(marketingCommonConfig.getQiFuConfigNum()) ? 1000 : marketingCommonConfig.getQiFuConfigNum();
            int lowerBound = (num - 1) * qiFuConfigNum;
            int upperBound = num * qiFuConfigNum;
            return "[" + lowerBound + " - " + upperBound + ")";
        } catch (NumberFormatException e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.QIFUAI_SERVICEERROR.getCode(),
                    "奇富AI 额度计算发生错误！错误信息：" + e.getMessage()), e);
            return "";
        }
    }

    /**
     * 日期字符串映射
     */
    private static final Pattern DATE_PATTERN = Pattern.compile("^(\\d{2})-(\\d{2})$");

    private String mapDateString(String input) {
        if (input == null || input.isEmpty()) {
            return "";
        }

        if ("noLimit".equalsIgnoreCase(input)) {
            return "noLimit";
        }

        Matcher matcher = DATE_PATTERN.matcher(input);
        if (matcher.matches()) {
            return input;
        }

        return "";
    }

    /**
     * 获取金额
     */
    private String getAmount(String highAmountys, String lowAmountys, String rTotalAvailableAmt) {
        if (rTotalAvailableAmt == null || rTotalAvailableAmt.isEmpty()) {
            return "";
        }

        rTotalAvailableAmt = rTotalAvailableAmt.replace("[", "").replace(")", "").replace(" ", "");
        String[] rangeParts = rTotalAvailableAmt.split("-");

        if (rangeParts.length != 2) {
            return "";
        }

        String leftValue = rangeParts[0];
        String rightValue = rangeParts[1];

        if (highAmountys != null) {
            return rightValue;
        } else if (lowAmountys != null) {
            return leftValue;
        }

        return "";
    }

    /**
     * 计算差值
     */
    private String calculateDifference(String highAmountys, String lowAmountys) {
        try {
            if (highAmountys == null || highAmountys.isEmpty() || lowAmountys == null || lowAmountys.isEmpty()) {
                return "";
            }
            int high = Integer.parseInt(highAmountys);
            int low = Integer.parseInt(lowAmountys);
            int result = high - low;

            return result > 0 ? String.valueOf(result) : "0";
        } catch (NumberFormatException e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.QIFUAI_SERVICEERROR.getCode(),
                    "奇富AI提升额度计算发生错误！错误信息：" + e.getMessage()), e);
            return "";
        }
    }

    /**
     * 计算天数差值
     */
    private String calculateDaysDifference(String rTaTemporaryAmountExpireDate) {
        if ("noLimit".equalsIgnoreCase(rTaTemporaryAmountExpireDate) ||
                (rTaTemporaryAmountExpireDate == null || rTaTemporaryAmountExpireDate.isEmpty())) {
            return "9999";
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate today = LocalDate.now();

        try {
            String dateWithYear = today.getYear() + "-" + rTaTemporaryAmountExpireDate;
            LocalDate expireDate = LocalDate.parse(dateWithYear, formatter);

            if (expireDate.isBefore(today)) {
                expireDate = expireDate.plusYears(1);
            }

            return String.valueOf(ChronoUnit.DAYS.between(today, expireDate));
        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.QIFUAI_SERVICEERROR.getCode(),
                    "奇富AI额度到期日期计算发生错误！错误信息：" + e.getMessage()), e);
            return "";
        }
    }

    /**
     * 计算增长率
     */
    private String calculateIncreaseRate(String highAmountys, String lowAmountys) {
        if (highAmountys == null || highAmountys.isEmpty() || lowAmountys == null || lowAmountys.isEmpty() || lowAmountys.equals("0")) {
            return "";
        }

        try {
            BigDecimal high = new BigDecimal(highAmountys);
            BigDecimal low = new BigDecimal(lowAmountys);

            BigDecimal rate = high.divide(low, 10, BigDecimal.ROUND_HALF_UP)
                    .subtract(BigDecimal.ONE)
                    .multiply(BigDecimal.valueOf(100));

            int result = (int) Math.ceil(rate.doubleValue());

            return String.valueOf(result);
        } catch (NumberFormatException e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.QIFUAI_SERVICEERROR.getCode(),
                    "奇富AI提额幅度计算发生错误！错误信息：" + e.getMessage()), e);
            return "";
        }
    }

}
