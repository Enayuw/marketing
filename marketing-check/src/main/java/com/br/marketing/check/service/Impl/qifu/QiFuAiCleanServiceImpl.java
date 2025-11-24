package com.br.marketing.check.service.Impl.qifu;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.br.common.log.AlertLog;
import com.br.marketing.check.service.qifu.QiFuAiCleanService;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.dto.MarketingPreUserDTO;
import com.br.marketing.dto.MarketingPreUserDetailDTO;
import com.br.marketing.dto.qifu.UpLoadCleanDTO;
import com.br.marketing.entity.BQifuUploadDataOriginal;
import com.br.marketing.entity.Log360ai;
import com.br.marketing.entity.Log360aiExample;
import com.br.marketing.mapper.BQifuUploadDataOriginalMapper;
import com.br.marketing.mapper.Log360aiMapper;
import com.br.marketing.service.Impl.qifu.enums.QiFuProcessStatusEnum;
import com.br.marketing.service.Impl.qifu.enums.QiFuSelectStatusEnum;
import com.br.marketing.service.Impl.qifu.valobj.QiFuCleanStatusEnum;
import com.br.marketing.service.PushInfoService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.ListUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ThreadPoolExecutor;
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
     * Redis开关key前缀
     */
    private static final String REDIS_SWITCH_KEY_PREFIX = "qifu:query:call:switch:";

    /**
     * 分页大小
     */
    private static final int PAGE_SIZE = 2000;

    /**
     * 有卷比例阈值
     */
    private static final double COUPON_RATIO_THRESHOLD = 0.75;

    /**
     * 时间阈值（12:10）
     */
    private static final LocalTime TIME_THRESHOLD = LocalTime.of(12, 10);

    /**
     * Redis过期时间（秒），24小时
     */
    private static final int REDIS_EXPIRE_SECONDS = 24 * 60 * 60;

    @Resource
    private RedisChgService redisChgService;

    @Resource
    private BQifuUploadDataOriginalMapper bQifuUploadDataOriginalMapper;

    @Resource
    private Log360aiMapper log360aiMapper;

    @Resource
    private PushInfoService pushInfoService;

    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Override
    public void aiCleanProcessFromOriginal() {
        log.warn("奇富ai清洗开始，查询b_qifu_upload_data_original数据");

        // 获取今天的日期
        String todayDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        // 查询今天所有不同的user_type
        List<String> userTypeList = bQifuUploadDataOriginalMapper.selectDistinctUserTypeByDate(todayDate);
        if (userTypeList == null || userTypeList.isEmpty()) {
            log.warn("今天 {} 没有查询到user_type数据，无需处理", todayDate);
            return;
        }

        JSONObject qifuAiCleanConfig = marketingCommonConfig.getQifuAiCleanConfig();
        Integer threadNum = Integer.valueOf(getValueOfJson(qifuAiCleanConfig, "threadNum", "10"));
        ThreadPoolExecutor threadPool = BrExecutors.getThreadPool(threadNum, threadNum, "qiAiCleanOriginal", 200);

        // 按user_type维度处理，每个user_type单独处理
        for (String userType : userTypeList) {
            final String finalUserType = userType;
            final String finalTodayDate = todayDate;
            threadPool.submit(() -> {
                try {
                    processUserTypeDataForClean(finalUserType, finalTodayDate);
                } catch (Exception e) {
                    log.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.QIFUAI_SERVICEERROR.getCode()
                            , "奇富360ai清洗数据异常[userType: " + finalUserType + "]" + e.getMessage()), e);
                }
            });
        }

        // 关闭线程池
        shutdownThreadPool(threadPool);
    }

    @Override
    public void aiRealTimeCleanProcessFromOriginal() {
        //获取待清洗的数据
        List<BQifuUploadDataOriginal> uploadDataOriginalList = new ArrayList<>();
        Long minId = null;
        while (true) {
            uploadDataOriginalList = bQifuUploadDataOriginalMapper.selectRealTimeDataForClean(minId, PAGE_SIZE);
            if (uploadDataOriginalList == null || uploadDataOriginalList.isEmpty()) {
                break;
            }

            minId = uploadDataOriginalList.get(uploadDataOriginalList.size() - 1).getId();

            // 数据清洗：设置 status=处理中,批量更新数据库
            updateStatus(uploadDataOriginalList);

            // 记录日志并调用上传接口
            insertLog(uploadDataOriginalList);

            // 调用上传接口
            pushProcessForOriginal(uploadDataOriginalList, "3");
        }

    }

    /**
     * 处理某个userType的清洗数据（按场景维度处理，基于今天的数据）
     */
    private void processUserTypeDataForClean(String userType, String todayDate) {
        // 检查当前场景的Redis开关
        boolean switchOpen = checkRedisSwitch(userType, todayDate);

        // 根据开关状态确定查询的select_status列表
        List<Integer> selectStatusList;
        if (switchOpen) {
            // 开关打开：查询 select_status = 查询成功
            selectStatusList = Arrays.asList(QiFuSelectStatusEnum.QUERY_SUCCESS.getCode());
        } else {
            // 开关关闭：查询 select_status in (查询成功, 重试-无卷信息)
            selectStatusList = Arrays.asList(
                QiFuSelectStatusEnum.QUERY_SUCCESS.getCode(),
                QiFuSelectStatusEnum.RETRY_NO_COUPON.getCode()
            );
        }

        Long indexId = null;
        boolean hasMore = true;

        while (hasMore) {
            // 查询当前场景今天需要清洗的数据
            List<BQifuUploadDataOriginal> dataList = bQifuUploadDataOriginalMapper.selectDataForCleanByUserTypeAndDate(
                    userType, selectStatusList, todayDate, PAGE_SIZE, indexId);
            if (dataList == null || dataList.isEmpty()) {
                hasMore = false;
                break;
            }

            indexId = dataList.get(dataList.size() - 1).getId();

            // 数据清洗：设置 status=处理中,批量更新数据库
            updateStatus(dataList);

            // 记录日志并调用上传接口
            insertLog(dataList);

            // 调用上传接口
            pushProcessForOriginal(dataList, "3");

            if (dataList.size() < PAGE_SIZE) {
                hasMore = false;
            }
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

    public void insertLog(List<BQifuUploadDataOriginal> dataList) {
        StringBuilder insertLogSql = new StringBuilder()
                .append("insert into b_log_360ai ")
                .append("(data_id,status) ")
                .append("values ");
        for (BQifuUploadDataOriginal record : dataList) {
            insertLogSql.append(String.format("(%d,%d),", record.getId(), QiFuCleanStatusEnum.RUNNING.getValue()));
        }
        if (insertLogSql.length() > 0) {
            insertLogSql.setLength(insertLogSql.length() - 1);
        }
        log360aiMapper.batchSaveLog(insertLogSql.toString());
    }

    /**
     * 检查Redis开关（按user_type维度）
     * 条件：user_type有卷比例>=75% 或者 当前时间>12:10
     */
    private boolean checkRedisSwitch(String userType, String todayDate) {
        try {
            // 检查当前时间是否>12:10
            LocalTime currentTime = LocalTime.now();
            if (currentTime.isAfter(TIME_THRESHOLD) || currentTime.equals(TIME_THRESHOLD)) {
                log.warn("userType={} 当前时间 {} >= {}，Redis开关打开", userType, currentTime, TIME_THRESHOLD);
                return true;
            }

            // 检查Redis中是否存在该user_type的开关
            String redisKey = REDIS_SWITCH_KEY_PREFIX + userType;
            Boolean exists = redisChgService.exists(redisKey);

            if (exists == null || !exists) {
                // Redis中不存在，查询数据库统计有卷比例并新增到Redis
                double ratio = calculateCouponRatio(userType, todayDate);
                // 新增到Redis
                redisChgService.setex(redisKey, String.valueOf(ratio), REDIS_EXPIRE_SECONDS);
                log.warn("userType={} Redis开关不存在，查询数据库统计今天有卷比例={}，已新增到Redis", userType, ratio);

                if (ratio >= COUPON_RATIO_THRESHOLD) {
                    log.warn("userType={} 有卷比例 {} >= {}，Redis开关打开", userType, ratio, COUPON_RATIO_THRESHOLD);
                    return true;
                }
            } else {
                // Redis中存在，获取有卷比例
                String ratioStr = redisChgService.get(redisKey);
                if (StringUtils.isNotBlank(ratioStr)) {
                    try {
                        double ratio = Double.parseDouble(ratioStr);
                        if (ratio >= COUPON_RATIO_THRESHOLD) {
                            log.warn("userType={} 有卷比例 {} >= {}，Redis开关打开", userType, ratio, COUPON_RATIO_THRESHOLD);
                            return true;
                        }
                    } catch (NumberFormatException e) {
                        log.warn("解析userType={}的有卷比例失败，ratioStr={}，重新计算", userType, ratioStr);
                        // 解析失败，重新计算并更新Redis
                        double ratio = calculateCouponRatio(userType, todayDate);
                        redisChgService.setex(redisKey, String.valueOf(ratio), REDIS_EXPIRE_SECONDS);
                        if (ratio >= COUPON_RATIO_THRESHOLD) {
                            return true;
                        }
                    }
                }
            }

            return false;
        } catch (Exception e) {
            log.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.QIFUAI_SERVICEERROR.getCode()
                    , "奇富360ai清洗数据异常[检查Redis开关失败, userType: " + userType + "]" + e.getMessage()), e);
            return false;
        }
    }

    /**
     * 计算有卷比例（基于今天的数据）
     */
    private double calculateCouponRatio(String userType, String todayDate) {
        try {
            // 查询该user_type下今天的数据总数
            Long totalCount = bQifuUploadDataOriginalMapper.countByUserTypeAndSelectStatusAndDate(userType, todayDate);

            if (totalCount == null || totalCount == 0) {
                log.warn("userType={} 今天 {} 没有查询到的数据", userType, todayDate);
                return 0.0;
            }

            // 查询今天有卷的数据数量（select_status=2）
            Long couponCount = bQifuUploadDataOriginalMapper.countCouponDataByUserTypeAndDate(userType, todayDate);

            if (couponCount == null || couponCount == 0) {
                return 0.0;
            }

            double ratio = (double) couponCount / totalCount;
            log.warn("userType={} 今天 {} 有卷比例计算：总数={}，有卷数={}，比例={}", userType, todayDate, totalCount, couponCount, ratio);
            return ratio;
        } catch (Exception e) {
            log.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.QIFUAI_SERVICEERROR.getCode()
                    , "奇富360ai清洗数据异常[计算有卷比例失败, userType: " + userType + ", todayDate: " + todayDate + "]" + e.getMessage()), e);
            return 0.0;
        }
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
        for (BQifuUploadDataOriginal record : dataList) {
            // 生成推送对象
            Result<MarketingPreUserDTO> result = buildPushDtoFromOriginal(record, operateType);
            if (!ResultCode.SUCCESS.getValue().equals(result.getCode())) {
                Log360aiExample example = new Log360aiExample();
                example.createCriteria().andDataIdEqualTo(record.getId());
                Log360ai log360ai = new Log360ai();
                log360ai.setStatus(QiFuCleanStatusEnum.FAILDATAACTION.getValue());
                log360ai.setErrorMsg(result.getMessage());
                log360aiMapper.updateByExampleSelective(log360ai, example);
                continue;
            }
            // 推送
            UpLoadCleanDTO upLoadCleanDTO = new UpLoadCleanDTO();
            upLoadCleanDTO.setDataId(record.getId());
            upLoadCleanDTO.setApiCode(record.getApiCode());
            upLoadCleanDTO.setJsonData(JSON.toJSONString(result.getData()));
            pushInfoService.pushUploadOfCleanRetry(upLoadCleanDTO, null);
        }
    }

    /**
     * 从BQifuUploadDataOriginal构建推送DTO
     */
    private Result<MarketingPreUserDTO> buildPushDtoFromOriginal(BQifuUploadDataOriginal record, String operateType) {
        Result<MarketingPreUserDTO> res = new Result<>();
        StringBuilder errorMsg = new StringBuilder();
        StringBuilder warnMsg = new StringBuilder();
        MarketingPreUserDTO marketingPreUserDTO = new MarketingPreUserDTO();
        ArrayList<MarketingPreUserDetailDTO> list = new ArrayList<>();

        try {
            // 验证必要字段
            if (StringUtils.isBlank(record.getBatchNo())) {
                errorMsg.append("batchNo为空");
            }
            if (StringUtils.isBlank(record.getFlowNo())) {
                errorMsg.append("flowNo为空");
            }
            if (StringUtils.isBlank(record.getTemplateNo())) {
                errorMsg.append("templateNo为空");
            }

            if (StringUtils.isNotBlank(errorMsg.toString())) {
                log.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.QIFUAI_SERVICEERROR.getCode()
                        , "奇富360ai清洗数据异常[" + record.getId() + "]" + errorMsg.toString()));
                return res.setCode(ResultCode.FAIL.getValue()).setMessage(errorMsg.toString());
            }

            String extend = record.getExtend();
            String batch;
            String strategyCode;
            String strategyName;
            String userType;
            boolean isRealTime = StringUtils.isNotBlank(operateType);
            if (isRealTime) {
                // 实时推送逻辑
                LocalDate today = LocalDate.now();
                String currentDate = today.format(DateTimeFormatter.ofPattern("yyyyMMdd"));

                batch = currentDate + "_" + record.getApiCode() + "_实时推送";
                strategyCode = "CASTR0322614";
                strategyName = "CASTR0322614";

                // 处理templateNo，提取userType
                String templateStr = record.getTemplateNo();
                if (templateStr.length() > 12) {
                    userType = templateStr.substring(0, templateStr.length() - 12);
                } else {
                    userType = templateStr;
                }
            } else {
                // 非实时推送逻辑
                batch = record.getReceiveDate().replaceAll("-", "").concat("_").concat(record.getApiCode());
                userType = record.getUserType();
                // 处理templateNo
                String templateStr = record.getTemplateNo();
                if (templateStr.length() > 12) {
                    strategyCode = templateStr.substring(templateStr.length() - 12);
                    strategyName = strategyCode;
                } else {
                    strategyCode = "";
                    strategyName = "";
                }
            }

            JSONObject extendKey = new JSONObject();
            extendKey.put("batchName", batch);
            extendKey.put("batchNumber", batch);

            // 设置taskId和requestId
            String taskId = record.getBatchNo();
            String requestId = String.format("%s_%s", record.getId(), record.getFlowNo());
            marketingPreUserDTO.setTaskId(taskId);
            marketingPreUserDTO.setRequestId(requestId);

            // 设置strategyCode和strategyName
            if (isRealTime) {
                // 实时推送直接使用固定的strategyCode
                extendKey.put("strategyCode", strategyCode);
                extendKey.put("strategyName", strategyName);
            } else {
                // 非实时推送根据配置决定是否使用strategyCode
                boolean flag = marketingCommonConfig.getQifuAiCleanStrategyCodeFlag();
                if (flag) {
                    extendKey.put("strategyCode", "");
                    extendKey.put("strategyName", "");
                } else {
                    extendKey.put("strategyCode", strategyCode);
                    extendKey.put("strategyName", strategyName);
                }
            }
            extendKey.put("userType", userType);

            // 设置其他字段
            extendKey.put("flowNo", record.getFlowNo());
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

            // 解析extend字段（extend是单个JSON对象，不是数组）
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

            MarketingPreUserDetailDTO marketingPreUserDetailDTO = buildListDto(detailJson, reserField1, warnMsg, extendJsonObject);
            list.add(marketingPreUserDetailDTO);

            if (StringUtils.isNotBlank(warnMsg.toString())) {
                log.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.QIFUAI_SERVICEERROR.getCode()
                        , "奇富360ai清洗数据异常字段告警[" + record.getId() + "]" + warnMsg.toString()));
            }

            marketingPreUserDTO.setDataItems(list);

            return res.setCode(ResultCode.SUCCESS.getValue()).setDate(marketingPreUserDTO).setMessage(warnMsg.toString());
        } catch (Exception ex) {
            return res.setCode(ResultCode.FAIL.getValue()).setMessage(ex.getMessage());
        }
    }

    /**
     * 构建列表DTO
     */
    private MarketingPreUserDetailDTO buildListDto(JSONObject o1, JSONObject reserField1, StringBuilder warnMsg,
                                                   JSONObject extendJsonObject) {
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
                    } else if (!"".equals(genderValue)) {
                        warnMsg.append("异常性别：").append(genderValue);
                    }
                    break;
                default:
                    reserField1.put(s, o1.getString(s));
                    break;
            }
        }
        buildNewListDto(extendJsonObject, reserField1, warnMsg);
        marketingPreUserDetailDTO.setReserveField1(JSONArray.toJSONString(reserField1));
        return marketingPreUserDetailDTO;
    }

    /**
     * 构建新的列表DTO（处理extend字段）
     */
    private void buildNewListDto(JSONObject extendJsonObject, JSONObject reserField1,
                                 StringBuilder warnMsg) {
        if (extendJsonObject == null || extendJsonObject.isEmpty()) {
            warnMsg.append("extendJsonObject 为空，请检查！\n");
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

    /**
     * 关闭线程池
     */
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

    /**
     * 获取JSON值
     */
    private String getValueOfJson(JSONObject jo, String key, String defaultValue) {
        if (jo == null || ObjectUtils.isEmpty(jo.getString(key))) {
            return defaultValue;
        }
        return jo.getString(key);
    }
}

