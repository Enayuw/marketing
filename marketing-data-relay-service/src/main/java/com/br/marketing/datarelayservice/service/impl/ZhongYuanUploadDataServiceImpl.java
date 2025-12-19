package com.br.marketing.datarelayservice.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.common.log.AlertLog;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.datarelayservice.enums.ZhongYuanResponseCodeEnum;
import com.br.marketing.datarelayservice.service.ZhongYuanUploadDataService;
import com.br.marketing.dto.zhongyuan.*;
import com.br.marketing.entity.*;
import com.br.marketing.enums.clean.DataProcessEnum;
import com.br.marketing.mapper.*;
import com.br.marketing.mapper.rulecleaning.MarketingCustomerOriginalDataMapper;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.marketingkit.tracking.model.indicator.DataFlowDirection;
import com.marketingkit.tracking.service.TrackingService;
import com.marketingkit.tracking.util.TrackingContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * @ClassName ZhongYuanUploadDataServiceImpl
 * @Description 中原消金
 * @Author kongbx
 * @Date 2025/11/14 11:19
 */
@Service
@Slf4j
public class ZhongYuanUploadDataServiceImpl implements ZhongYuanUploadDataService {

    @Resource
    private RedisChgService redisChgService;
    @Resource
    private ZhongYuanUploadMapper zhongYuanUploadMapper;
    @Resource
    private ZhongYuanTransferMapper zhongYuanTransferMapper;
    @Resource
    MarketingCustomerOriginalDataMapper marketingCustomerOriginalDataMapper;
    @Resource
    private MarketingCommonConfig marketingCommonConfig;
    @Resource
    private CallRecordLLMResultV2Mapper callRecordLLMResultV2Mapper;
    @Resource
    private MarketingSceneVariableMapper marketingSceneVariableMapper;
    @Resource
    private TrackingService trackingService;
    private static final String TOKEN_PREFIX = "zyxj:token:";
    private static final long TOKEN_EXPIRE_TIME = 7200; // 2小时

    @Override
    public ZhongYuanBaseResponse<?> login(String jsonData, HttpServletRequest request) {
        try {
            log.warn("中原消金登录接口请求，jsonData: {}", jsonData);

            // 1. 解析请求数据
            ZhongYuanBaseRequest<LoginRequest> baseRequest = JSON.parseObject(jsonData,
                    new com.alibaba.fastjson.TypeReference<ZhongYuanBaseRequest<LoginRequest>>() {
                    });

            if (baseRequest == null || baseRequest.getData() == null) {
                return ZhongYuanBaseResponse.fail(ZhongYuanResponseCodeEnum.PARAM_ERROR.getCode(), ZhongYuanResponseCodeEnum.PARAM_ERROR.getMessage());
            }

            LoginRequest loginData = baseRequest.getData();

            // 2. 参数校验
            if (!StringUtils.hasText(loginData.getAppUser()) || !StringUtils.hasText(loginData.getAppKey())) {
                return ZhongYuanBaseResponse.fail(ZhongYuanResponseCodeEnum.PARAM_ERROR.getCode(), "参数错误：appUser或appKey为空");
            }

            // 3. 验证appUser和appKey
            if (!validateCredentials(loginData.getAppUser(), loginData.getAppKey())) {
                log.warn("登录失败，用户名或密码错误，appUser: {}", loginData.getAppUser());
                return ZhongYuanBaseResponse.fail(ZhongYuanResponseCodeEnum.PARAM_ERROR.getCode(), "用户名或密码错误");
            }

            // 4. 直接检查Redis中是否存在有效的Token（通过appUser）
            String tokenKey = TOKEN_PREFIX + loginData.getAppUser();
            String token = redisChgService.get(tokenKey);

            if (StringUtils.isEmpty(token)) {
                // 不存在Token，生成新Token
                token = generateToken(loginData.getAppUser());
                redisChgService.setex(tokenKey, token, (int) TOKEN_EXPIRE_TIME);
                log.warn("中原消金登录成功（生成新Token），appUser: {}, token: {}", tokenKey, token);
            }

            // 5. 构建响应
            LoginResponse loginResponse = new LoginResponse();
            loginResponse.setToken(token);

            try {
                Map<String, String> zhongYuanIdentity = marketingCommonConfig.getZhongYuanIdentity();
                String apiCode = zhongYuanIdentity.get("apiCode");
                String remark = String.format("中原消金-用户登录接口，获取token：%s", token);
                trackingService.trackPointLog(DataFlowDirection.IN
                        , apiCode
                        , "中原消金-用户登录接口"
                        , 1L
                        , remark
                        , TrackingContext.generateBatchId());
            } catch (Exception ex) {
                log.warn(
                        AlertLog.buildWarnMessage(
                                AlarmSendCodeEnum.TRACKING_POINT_SERVICEERROR.getCode()
                                , ex.getMessage()
                                , "埋点异常")
                        , ex);
            }

            return ZhongYuanBaseResponse.success(loginResponse);

        } catch (Exception e) {
            log.error("中原消金登录接口异常", e);
            return ZhongYuanBaseResponse.fail(ZhongYuanResponseCodeEnum.SYSTEM_ERROR.getCode(), ZhongYuanResponseCodeEnum.SYSTEM_ERROR.getMessage() + "：" + e.getMessage());
        }
    }

    @Override
    public ZhongYuanBaseResponse<?> batchTask(String jsonData, HttpServletRequest request) {
        try {
            log.warn("中原消金批量任务上报接口请求，jsonData长度: {}", jsonData != null ? jsonData.length() : 0);

            // 1. 解析请求数据
            ZhongYuanBaseRequest<BatchTaskRequest> baseRequest = JSON.parseObject(jsonData,
                    new com.alibaba.fastjson.TypeReference<ZhongYuanBaseRequest<BatchTaskRequest>>() {
                    });

            if (baseRequest == null || baseRequest.getData() == null) {
                return ZhongYuanBaseResponse.fail(ZhongYuanResponseCodeEnum.PARAM_ERROR.getCode(), ZhongYuanResponseCodeEnum.PARAM_ERROR.getMessage());
            }

            // 2. Token验证
            ZhongYuanBaseResponse<?> tokenResponse = validateTokenFromRequest(baseRequest);
            if (!ZhongYuanResponseCodeEnum.SUCCESS.getCode().equals(tokenResponse.getCode())) {
                return tokenResponse;
            }

            BatchTaskRequest batchData = baseRequest.getData();

            // 3. 参数校验
            if (!StringUtils.hasText(batchData.getBatchNo()) || batchData.getTaskDataList() == null
                    || batchData.getTaskDataList().isEmpty()) {
                return ZhongYuanBaseResponse.fail(ZhongYuanResponseCodeEnum.PARAM_ERROR.getCode(), "参数错误：批次编号或任务数据列表为空");
            }

            // 4. 保存原始数据到b_marketing_zhongyuan_upload表
            ZhongYuanUpload zhongYuanUpload = new ZhongYuanUpload();
            Map<String, String> zhongYuanIdentity = marketingCommonConfig.getZhongYuanIdentity();
            String testApiCode = request.getHeader("Test-ApiCode");
            String apiCode = testApiCode != null ? testApiCode : zhongYuanIdentity.get("apiCode");
            zhongYuanUpload.setApiCode(apiCode);
            // 从baseRequest获取公共字段
            zhongYuanUpload.setFlowId(StringUtils.hasText(baseRequest.getFlowId()) ? baseRequest.getFlowId() : null);
            zhongYuanUpload.setSysId(StringUtils.hasText(baseRequest.getSysId()) ? baseRequest.getSysId() : null);
            zhongYuanUpload.setTimestamp(StringUtils.hasText(baseRequest.getTimestamp()) ? baseRequest.getTimestamp() : null);
            zhongYuanUpload.setChannelNo(StringUtils.hasText(baseRequest.getChannelNo()) ? baseRequest.getChannelNo() : null);
            zhongYuanUpload.setVersion(StringUtils.hasText(baseRequest.getVersion()) ? baseRequest.getVersion() : null);
            zhongYuanUpload.setToken(StringUtils.hasText(baseRequest.getToken()) ? baseRequest.getToken() : null);
            // 从batchData获取批次相关字段
            zhongYuanUpload.setBatchName(StringUtils.hasText(batchData.getBatchName()) ? batchData.getBatchName() : null);
            zhongYuanUpload.setBatchNo(batchData.getBatchNo());
            zhongYuanUpload.setSceneCode(StringUtils.hasText(batchData.getSceneCode()) ? batchData.getSceneCode() : null);
            zhongYuanUpload.setStartTime(StringUtils.hasText(batchData.getStartTime()) ? batchData.getStartTime() : null);
            zhongYuanUpload.setEndTime(StringUtils.hasText(batchData.getEndTime()) ? batchData.getEndTime() : null);
            zhongYuanUpload.setFestivalBan(batchData.getFestivalBan() != null ? String.valueOf(batchData.getFestivalBan()) : null);
            zhongYuanUpload.setPriority(batchData.getPriority() != null ? String.valueOf(batchData.getPriority()) : null);
            zhongYuanUpload.setReportEndFlag(StringUtils.hasText(batchData.getReportEndFlag()) ? batchData.getReportEndFlag() : null);
            zhongYuanUpload.setCreateTime(new Date());
            zhongYuanUpload.setUpdateTime(new Date());

            // 将taskDataList转换为JSON字符串
            if (batchData.getTaskDataList() != null && !batchData.getTaskDataList().isEmpty()) {
                zhongYuanUpload.setTaskdataList(JSON.toJSONString(batchData.getTaskDataList()));
            }

            // 保存到数据库
            int insertResult = zhongYuanUploadMapper.insertSelective(zhongYuanUpload);
            log.warn("中原消金批量任务数据入库成功，batchNo: {}, insertResult: {}, id: {}",
                    batchData.getBatchNo(), insertResult, zhongYuanUpload.getId());

            // 5. 数据清洗和推送逻辑
            buildPushUpload(apiCode, batchData, baseRequest);

            // 6. 构建响应
            List<BatchTaskResponse.TaskInfo> taskInfoList = new ArrayList<>();
            for (BatchTaskRequest.TaskData taskData : batchData.getTaskDataList()) {
                BatchTaskResponse.TaskInfo taskInfo = new BatchTaskResponse.TaskInfo();
                taskInfo.setTaskUid(taskData.getTaskNo());
                taskInfo.setTaskNo(taskData.getTaskNo());
                taskInfo.setTelNo(taskData.getTelNo());
                taskInfoList.add(taskInfo);
            }

            // 7. 组装响应
            BatchTaskResponse batchTaskResponse = new BatchTaskResponse();
            batchTaskResponse.setBatchNo(batchData.getBatchNo());
            batchTaskResponse.setBatchUid(batchData.getBatchNo());
            batchTaskResponse.setTaskInfoList(taskInfoList);

            ZhongYuanBaseResponse<BatchTaskResponse> response = ZhongYuanBaseResponse.success(batchTaskResponse);

            // 埋点
            try {
                JSONObject condition = new JSONObject();
                condition.put("flowId", baseRequest.getFlowId());
                trackingService.trackBusinessLog(DataFlowDirection.IN
                        , apiCode
                        , "中原消金-外呼上报接口"
                        ,"b_marketing_zhongyuan_upload"
                        , JSON.toJSONString(condition)
                        , Long.valueOf(batchData.getTaskDataList().size())
                        , TrackingContext.generateBatchId());
            } catch (Exception ex) {
                log.warn(
                        AlertLog.buildWarnMessage(
                                AlarmSendCodeEnum.TRACKING_POINT_SERVICEERROR.getCode()
                                , ex.getMessage()
                                , "埋点异常")
                        , ex);
            }

            log.warn("中原消金批量任务上报成功，batchNo: {}, taskCount: {}",
                    batchData.getBatchNo(), taskInfoList.size());
            return response;

        } catch (Exception e) {
            log.error("中原消金批量任务上报接口异常", e);
            return ZhongYuanBaseResponse.fail(ZhongYuanResponseCodeEnum.SYSTEM_ERROR.getCode(),
                    ZhongYuanResponseCodeEnum.SYSTEM_ERROR.getMessage() + "：" + e.getMessage());
        }
    }

    /**
     * 解析客户原始数据 组装成标准上传数据
     *
     * @param apiCode    商户编号
     * @param batchData  批次任务数据
     * @param baseRequest 基础请求对象
     * @return Map包含batchUid和taskUidMap，taskUidMap的key是taskNo，value是batchNumber（taskUid）
     */
    private void buildPushUpload(String apiCode, BatchTaskRequest batchData, ZhongYuanBaseRequest<BatchTaskRequest> baseRequest) {
        try {
            // 1. 构建标准上传数据结构
            JSONObject pushData = new JSONObject();

            // 2. batchNo=batchUid=上传taskId
            pushData.put("taskId", batchData.getBatchNo());

            // 3. 生成requestId: yyyymmdd_apicode_ + 5位随机数 + 毫秒时间戳
            String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            String random5Digits = String.format("%05d", new Random().nextInt(100000));
            String requestId = dateStr + "_" + apiCode + "_" + random5Digits + System.currentTimeMillis();
            pushData.put("requestId", requestId);

            // 4. 构建dataItems数组
            List<JSONObject> dataItems = new ArrayList<>();
            if (batchData.getTaskDataList() != null) {
                for (BatchTaskRequest.TaskData taskData : batchData.getTaskDataList()) {
                    Map<String, Object> itemResult = buildDataItem(taskData, batchData, baseRequest, apiCode);
                    if (itemResult != null) {
                        JSONObject dataItem = (JSONObject) itemResult.get("dataItem");
                        if (dataItem != null) {
                            dataItems.add(dataItem);
                        }
                    }
                }
            }
            pushData.put("dataItems", dataItems);

            // 5. 转换为JSON字符串并保存
            String jsonData = JSON.toJSONString(pushData);
            log.warn("中原消金数据清洗推送成功，jsonData: {}", jsonData);

            MarketingCustomerOriginalData originalData = new MarketingCustomerOriginalData();
            originalData.setApiCode(apiCode);
            originalData.setRequestId(requestId);
            originalData.setJsonData(jsonData);
            if(batchData.getTaskDataList() != null){
                originalData.setActualNum(batchData.getTaskDataList().size());
            }
            originalData.setDataType(DataProcessEnum.DataTypeEnum.UPLOAD.getCode());
            originalData.setAcceptType(DataProcessEnum.AcceptTypeEnum.CUSTOM.getCode());
            originalData.setReceiveDate(LocalDate.now().toString());
            marketingCustomerOriginalDataMapper.insertSelective(originalData);
        } catch (Exception e) {
            log.error("中原消金数据清洗推送异常", e);
        }
    }

    /**
     * 构建单个dataItem对象
     *
     * @param taskData   任务数据
     * @param batchData  批次数据
     * @param baseRequest 基础请求对象
     * @param apiCode    商户编号
     * @return Map包含dataItem和batchNumber
     */
    private Map<String, Object> buildDataItem(BatchTaskRequest.TaskData taskData,
                                              BatchTaskRequest batchData,
                                              ZhongYuanBaseRequest<BatchTaskRequest> baseRequest,
                                              String apiCode) {
        Map<String, Object> result = new HashMap<>();
        try {
            JSONObject dataItem = new JSONObject();

            // 1. cell: 使用telNo
            if (StringUtils.hasText(taskData.getTelNo())) {
                dataItem.put("cell", taskData.getTelNo());
            }

            // 2. custNum: 使用taskNo
            if (StringUtils.hasText(taskData.getTaskNo())) {
                dataItem.put("custNum", taskData.getTaskNo());
            }

            // 3. operateType: 固定值"5"
            dataItem.put("operateType", "5");

            // 4. 构建reserveField1
            Map<String, Object> reserveFieldResult = buildReserveField1(taskData, batchData, baseRequest);
            JSONObject reserveField1 = (JSONObject) reserveFieldResult.get("reserveField1");
            dataItem.put("reserveField1", reserveField1);
            dataItem.put("reserveField2", "");

            result.put("dataItem", dataItem);
            return result;
        } catch (Exception e) {
            log.error("构建dataItem异常，taskNo: {}", taskData.getTaskNo(), e);
            return null;
        }
    }

    /**
     * 构建reserveField1对象
     * 除了taskId和batchNumber外，其他所有字段都直接放入reserveField1中
     *
     * @param taskData   任务数据
     * @param batchData  批次数据
     * @param baseRequest 基础请求对象
     * @return Map包含reserveField1和batchNumber
     */
    private Map<String, Object> buildReserveField1(BatchTaskRequest.TaskData taskData,
                                                   BatchTaskRequest batchData,
                                                   ZhongYuanBaseRequest<BatchTaskRequest> baseRequest
    ) {
        Map<String, Object> result = new HashMap<>();
        JSONObject reserveField1 = new JSONObject();

        // 1. 从baseRequest获取所有字段，直接放入reserveField1
        if (baseRequest != null) {
            if (StringUtils.hasText(baseRequest.getFlowId())) {
                reserveField1.put("flowId", baseRequest.getFlowId());
            }
            if (StringUtils.hasText(baseRequest.getSysId())) {
                reserveField1.put("sysId", baseRequest.getSysId());
            }
            if (StringUtils.hasText(baseRequest.getTimestamp())) {
                reserveField1.put("timestamp", baseRequest.getTimestamp());
            }
            if (StringUtils.hasText(baseRequest.getChannelNo())) {
                reserveField1.put("channelNo", baseRequest.getChannelNo());
            }
            if (StringUtils.hasText(baseRequest.getVersion())) {
                reserveField1.put("version", baseRequest.getVersion());
            }
            if (StringUtils.hasText(baseRequest.getToken())) {
                reserveField1.put("token", baseRequest.getToken());
            }
        }

        // 2. 从batchData获取所有字段，直接放入reserveField1
        if (batchData != null) {
            if (StringUtils.hasText(batchData.getBatchName())) {
                reserveField1.put("batchName", batchData.getBatchName());
            }
            if (StringUtils.hasText(batchData.getBatchNo())) {
                reserveField1.put("batchNo", batchData.getBatchNo());
            }
            if (StringUtils.hasText(batchData.getSceneCode())) {
                reserveField1.put("sceneCode", batchData.getSceneCode());
            }
            if (StringUtils.hasText(batchData.getStartTime())) {
                reserveField1.put("startTime", batchData.getStartTime());
            }
            if (StringUtils.hasText(batchData.getEndTime())) {
                reserveField1.put("endTime", batchData.getEndTime());
            }
            if (batchData.getFestivalBan() != null) {
                reserveField1.put("festivalBan", batchData.getFestivalBan());
            }
            if (batchData.getPriority() != null) {
                reserveField1.put("priority", batchData.getPriority());
            }
            if (StringUtils.hasText(batchData.getReportEndFlag())) {
                reserveField1.put("reportEndFlag", batchData.getReportEndFlag());
            }
        }

        // 3. 从taskData获取所有字段，直接放入reserveField1
        if (taskData != null) {
            if (StringUtils.hasText(taskData.getTaskNo())) {
                reserveField1.put("taskNo", taskData.getTaskNo());
            }
            if (StringUtils.hasText(taskData.getTelNo())) {
                reserveField1.put("telNo", taskData.getTelNo());
            }

            // 4. 从variableList获取所有变量，直接放入reserveField1
            if (taskData.getVariableList() != null) {
                for (BatchTaskRequest.Variable variable : taskData.getVariableList()) {
                    if (variable != null && StringUtils.hasText(variable.getCode()) && StringUtils.hasText(variable.getValue())) {
                        // 如果字段已存在，跳过（避免覆盖）
                        if (!reserveField1.containsKey(variable.getCode())) {
                            reserveField1.put(variable.getCode(), variable.getValue());
                        }
                    }
                }
            }
        }
        result.put("reserveField1", reserveField1);
        return result;
    }

    @Override
    public ZhongYuanBaseResponse<?> sceneVariable(String jsonData, HttpServletRequest request) {
        try {
            log.warn("中原消金场景变量查询接口请求，jsonData: {}", jsonData);

            // 1. 解析请求数据
            ZhongYuanBaseRequest<SceneVariableRequest> baseRequest = JSON.parseObject(jsonData,
                    new com.alibaba.fastjson.TypeReference<ZhongYuanBaseRequest<SceneVariableRequest>>() {
                    });

            if (baseRequest == null || baseRequest.getData() == null) {
                return ZhongYuanBaseResponse.fail(ZhongYuanResponseCodeEnum.PARAM_ERROR.getCode(), ZhongYuanResponseCodeEnum.PARAM_ERROR.getMessage());
            }

            // 2. Token验证
            ZhongYuanBaseResponse<?> tokenResponse = validateTokenFromRequest(baseRequest);
            if (!ZhongYuanResponseCodeEnum.SUCCESS.getCode().equals(tokenResponse.getCode())) {
                return tokenResponse;
            }

            SceneVariableRequest sceneData = baseRequest.getData();

            // 3. 参数校验
            if (!StringUtils.hasText(sceneData.getSceneCode())) {
                return ZhongYuanBaseResponse.fail(ZhongYuanResponseCodeEnum.PARAM_ERROR.getCode(), "参数错误：场景代码为空");
            }

            // 4. 查询场景变量
            List<SceneVariableResponse> sceneVariableList = getSceneVariables();

            // 5. 构建响应
            ZhongYuanBaseResponse<List<SceneVariableResponse>> response = ZhongYuanBaseResponse.success(sceneVariableList);

            try {
                Map<String, String> zhongYuanIdentity = marketingCommonConfig.getZhongYuanIdentity();
                String apiCode = zhongYuanIdentity.get("apiCode");
                String remark = String.format("中原消金-场景变量信息接口，sceneCode：%s", sceneData.getSceneCode());
                trackingService.trackPointLog(DataFlowDirection.IN
                        , apiCode
                        , "中原消金-场景变量信息接口"
                        , 1L
                        , remark
                        , TrackingContext.generateBatchId());
            } catch (Exception ex) {
                log.warn(
                        AlertLog.buildWarnMessage(
                                AlarmSendCodeEnum.TRACKING_POINT_SERVICEERROR.getCode()
                                , ex.getMessage()
                                , "埋点异常")
                        , ex);
            }

            log.warn("中原消金场景变量查询成功，sceneCode: {}, variableCount: {}",
                    sceneData.getSceneCode(), sceneVariableList.size());

            return response;

        } catch (Exception e) {
            log.error("中原消金场景变量查询接口异常", e);
            return ZhongYuanBaseResponse.fail(ZhongYuanResponseCodeEnum.SYSTEM_ERROR.getCode(), ZhongYuanResponseCodeEnum.SYSTEM_ERROR.getMessage() + "：" + e.getMessage());
        }
    }

    /**
     * 验证用户名和密码
     *
     * @param appUser 应用用户
     * @param appKey  应用密钥
     * @return 是否有效
     */
    private boolean validateCredentials(String appUser, String appKey) {
        // 从配置文件读取的appUser和appKey进行验证
        Map<String, String> zhongYuanIdentity = marketingCommonConfig.getZhongYuanIdentity();
        String configAppUser = zhongYuanIdentity.get("appUser");
        String configAppKey = zhongYuanIdentity.get("appKey");
        return configAppUser.equals(appUser) && configAppKey.equals(appKey);
    }

    /**
     * 验证Token（公共方法）
     * 从baseRequest或request参数中获取token并验证
     *
     * @param baseRequest 基础请求对象
     * @return 验证结果响应，成功返回success响应，失败返回fail响应
     */
    private ZhongYuanBaseResponse<?> validateTokenFromRequest(ZhongYuanBaseRequest<?> baseRequest) {
        // 1. 从baseRequest获取token
        String token = baseRequest != null ? baseRequest.getToken() : null;

        // 2. Token为空检查
        if (!StringUtils.hasText(token)) {
            return ZhongYuanBaseResponse.fail(ZhongYuanResponseCodeEnum.TOKEN_INVALID.getCode(), ZhongYuanResponseCodeEnum.TOKEN_INVALID.getMessage());
        }

        // 3. 验证Token
        try {
            validateToken(token);
            // 验证成功，返回success响应
            return ZhongYuanBaseResponse.success(null);
        } catch (RuntimeException e) {
            String errorMsg = e.getMessage();
            if (errorMsg != null && errorMsg.contains(ZhongYuanResponseCodeEnum.TOKEN_INVALID.getCode())) {
                return ZhongYuanBaseResponse.fail(ZhongYuanResponseCodeEnum.TOKEN_INVALID.getCode(), errorMsg.substring(errorMsg.indexOf(":") + 1));
            }
            return ZhongYuanBaseResponse.fail(ZhongYuanResponseCodeEnum.TOKEN_INVALID.getCode(), ZhongYuanResponseCodeEnum.TOKEN_INVALID.getMessage());
        }
    }

    /**
     * 获取场景变量列表
     *
     * @return 场景变量列表
     */
    private List<SceneVariableResponse> getSceneVariables() {
        // 查询数据库（这里使用固定数据，实际应该从数据库查询）
        List<SceneVariableResponse> variableList = new ArrayList<>();
        variableList.add(createSceneVariable("custName", "客户姓名", null));
        variableList.add(createSceneVariable("gender", "客户性别", null));
        variableList.add(createSceneVariable("overDays", "逾期天数", null));
        variableList.add(createSceneVariable("overAmt", "逾期金额", null));
        variableList.add(createSceneVariable("compName", "企业名称", "中原消费金融"));
        variableList.add(createSceneVariable("compTel", "客服电话", "4001112233"));
        return variableList;
    }

    /**
     * 创建场景变量对象
     *
     * @param code     变量代码
     * @param name     变量名称
     * @param defValue 默认值
     * @return 场景变量对象
     */
    private SceneVariableResponse createSceneVariable(String code, String name, String defValue) {
        SceneVariableResponse variable = new SceneVariableResponse();
        variable.setCode(code);
        variable.setName(name);
        variable.setDefValue(defValue);
        return variable;
    }

    /**
     * 生成Token
     * Token = SHA256(appUser + "_" + timestamp + "_" + uuid).substring(0, 32)
     *
     * @param appUser 应用用户
     * @return Token值
     */
    public String generateToken(String appUser) {
        // 1. 生成UUID
        String uuid = UUID.randomUUID().toString().replace("-", "");

        // 2. 获取当前时间戳
        long timestamp = System.currentTimeMillis();

        // 3. 组合字符串：appUser + "_" + timestamp + "_" + uuid
        String rawToken = appUser + "_" + timestamp + "_" + uuid;

        // 4. SHA256加密并截取前32位
        String token = DigestUtils.sha256Hex(rawToken).substring(0, 32);

        log.warn("生成Token成功，appUser: {}, token: {}", appUser, token);
        return token;
    }

    /**
     * 校验Token
     *
     * @param token Token值
     * @throws RuntimeException Token校验失败时抛出异常
     */
    public void validateToken(String token) {
        // 1. Token存在性校验
        if (!StringUtils.hasText(token)) {
            throw new RuntimeException(ZhongYuanResponseCodeEnum.TOKEN_INVALID.getCode() + ":Token无效");
        }

        // 2. Token格式校验
        if (!isValidTokenFormat(token)) {
            throw new RuntimeException(ZhongYuanResponseCodeEnum.TOKEN_INVALID.getCode() + ":Token格式错误");
        }

        // 3. 从配置获取appUser，然后查询Redis验证token
        Map<String, String> zhongYuanIdentity = marketingCommonConfig.getZhongYuanIdentity();
        String appUser = zhongYuanIdentity.get("appUser");

        if (!StringUtils.hasText(appUser)) {
            throw new RuntimeException(ZhongYuanResponseCodeEnum.TOKEN_INVALID.getCode() + ":系统配置错误");
        }

        // 4. 从Redis获取存储的token
        String userTokenKey = TOKEN_PREFIX + appUser;
        String storedToken = redisChgService.get(userTokenKey);

        if (!StringUtils.hasText(storedToken)) {
            throw new RuntimeException(ZhongYuanResponseCodeEnum.TOKEN_INVALID.getCode() + ":Token无效或已过期");
        }

        // 5. 验证token是否匹配
        if (!token.equals(storedToken)) {
            throw new RuntimeException(ZhongYuanResponseCodeEnum.TOKEN_INVALID.getCode() + ":Token无效");
        }

        // 6. 更新过期时间（续期）
        redisChgService.setex(userTokenKey, token, (int) TOKEN_EXPIRE_TIME);

        log.warn("Token校验成功，token: {}, appUser: {}", token, appUser);
    }

    /**
     * 校验Token格式
     *
     * @param token Token值
     * @return 是否有效
     */
    private boolean isValidTokenFormat(String token) {
        // 长度必须是32位
        if (token.length() != 32) {
            return false;
        }
        // 字符集：0-9, a-f
        return token.matches("^[0-9a-f]{32}$");
    }

    @Override
    public ZhongYuanBaseResponse<?> status(String jsonData, HttpServletRequest request) {
        try {
            log.warn("中原消金批量外呼任务状态修改接口请求，jsonData: {}", jsonData);

            // 1. 解析请求数据
            ZhongYuanBaseRequest<TaskStatusRequest> baseRequest = JSON.parseObject(jsonData,
                    new com.alibaba.fastjson.TypeReference<ZhongYuanBaseRequest<TaskStatusRequest>>() {
                    });

            if (baseRequest == null || baseRequest.getData() == null) {
                return ZhongYuanBaseResponse.fail(ZhongYuanResponseCodeEnum.PARAM_ERROR.getCode(), ZhongYuanResponseCodeEnum.PARAM_ERROR.getMessage());
            }

            // 2. Token验证
            ZhongYuanBaseResponse<?> tokenResponse = validateTokenFromRequest(baseRequest);
            if (!ZhongYuanResponseCodeEnum.SUCCESS.getCode().equals(tokenResponse.getCode())) {
                return tokenResponse;
            }

            TaskStatusRequest statusData = baseRequest.getData();

            // 2.1 保存原始数据到b_marketing_zhongyuan_transfer表
            ZhongYuanTransfer zhongYuanTransfer = new ZhongYuanTransfer();
            Map<String, String> zhongYuanIdentity = marketingCommonConfig.getZhongYuanIdentity();
            String testApiCode = request.getHeader("Test-ApiCode");
            String apiCode = testApiCode != null ? testApiCode : zhongYuanIdentity.get("apiCode");
            zhongYuanTransfer.setApiCode(apiCode);
            // 从baseRequest获取公共字段
            zhongYuanTransfer.setFlowId(StringUtils.hasText(baseRequest.getFlowId()) ? baseRequest.getFlowId() : null);
            zhongYuanTransfer.setSysId(StringUtils.hasText(baseRequest.getSysId()) ? baseRequest.getSysId() : null);
            zhongYuanTransfer.setTimestamp(StringUtils.hasText(baseRequest.getTimestamp()) ? baseRequest.getTimestamp() : null);
            zhongYuanTransfer.setChannelNo(StringUtils.hasText(baseRequest.getChannelNo()) ? baseRequest.getChannelNo() : null);
            zhongYuanTransfer.setVersion(StringUtils.hasText(baseRequest.getVersion()) ? baseRequest.getVersion() : null);
            zhongYuanTransfer.setToken(StringUtils.hasText(baseRequest.getToken()) ? baseRequest.getToken() : null);
            // 从statusData获取状态修改相关字段
            zhongYuanTransfer.setBatchUid(StringUtils.hasText(statusData.getBatchUid()) ? statusData.getBatchUid() : null);
            zhongYuanTransfer.setOperation(StringUtils.hasText(statusData.getOperation()) ? statusData.getOperation() : null);
            // 将taskUidList转换为JSON字符串
            if (statusData.getTaskUidList() != null && !statusData.getTaskUidList().isEmpty()) {
                zhongYuanTransfer.setTaskuidList(JSON.toJSONString(statusData.getTaskUidList()));
            }
            zhongYuanTransfer.setCleanStatus(0); // 0-待清洗
            zhongYuanTransfer.setCreateTime(new Date());
            zhongYuanTransfer.setUpdateTime(new Date());

            // 保存到数据库
            int insertResult = zhongYuanTransferMapper.insertSelective(zhongYuanTransfer);
            log.warn("中原消金批量外呼任务状态修改数据入库成功，operation: {}, taskUidCount: {}, insertResult: {}, id: {}",
                    statusData.getOperation(),
                    statusData.getTaskUidList() != null ? statusData.getTaskUidList().size() : 0,
                    insertResult, zhongYuanTransfer.getId());

            // 3. 参数校验
            if (statusData.getTaskUidList() == null || statusData.getTaskUidList().isEmpty()) {
                return ZhongYuanBaseResponse.fail(ZhongYuanResponseCodeEnum.PARAM_ERROR.getCode(), "参数错误：任务UID列表为空");
            }

            if (!StringUtils.hasText(statusData.getOperation()) || !"cancel".equals(statusData.getOperation())) {
                return ZhongYuanBaseResponse.fail(ZhongYuanResponseCodeEnum.PARAM_ERROR.getCode(), "参数错误：操作类型必须为cancel");
            }

            // 4. （taskUid = custNum）
            List<String> taskUidList = statusData.getTaskUidList();

            if (taskUidList.isEmpty()) {
                return ZhongYuanBaseResponse.fail(ZhongYuanResponseCodeEnum.PARAM_ERROR.getCode(), "参数错误：任务UID列表格式错误");
            }

            // 5. 查询通话明细记录
            CallRecordLLMResultV2Example example = new CallRecordLLMResultV2Example();
            example.createCriteria().andApiCodeEqualTo(apiCode).andCustNumIn(taskUidList)
                    .andReceiveDateEqualTo(LocalDate.now().toString());
            // 按createTime降序排序，确保获取最新的一条
            example.setOrderByClause("create_time DESC");
            List<CallRecordLLMResultV2> callRecordLLMResultV2s = callRecordLLMResultV2Mapper.selectByExample(example);

            // 6. 构建custNum到CallRecording的映射,taskUid=taskNo=上传custNum
            // 由于已按createTime降序排序，同一custNum的第一条记录就是最新的
            Map<String, CallRecordLLMResultV2> taskUidToRecordingMap = new HashMap<>();
            for (CallRecordLLMResultV2 recording : callRecordLLMResultV2s) {
                if (recording.getCustNum() != null && !taskUidToRecordingMap.containsKey(recording.getCustNum())) {
                    // 如果已存在该custNum的记录，跳过（因为已排序，第一条就是最新的）
                    taskUidToRecordingMap.put(recording.getCustNum(), recording);
                }
            }

            // 7. 处理每个taskUid，判断是否可以剔除
            List<TaskStatusResponse.FailInfo> failList = new ArrayList<>();
            int successCount = 0;
            int failCount = 0;

            for (String taskUid : taskUidList) {
                CallRecordLLMResultV2 recording = taskUidToRecordingMap.get(taskUid);

                // 7.1 当callStatus>=12或无通话明细，返回操作成功
                if (recording == null || recording.getCallStatus() == null || recording.getCallStatus() >= 12) {
                    successCount++;
                } else {
                    // 7.2 当callStatus<12，返回已拨号完毕无法剔除
                    TaskStatusResponse.FailInfo failInfo = new TaskStatusResponse.FailInfo();
                    failInfo.setTaskUid(taskUid);
                    failInfo.setMessage("已拨号完毕无法剔除");
                    failList.add(failInfo);
                    failCount++;
                }
            }

            // 8. 构建响应
            TaskStatusResponse responseData = new TaskStatusResponse();
            responseData.setFailList(failList);

            // 9. 根据成功和失败情况返回不同的响应码
            ZhongYuanResponseCodeEnum responseCodeEnum;
            int size = taskUidList.size();
            if (size == successCount) {
                // 全部成功
                responseCodeEnum = ZhongYuanResponseCodeEnum.SUCCESS;
            } else if (size == failCount) {
                // 全部失败
                responseCodeEnum = ZhongYuanResponseCodeEnum.ALL_FAILED;
            } else {
                // 部分成功
                responseCodeEnum = ZhongYuanResponseCodeEnum.PARTIAL_SUCCESS;
            }

            ZhongYuanBaseResponse<TaskStatusResponse> response = new ZhongYuanBaseResponse<>();
            response.setCode(responseCodeEnum.getCode());
            response.setMessage(responseCodeEnum.getMessage());
            response.setData(responseData);

            // 埋点
            try {
                JSONObject condition = new JSONObject();
                condition.put("flowId", baseRequest.getFlowId());
                trackingService.trackBusinessLog(DataFlowDirection.IN
                        , apiCode
                        , "中原消金-批量外呼任务状态修改接口"
                        ,"b_marketing_zhongyuan_transfer"
                        , JSON.toJSONString(condition)
                        , Long.valueOf(statusData.getTaskUidList().size())
                        , TrackingContext.generateBatchId());
            } catch (Exception ex) {
                log.warn(
                        AlertLog.buildWarnMessage(
                                AlarmSendCodeEnum.TRACKING_POINT_SERVICEERROR.getCode()
                                , ex.getMessage()
                                , "埋点异常")
                        , ex);
            }

            log.warn("中原消金批量外呼任务状态修改完成，成功数: {}, 失败数: {}, 响应码: {}",
                    successCount, failCount, responseCodeEnum.getCode());

            return response;

        } catch (Exception e) {
            log.error("中原消金批量外呼任务状态修改接口异常", e);
            return ZhongYuanBaseResponse.fail(ZhongYuanResponseCodeEnum.SYSTEM_ERROR.getCode(), ZhongYuanResponseCodeEnum.SYSTEM_ERROR.getMessage() + "：" + e.getMessage());
        }
    }

    @Override
    public ZhongYuanBaseResponse<?> changeSceneVariable(String jsonData, HttpServletRequest request) {
        try {
            log.warn("中原消金外呼任务场景变量修改接口请求，jsonData: {}", jsonData);

            // 1. 解析请求数据
            ZhongYuanBaseRequest<ChangeSceneVariableRequest> baseRequest = JSON.parseObject(jsonData,
                    new com.alibaba.fastjson.TypeReference<ZhongYuanBaseRequest<ChangeSceneVariableRequest>>() {
                    });

            if (baseRequest == null || baseRequest.getData() == null) {
                return ZhongYuanBaseResponse.fail(ZhongYuanResponseCodeEnum.PARAM_ERROR.getCode(),
                        ZhongYuanResponseCodeEnum.PARAM_ERROR.getMessage());
            }

            // 2. Token验证
            ZhongYuanBaseResponse<?> tokenResponse = validateTokenFromRequest(baseRequest);
            if (!ZhongYuanResponseCodeEnum.SUCCESS.getCode().equals(tokenResponse.getCode())) {
                return tokenResponse;
            }

            ChangeSceneVariableRequest changeData = baseRequest.getData();

            // 3. 参数校验
            if (!StringUtils.hasText(changeData.getTaskUid())) {
                return ZhongYuanBaseResponse.fail(ZhongYuanResponseCodeEnum.PARAM_ERROR.getCode(),
                        "参数错误：taskUid为空");
            }

            if (!StringUtils.hasText(changeData.getSceneCode())) {
                return ZhongYuanBaseResponse.fail(ZhongYuanResponseCodeEnum.PARAM_ERROR.getCode(),
                        "参数错误：sceneCode为空");
            }

            if (changeData.getVariableList() == null || changeData.getVariableList().isEmpty()) {
                return ZhongYuanBaseResponse.fail(ZhongYuanResponseCodeEnum.PARAM_ERROR.getCode(),
                        "参数错误：variableList为空");
            }

            // 4. 获取apiCode
            Map<String, String> zhongYuanIdentity = marketingCommonConfig.getZhongYuanIdentity();
            String testApiCode = request.getHeader("Test-ApiCode");
            String apiCode = testApiCode != null ? testApiCode : zhongYuanIdentity.get("apiCode");

            // 5. 记录变更数据到数据库
            MarketingSceneVariable marketingSceneVariable = new MarketingSceneVariable();
            marketingSceneVariable.setApiCode(apiCode);
            marketingSceneVariable.setFlowId(baseRequest.getFlowId());
            marketingSceneVariable.setSysId(baseRequest.getSysId());
            marketingSceneVariable.setTimestamp(baseRequest.getTimestamp());
            marketingSceneVariable.setChannelNo(baseRequest.getChannelNo());
            marketingSceneVariable.setVersion(baseRequest.getVersion());
            marketingSceneVariable.setToken(baseRequest.getToken());
            marketingSceneVariable.setSceneCode(changeData.getSceneCode());
            marketingSceneVariable.setTaskUid(changeData.getTaskUid());
            // 将变量列表转换为JSON字符串存储
            marketingSceneVariable.setVariableList(JSON.toJSONString(changeData.getVariableList()));
            marketingSceneVariable.setExecuteStatus(0); // 0-待执行
            marketingSceneVariable.setCreateTime(new Date());
            marketingSceneVariable.setUpdateTime(new Date());

            // 执行插入操作
            int insertResult = marketingSceneVariableMapper.insertSelective(marketingSceneVariable);
            if (insertResult <= 0) {
                log.error("中原消金外呼任务场景变量修改失败，插入数据库失败，taskUid: {}, sceneCode: {}",
                        changeData.getTaskUid(), changeData.getSceneCode());
                return ZhongYuanBaseResponse.fail(ZhongYuanResponseCodeEnum.SYSTEM_ERROR.getCode(),
                        "数据更新失败");
            }

            // 6. 构建响应
            ChangeSceneVariableResponse responseData = new ChangeSceneVariableResponse();
            responseData.setTaskUid(changeData.getTaskUid());
            responseData.setEw("更新成功");

            // 埋点
            try {
                JSONObject condition = new JSONObject();
                condition.put("flowId", baseRequest.getFlowId());
                trackingService.trackBusinessLog(DataFlowDirection.IN
                        , apiCode
                        , "中原消金-外呼任务场景变量修改接口"
                        ,"b_marketing_scene_variable"
                        , JSON.toJSONString(condition)
                        , 1L
                        , TrackingContext.generateBatchId());
            } catch (Exception ex) {
                log.warn(
                        AlertLog.buildWarnMessage(
                                AlarmSendCodeEnum.TRACKING_POINT_SERVICEERROR.getCode()
                                , ex.getMessage()
                                , "埋点异常")
                        , ex);
            }

            log.warn("中原消金外呼任务场景变量修改成功，taskUid: {}, sceneCode: {}, variableList: {}",
                    changeData.getTaskUid(), changeData.getSceneCode(), JSON.toJSONString(changeData.getVariableList()));

            return ZhongYuanBaseResponse.success(responseData);

        } catch (Exception e) {
            log.error("中原消金外呼任务场景变量修改接口异常", e);
            return ZhongYuanBaseResponse.fail(ZhongYuanResponseCodeEnum.SYSTEM_ERROR.getCode(),
                    ZhongYuanResponseCodeEnum.SYSTEM_ERROR.getMessage() + "：" + e.getMessage());
        }
    }

}
