package com.br.marketing.service.Impl.qifu;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.br.common.log.AlertLog;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.dto.CouponInfo;
import com.br.marketing.dto.MarketingPreUserDTO;
import com.br.marketing.dto.MarketingPreUserDetailDTO;
import com.br.marketing.dto.qifu.UpLoadCleanDTO;
import com.br.marketing.entity.DrsCustomizeUploadData;
import com.br.marketing.entity.Log360ai;
import com.br.marketing.entity.Log360aiExample;
import com.br.marketing.mapper.DrsCustomizeUploadDataMapper;
import com.br.marketing.mapper.Log360aiMapper;
import com.br.marketing.service.Impl.qifu.valobj.QiFuCleanStatusEnum;
import com.br.marketing.service.Impl.qifu.valobj.QiFuSyncStatusEnum;
import com.br.marketing.service.Impl.qifu.enums.CouponType;
import com.br.marketing.service.PushInfoService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.util.ThreadPoolAdjustmentUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 已废弃
 */
@Service
@Slf4j
public class QiFuServiceImpl implements IQiFuService {

    @Resource
    DrsCustomizeUploadDataMapper drsCustomizeUploadDataMapper;

    @Resource
    MarketingCommonConfig marketingCommonConfig;

    @Resource
    Log360aiMapper log360aiMapper;

    @Resource
    PushInfoService pushInfoService;

    @Override
    public void aiCleanProcess() {
        // 原有流程：从DrsCustomizeUploadData表查询数据
        JSONObject qifuAiCleanConfig = marketingCommonConfig.getQifuAiCleanConfig();
        String tcId = getValueOfJson(qifuAiCleanConfig, "tCid", "");
        List<String> apiCodes = Arrays.asList(getValueOfJson(qifuAiCleanConfig, "cleanApiCode", "3700226").split(","));
        String dataTimeMark = getValueOfJson(qifuAiCleanConfig, "dataTime", "-1");
        LocalDate now = LocalDate.now();
        String nowDay = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String yesterDay = now.minusDays(1L).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        List<String> receiveDates = new ArrayList<>();
        if ("-1".equals(dataTimeMark)) {
            receiveDates.add(yesterDay);
            receiveDates.add(nowDay);
        } else if ("1".equals(dataTimeMark)) {
            receiveDates.add(nowDay);
        } else {
            receiveDates.add(dataTimeMark);
        }
        Integer pageSize = Integer.valueOf(getValueOfJson(qifuAiCleanConfig, "pageSize", "10"));
        Integer threadNum = Integer.valueOf(getValueOfJson(qifuAiCleanConfig, "threadNum", "1"));
        ThreadPoolExecutor threadPool = BrExecutors.getThreadPool(threadNum, threadNum, "qiAiClean", 200);
        Boolean actionMark = Boolean.TRUE;
        while (actionMark) {

            Boolean b = dynamicAction(threadPool);
            if (b) {
                actionMark = Boolean.FALSE;
                continue;
            }
            List<DrsCustomizeUploadData> dataOfNeedClean = drsCustomizeUploadDataMapper.getDataOfNeedClean(tcId, apiCodes, receiveDates, pageSize);
            if (dataOfNeedClean.size() <= 0) {
                actionMark = Boolean.FALSE;
                continue;
            }
            ArrayList<Long> ids = new ArrayList<>();
            StringBuilder insertLogSql = new StringBuilder();
            insertLogSql.append("insert into b_log_360ai ");
            insertLogSql.append("(data_id,status) ");
            insertLogSql.append("values ");
            for (DrsCustomizeUploadData drsCustomizeUploadData : dataOfNeedClean) {
                insertLogSql.append(String.format("(%d,%d),", drsCustomizeUploadData.getId(), QiFuCleanStatusEnum.RUNNING.getValue()));
                ids.add(drsCustomizeUploadData.getId());
            }
            String insertLog = insertLogSql.toString().substring(0, insertLogSql.toString().length() - 1);
            log360aiMapper.batchSaveLog(insertLog);
            drsCustomizeUploadDataMapper.updateSyncStatusByIds(tcId, ids, QiFuSyncStatusEnum.SUCCESS.getValue());
            threadPool.submit(() -> {
                try {
                    pushProcess(dataOfNeedClean);
                } catch (Exception ex) {
                    log.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.QIFUAI_SERVICEERROR.getCode(), ex.getMessage()), ex);
                }
            });

        }
        shutdownThreadPool(threadPool);
    }

    void pushProcess(List<DrsCustomizeUploadData> dataOfNeedClean) {
        for (DrsCustomizeUploadData drsCustomizeUploadData : dataOfNeedClean) {
            // 生成推送对象
            Result<MarketingPreUserDTO> result = buildPushDto(drsCustomizeUploadData);
            if (!ResultCode.SUCCESS.getValue().equals(result.getCode())) {
                Log360aiExample example = new Log360aiExample();
                example.createCriteria().andDataIdEqualTo(drsCustomizeUploadData.getId());
                Log360ai log360ai = new Log360ai();
                log360ai.setStatus(QiFuCleanStatusEnum.FAILDATAACTION.getValue());
                log360ai.setErrorMsg(result.getMessage());
                log360aiMapper.updateByExampleSelective(log360ai, example);
                continue;
            }
            // 推送
            UpLoadCleanDTO upLoadCleanDTO = new UpLoadCleanDTO();
            upLoadCleanDTO.setDataId(drsCustomizeUploadData.getId());
            upLoadCleanDTO.setApiCode(drsCustomizeUploadData.getApiCode());
            upLoadCleanDTO.setJsonData(JSON.toJSONString(result.getData()));
            pushInfoService.pushUploadOfCleanRetry(upLoadCleanDTO, null);
        }
    }

    private Result<MarketingPreUserDTO> buildPushDto(DrsCustomizeUploadData drsCustomizeUploadData) {
        Result<MarketingPreUserDTO> res = new Result<>();
        StringBuilder errorMsg = new StringBuilder();
        StringBuilder warnMsg = new StringBuilder();
        MarketingPreUserDTO marketingPreUserDTO = new MarketingPreUserDTO();
        ArrayList<MarketingPreUserDetailDTO> list = new ArrayList<>();
        String requestJsonData = drsCustomizeUploadData.getRequestJsonData();
        String extend = drsCustomizeUploadData.getExtend();
        JSONObject jsonObject = JSONObject.parseObject(requestJsonData);
        String taskId = "";
        String requestId = "";
        JSONObject extendKey = new JSONObject();
        String batch = drsCustomizeUploadData.getReceiveDate().replaceAll("-", "").concat("_").concat(drsCustomizeUploadData.getApiCode());
        extendKey.put("operateType", "3");
        extendKey.put("batchName", batch);
        extendKey.put("batchNumber", batch);

        try {
            //region 遍历一级字段
            outerLoop:
            for (String s : jsonObject.keySet()) {
                switch (s) {
                    case "batchNo":
                        taskId = jsonObject.getString(s);
                        if (ObjectUtils.isEmpty(taskId)) {
                            errorMsg.append("batchNo为空");
                            continue outerLoop;
                        }
                        break;
                    case "flowNo":
                        String flowNo = jsonObject.getString(s);
                        if (ObjectUtils.isEmpty(flowNo)) {
                            errorMsg.append("flowNo为空");
                            continue outerLoop;
                        }
                        requestId = String.format("%s_%s", drsCustomizeUploadData.getId(), flowNo);
                        extendKey.put(s, jsonObject.getString(s));
                        break;
                    case "dataList":
                        break;
                    case "templateNo":
                        String templateStr = jsonObject.getString(s);
                        if (ObjectUtils.isEmpty(templateStr)) {
                            errorMsg.append("templateNo为空");
                            continue outerLoop;
                        }
                        String userType = "";
                        String strategyCode = "";
                        if (templateStr.length() > 12) {
                            userType = templateStr.substring(0, templateStr.length() - 12);
                            strategyCode = templateStr.substring(templateStr.length() - 12);
                        } else {
                            userType = templateStr;
                            errorMsg.append("templateNo长度小于12");
                            continue outerLoop;
                        }
                        boolean flag = marketingCommonConfig.getQifuAiCleanStrategyCodeFlag();
                        if (flag) {
                            extendKey.put("strategyCode", "");
                            extendKey.put("strategyName", "");
                        } else {
                            extendKey.put("strategyCode", strategyCode);
                            extendKey.put("strategyName", strategyCode);
                        }
                        extendKey.put("userType", userType);
                        break;
                    case "operateScene":
                        extendKey.put("customName", jsonObject.getString(s));
                        extendKey.put("customNameType", jsonObject.getString(s));
                        break;
                    default:
                        extendKey.put(s, jsonObject.getString(s));
                        break;
                }
            }
            //endregion

            if (StringUtils.isNotBlank(errorMsg.toString())) {
                log.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.QIFUAI_SERVICEERROR.getCode()
                        , "奇富360ai清洗数据异常[" + drsCustomizeUploadData.getId() + "]" + errorMsg.toString()));
                return res.setCode(ResultCode.FAIL.getValue()).setMessage(errorMsg.toString());
            }

            marketingPreUserDTO.setTaskId(taskId);
            marketingPreUserDTO.setRequestId(requestId);
            Map<String, Map<String, String>> extendToMap = parseExtendToMap(extend);
            //region 遍历二级字段
            JSONArray dataList = jsonObject.getJSONArray("dataList");
            if (!ObjectUtils.isEmpty(dataList)) {
                for (Object o : dataList) {
                    JSONObject reserField1 = new JSONObject();
                    extendKey.keySet().forEach(t -> reserField1.put(t, extendKey.get(t)));
                    JSONObject o1 = (JSONObject) o;
                    MarketingPreUserDetailDTO marketingPreUserDetailDTO = buildListDto(o1, reserField1, warnMsg, extendToMap);
                    list.add(marketingPreUserDetailDTO);
                }
                if (StringUtils.isNotBlank(warnMsg.toString())) {
                    log.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.QIFUAI_SERVICEERROR.getCode()
                            , "奇富360ai清洗数据异常字段告警[" + drsCustomizeUploadData.getId() + "]" + warnMsg.toString()));
                }
            }
            marketingPreUserDTO.setDataItems(list);


            return res.setCode(ResultCode.SUCCESS.getValue()).setDate(marketingPreUserDTO).setMessage(warnMsg.toString());
        } catch (Exception ex) {
            return res.setCode(ResultCode.FAIL.getValue()).setMessage(ex.getMessage());
        }
    }


    public static Map<String, Map<String, String>> parseExtendToMap(String extend) {
        if (extend == null || extend.isEmpty()) {
            return Collections.emptyMap();
        }

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            List<Map<String, String>> list = objectMapper.readValue(extend, new TypeReference<List<Map<String, String>>>() {});

            Map<String, Map<String, String>> resultMap = new HashMap<>();
            for (Map<String, String> item : list) {
                String serialNo = item.get("serialNo");
                if (serialNo != null) {
                    resultMap.put(serialNo, item);
                }
            }
            return resultMap;
        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.QIFUAI_SERVICEERROR.getCode(), "extend解析错误"));
            return Collections.emptyMap();
        }
    }

    private MarketingPreUserDetailDTO buildListDto(JSONObject o1, JSONObject reserField1, StringBuilder warnMsg,
                                                   Map<String, Map<String, String>> extendToMap) {
        MarketingPreUserDetailDTO marketingPreUserDetailDTO = new MarketingPreUserDetailDTO();
        for (String s : o1.keySet()) {
            switch (s) {
                case "serialNo":
                    marketingPreUserDetailDTO.setCustNum(o1.getString(s));
                    break;
                case "phoneNoMd5":
                    marketingPreUserDetailDTO.setCell(o1.getString(s));
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
                    } else if (!"".equals(genderValue)) {
                        warnMsg.append("异常性别：").append(genderValue);
                    }
                    break;
                default:
                    reserField1.put(s, o1.getString(s));
                    break;
            }
        }
        buildNewListDto(marketingPreUserDetailDTO.getCustNum(), extendToMap, reserField1, warnMsg);
        marketingPreUserDetailDTO.setReserveField1(JSONArray.toJSONString(reserField1));
        return marketingPreUserDetailDTO;
    }

    public void buildNewListDto(String custNum, Map<String, Map<String, String>> extendToMap, JSONObject reserField1,
                                StringBuilder warnMsg) {
        if (custNum == null || custNum.isEmpty()) {
            warnMsg.append("custNum 为空，请检查！\n");
            return;
        }

        if (extendToMap == null || extendToMap.isEmpty()) {
            warnMsg.append("extendToMap 为空，请检查！\n");
            return;
        }

        if (reserField1 == null) {
            reserField1 = new JSONObject();
        }

        Map<String, String> extendData = extendToMap.get(custNum);

        if (extendData == null) {
            warnMsg.append("未找到匹配项: custNum=").append(custNum).append(";\n");
            return;
        }

        for (Map.Entry<String, String> entry : extendData.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue() != null ? entry.getValue() : "";

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

    public String processCouponInfo(String rCouponInfo) {
        if (StringUtils.isBlank(rCouponInfo)) {
            return "";
        }

        try {
            JSONArray coupons = JSON.parseArray(rCouponInfo);
            if (coupons == null || coupons.isEmpty()) {
                return "";
            }

            // 新需求：只保留清洗逻辑，取清洗后的第一个券
            String firstCouponName = coupons.getJSONObject(0).getString("couponName");
            return cleanCouponName(firstCouponName);

            // 第一步：清洗字段 + 第二步：券分类
            // List<CouponInfo> couponInfos = new ArrayList<>();
            // for (int i = 0; i < coupons.size(); i++) {
            //     String couponName = coupons.getJSONObject(i).getString("couponName");
            //     String cleanedName = cleanCouponName(couponName);
            //     CouponInfo couponInfo = classifyCoupon(cleanedName, i);
            //     couponInfos.add(couponInfo);
            // }
            // 
            // // 如果只有一个券，直接返回清洗后的名称
            // if (couponInfos.size() == 1) {
            //     return couponInfos.get(0).getCleanedName();
            // }
            // 
            // // 第三步：券优先级 + 第四步：返回结果
            // return selectBestCoupon(couponInfos);

        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.QIFUAI_SERVICEERROR.getCode(),
                    "处理优惠券信息时发生错误，错误信息：" + e.getMessage()), e);
            return "";
        }
    }

    /**
     * 第一步：清洗字段
     * 清除"智信"、"超级会员"、"专属"字眼
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
     * 第二步：券分类
     */
    private CouponInfo classifyCoupon(String cleanedName, int originalIndex) {
        if (StringUtils.isBlank(cleanedName)) {
            return new CouponInfo("", cleanedName, CouponType.COMMON, 0, 0, originalIndex);
        }

        // 分期券：券名称带"期"字，分别提取期数和金额
        if (cleanedName.contains("期")) {
            double periods = extractInstallmentPeriods(cleanedName);
            double amount = extractAmountFromString(cleanedName);
            return new CouponInfo("", cleanedName, CouponType.INSTALLMENT, periods, amount, originalIndex);
        }

        // 周转金：券名称带"周转金"字，分别提取天数和金额
        if (cleanedName.contains("周转金")) {
            double days = extractTurnoverDays(cleanedName);
            double amount = extractAmountFromString(cleanedName);
            return new CouponInfo("", cleanedName, CouponType.TURNOVER, days, amount, originalIndex);
        }

        // 折扣券：券名称带"折"字，分别提取折扣率和金额
        if (cleanedName.contains("折")) {
            double discountRate = extractDiscountRate(cleanedName);
            double amount = extractAmountFromString(cleanedName);
            return new CouponInfo("", cleanedName, CouponType.DISCOUNT, discountRate, amount, originalIndex);
        }

        // 大额直减券和小额直减券：券名称带"元"字
        if (cleanedName.contains("元")) {
            double amount = extractAmountFromString(cleanedName);
            if (amount >= 600) {
                return new CouponInfo("", cleanedName, CouponType.LARGE_REDUCTION, amount, amount, originalIndex);
            } else {
                return new CouponInfo("", cleanedName, CouponType.SMALL_REDUCTION, amount, amount, originalIndex);
            }
        }

        // 普通券：不符合以上规则
        double extractedValue = extractNumber(cleanedName);
        double amount = extractAmountFromString(cleanedName);
        return new CouponInfo("", cleanedName, CouponType.COMMON, extractedValue, amount, originalIndex);
    }

    /**
     * 提取分期券的期数
     */
    private double extractInstallmentPeriods(String couponName) {
        if (StringUtils.isBlank(couponName)) {
            return 0;
        }

        try {
            // 提取期数
            Pattern installmentPattern = Pattern.compile("(\\d+(?:\\.\\d+)?)期");
            Matcher installmentMatcher = installmentPattern.matcher(couponName);
            if (installmentMatcher.find()) {
                return Double.parseDouble(installmentMatcher.group(1));
            }
        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.QIFUAI_SERVICEERROR.getCode(),
                    "提取分期券期数时发生错误: " + couponName + "，错误信息：" + e.getMessage()), e);
        }
        return 0;
    }

    /**
     * 从券名称中提取金额
     */
    private double extractAmountFromString(String couponName) {
        if (StringUtils.isBlank(couponName)) {
            return 0;
        }

        try {
            // 提取金额（带"元"字的）
            Pattern amountPattern = Pattern.compile("(\\d+(?:\\.\\d+)?)元");
            Matcher amountMatcher = amountPattern.matcher(couponName);
            if (amountMatcher.find()) {
                return Double.parseDouble(amountMatcher.group(1));
            }
        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.QIFUAI_SERVICEERROR.getCode(),
                    "提取券金额时发生错误: " + couponName + "，错误信息：" + e.getMessage()), e);
        }
        return 0;
    }

    /**
     * 提取周转金券的天数
     */
    private double extractTurnoverDays(String couponName) {
        if (StringUtils.isBlank(couponName)) {
            return 0;
        }

        try {
            // 提取天数
            Pattern daysPattern1 = Pattern.compile("(\\d+(?:\\.\\d+)?)天周转金");
            Matcher daysMatcher1 = daysPattern1.matcher(couponName);
            if (daysMatcher1.find()) {
                return Double.parseDouble(daysMatcher1.group(1));
            } else {
                // 尝试其他模式
                Pattern daysPattern2 = Pattern.compile("(\\d+(?:\\.\\d+)?)(?:元)?周转金");
                Matcher daysMatcher2 = daysPattern2.matcher(couponName);
                if (daysMatcher2.find()) {
                    return Double.parseDouble(daysMatcher2.group(1));
                } else {
                    Pattern daysPattern3 = Pattern.compile("周转金(\\d+(?:\\.\\d+)?)(?:天|元)?");
                    Matcher daysMatcher3 = daysPattern3.matcher(couponName);
                    if (daysMatcher3.find()) {
                        return Double.parseDouble(daysMatcher3.group(1));
                    }
                }
            }
        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.QIFUAI_SERVICEERROR.getCode(),
                    "提取周转金天数时发生错误: " + couponName + "，错误信息：" + e.getMessage()), e);
        }
        return 0;
    }

    /**
     * 提取折扣券的折扣率
     */
    private double extractDiscountRate(String couponName) {
        if (StringUtils.isBlank(couponName)) {
            // 默认值，表示没有折扣
            return 10;
        }

        try {
            // 提取折扣率
            Pattern discountPattern = Pattern.compile("(\\d+(?:\\.\\d+)?)折");
            Matcher discountMatcher = discountPattern.matcher(couponName);
            if (discountMatcher.find()) {
                return Double.parseDouble(discountMatcher.group(1));
            }
        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.QIFUAI_SERVICEERROR.getCode(),
                    "提取折扣率时发生错误: " + couponName + "，错误信息：" + e.getMessage()), e);
        }
        // 默认值，表示没有折扣
        return 10;
    }

    /**
     * 从券名称中提取数字
     * 根据券类型使用不同的提取策略，返回组合权重值
     */
    private double extractNumber(String couponName) {
        if (StringUtils.isBlank(couponName)) {
            return 0;
        }

        try {
            // 提取所有数字并取最大值
            Pattern pattern = Pattern.compile("(\\d+(?:\\.\\d+)?)");
            Matcher matcher = pattern.matcher(couponName);

            List<Double> numbers = new ArrayList<>();
            while (matcher.find()) {
                numbers.add(Double.parseDouble(matcher.group(1)));
            }

            if (numbers.isEmpty()) {
                return 0;
            }

            return numbers.stream().mapToDouble(Double::doubleValue).max().orElse(0);

        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.QIFUAI_SERVICEERROR.getCode(),
                    "提取券名称中的数字时发生错误: " + couponName + "，错误信息：" + e.getMessage()), e);
            return 0;
        }
    }

    /**
     * 第三步：券优先级选择
     * 分期券>大额直减券>周转金>折扣券>小额直减券>普通券
     * D20250919营销360AI 券字段清洗&优先级排序需求 取消使用
     */
    private String selectBestCoupon(List<CouponInfo> couponInfos) {
        if (couponInfos == null || couponInfos.isEmpty()) {
            return "";
        }

        // 按类型优先级和数值优先级排序
        couponInfos.sort((c1, c2) -> {
            // 先按类型优先级排序
            int typeComparison = Integer.compare(c1.getType().getPriority(), c2.getType().getPriority());
            if (typeComparison != 0) {
                return typeComparison;
            }

            // 同类型内按数值优先级排序
            if (c1.getType() == CouponType.COMMON) {
                // 普通券：按原始索引排序（取第一个）
                return Integer.compare(c1.getOriginalIndex(), c2.getOriginalIndex());
            } else if (c1.getType() == CouponType.INSTALLMENT) {
                // 分期券：先比较期数（期数越大优先级越高），期数相同时比较金额（金额越大优先级越高）
                int periodComparison = Double.compare(c2.getValue(), c1.getValue());
                if (periodComparison != 0) {
                    return periodComparison;
                }
                // 期数相同时比较金额
                return Double.compare(c2.getAmount(), c1.getAmount());
            } else if (c1.getType() == CouponType.TURNOVER) {
                // 周转金券：先比较天数（天数越大优先级越高），天数相同时比较金额（金额越大优先级越高）
                int daysComparison = Double.compare(c2.getValue(), c1.getValue());
                if (daysComparison != 0) {
                    return daysComparison;
                }
                // 天数相同时比较金额
                return Double.compare(c2.getAmount(), c1.getAmount());
            } else if (c1.getType() == CouponType.DISCOUNT) {
                // 折扣券：先比较折扣率（折扣率越小优先级越高），折扣率相同时比较金额（金额越大优先级越高）
                // 注意：折扣率小的优先级高，所以c1和c2位置相反
                int discountComparison = Double.compare(c1.getValue(), c2.getValue());
                if (discountComparison != 0) {
                    return discountComparison;
                }
                // 折扣率相同时比较金额
                return Double.compare(c2.getAmount(), c1.getAmount());
            } else {
                // 所有其他券类型：按amount排序（数值越大优先级越高）
                if (c1.getType() == CouponType.LARGE_REDUCTION || c1.getType() == CouponType.SMALL_REDUCTION) {
                    // 直减券按金额比较
                    return Double.compare(c2.getAmount(), c1.getAmount());
                } else {
                    // 普通券等其他类型按value比较
                    return Double.compare(c2.getValue(), c1.getValue());
                }
            }
        });

        // 返回最优券的清洗后名称
        return couponInfos.get(0).getCleanedName();
    }

    private String getValueOfJson(JSONObject jo, String key, String defaultValue) {
        if (jo == null || ObjectUtils.isEmpty(jo.getString(key))) {
            return defaultValue;
        }
        return jo.getString(key);
    }

    private void shutdownThreadPool(ThreadPoolExecutor executor) {
        executor.shutdown();
        Boolean b = true;
        while (b) {
            if (executor.isTerminated()) {
                b = false;
            } else {
                try {
                    Thread.sleep(3000L);
                } catch (InterruptedException e) {
                    log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.QIFUAI_SERVICEERROR.getCode(),
                            e.getMessage()), e);
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    private Boolean dynamicAction(ThreadPoolExecutor executor) {
        JSONObject qifuAiCleanConfig = marketingCommonConfig.getQifuAiCleanConfig();
        Boolean isPause = qifuAiCleanConfig.getBoolean("isPause");
        if (isPause == null || isPause) {
            return Boolean.TRUE;
        }
        if (StringUtils.isNotBlank(qifuAiCleanConfig.getString("threadNum"))) {
            Integer threadNum = Integer.valueOf(qifuAiCleanConfig.getString("threadNum"));
            if (executor.getCorePoolSize() != threadNum.intValue()) {
                ThreadPoolAdjustmentUtil.adjustThreadPoolSize(executor, threadNum);
            }
        }
        return Boolean.FALSE;
    }

    public String mapYesNo(String input) {
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

    public String mapNumberToRange(String input) {
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

            Integer qiFuConfigNum = ObjectUtil.isEmpty(marketingCommonConfig.getQiFuConfigNum()) ? 1000 : marketingCommonConfig.getQiFuConfigNum();
            int lowerBound = (num - 1) * qiFuConfigNum;
            int upperBound = num * qiFuConfigNum;
            return "[" + lowerBound + " - " + upperBound + ")";
        } catch (NumberFormatException e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.QIFUAI_SERVICEERROR.getCode(),
                    "奇富AI 额度计算发生错误！错误信息：" + e.getMessage()), e);
            return "";
        }
    }

    private static final Pattern DATE_PATTERN = Pattern.compile("^(\\d{2})-(\\d{2})$");

    public String mapDateString(String input) {
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

    public String getAmount(String highAmountys, String lowAmountys, String rTotalAvailableAmt) {
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


    public String calculateDifference(String highAmountys, String lowAmountys) {
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

    public String calculateDaysDifference(String rTaTemporaryAmountExpireDate) {
        if ("noLimit".equalsIgnoreCase(rTaTemporaryAmountExpireDate) || ObjectUtil.isEmpty(rTaTemporaryAmountExpireDate)) {
            return "9999";
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate today = LocalDate.now();

        try {
            String dateWithYear = today.getYear() + "-" + rTaTemporaryAmountExpireDate;
            LocalDate expireDate = LocalDate.parse(dateWithYear, formatter);

            if (expireDate.isBefore(today)) {
                log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.QIFUAI_SERVICEERROR.getCode(),
                        "奇富AI 额度到期日期小于今天！"));
                expireDate = expireDate.plusYears(1);
            }

            return String.valueOf(ChronoUnit.DAYS.between(today, expireDate));
        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.QIFUAI_SERVICEERROR.getCode(),
                    "奇富AI额度到期日期计算发生错误！错误信息：" + e.getMessage()), e);
            return "";
        }
    }

    public String calculateIncreaseRate(String highAmountys, String lowAmountys) {
        if (ObjectUtil.isEmpty(highAmountys) || ObjectUtil.isEmpty(lowAmountys) || lowAmountys.equals("0")) {
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
