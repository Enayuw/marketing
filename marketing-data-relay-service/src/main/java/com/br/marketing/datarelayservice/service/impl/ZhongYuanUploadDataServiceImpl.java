package com.br.marketing.datarelayservice.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.common.utils.DateHelper;
import com.br.marketing.datarelayservice.enums.ZhongYuanResponseCodeEnum;
import com.br.marketing.datarelayservice.service.ZhongYuanUploadDataService;
import com.br.marketing.dto.zhongyuan.*;
import com.br.marketing.entity.MarketingCustomerOriginalData;
import com.br.marketing.entity.ZhongYuanUpload;
import com.br.marketing.enums.clean.DataProcessEnum;
import com.br.marketing.mapper.ZhongYuanUploadMapper;
import com.br.marketing.mapper.rulecleaning.MarketingCustomerOriginalDataMapper;
import com.br.marketing.rule.ai.policy.OperateSixProcessor;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

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
    MarketingCustomerOriginalDataMapper marketingCustomerOriginalDataMapper;
    @Resource
    OperateSixProcessor operateSixProcessor;
    @Resource
    private MarketingCommonConfig marketingCommonConfig;
    private static final String TOKEN_PREFIX = "zyxj:token:";
    private static final long TOKEN_EXPIRE_TIME = 7200; // 2小时

    @Override
    public ZhongYuanBaseResponse<?> login(String jsonData) {
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

            return ZhongYuanBaseResponse.success(loginResponse);

        } catch (Exception e) {
            log.error("中原消金登录接口异常", e);
            return ZhongYuanBaseResponse.fail(ZhongYuanResponseCodeEnum.SYSTEM_ERROR.getCode(), ZhongYuanResponseCodeEnum.SYSTEM_ERROR.getMessage() + "：" + e.getMessage());
        }
    }

    @Override
    public ZhongYuanBaseResponse<?> batchTask(String jsonData) {
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
            String apiCode = zhongYuanIdentity.get("apiCode");
            zhongYuanUpload.setApiCode(StringUtils.hasText(apiCode) ? apiCode : "3760019");
            // 从baseRequest获取公共字段
            zhongYuanUpload.setFlowid(StringUtils.hasText(baseRequest.getFlowId()) ? baseRequest.getFlowId() : null);
            zhongYuanUpload.setSysid(StringUtils.hasText(baseRequest.getSysId()) ? baseRequest.getSysId() : null);
            zhongYuanUpload.setTimestamp(StringUtils.hasText(baseRequest.getTimestamp()) ? baseRequest.getTimestamp() : null);
            zhongYuanUpload.setChannelno(StringUtils.hasText(baseRequest.getChannelNo()) ? baseRequest.getChannelNo() : null);
            zhongYuanUpload.setVersion(StringUtils.hasText(baseRequest.getVersion()) ? baseRequest.getVersion() : null);
            zhongYuanUpload.setToken(StringUtils.hasText(baseRequest.getToken()) ? baseRequest.getToken() : null);
            // 从batchData获取批次相关字段
            zhongYuanUpload.setBatchname(StringUtils.hasText(batchData.getBatchName()) ? batchData.getBatchName() : null);
            zhongYuanUpload.setBatchno(batchData.getBatchNo());
            zhongYuanUpload.setScenecode(StringUtils.hasText(batchData.getSceneCode()) ? batchData.getSceneCode() : null);
            zhongYuanUpload.setStarttime(StringUtils.hasText(batchData.getStartTime()) ? batchData.getStartTime() : null);
            zhongYuanUpload.setEndtime(StringUtils.hasText(batchData.getEndTime()) ? batchData.getEndTime() : null);
            zhongYuanUpload.setFestivalban(batchData.getFestivalBan() != null ? String.valueOf(batchData.getFestivalBan()) : null);
            zhongYuanUpload.setPriority(batchData.getPriority() != null ? String.valueOf(batchData.getPriority()) : null);
            zhongYuanUpload.setReportendflag(StringUtils.hasText(batchData.getReportEndFlag()) ? batchData.getReportEndFlag() : null);
            zhongYuanUpload.setCreateTime(new Date());
            zhongYuanUpload.setUpdateTime(new Date());

            // 将taskDataList转换为JSON字符串
            if (batchData.getTaskDataList() != null && !batchData.getTaskDataList().isEmpty()) {
                zhongYuanUpload.setTaskdatalist(JSON.toJSONString(batchData.getTaskDataList()));
            }

            // 保存到数据库
            int insertResult = zhongYuanUploadMapper.insertSelective(zhongYuanUpload);
            log.warn("中原消金批量任务数据入库成功，batchNo: {}, insertResult: {}, id: {}", 
                    batchData.getBatchNo(), insertResult, zhongYuanUpload.getId());

            // 5. 数据清洗和推送逻辑，返回batchUid和每条数据的taskUid映射
            Map<String, Object> uidMap = buildPushUpload(apiCode, batchData);
            String batchUid = (String) uidMap.get("batchUid");
            Map<String, String> taskUidMap = (Map<String, String>) uidMap.get("taskUidMap");

            // 6. 构建响应
            List<BatchTaskResponse.TaskInfo> taskInfoList = new ArrayList<>();
            for (BatchTaskRequest.TaskData taskData : batchData.getTaskDataList()) {
                BatchTaskResponse.TaskInfo taskInfo = new BatchTaskResponse.TaskInfo();
                // taskUid的值是每条数据的batchNumber
                String taskUid = null;
                if (StringUtils.hasText(taskData.getTaskNo()) && taskUidMap != null) {
                    // 优先通过taskNo获取
                    taskUid = taskUidMap.get(taskData.getTaskNo());
                }
                taskInfo.setTaskUid(taskUid);
                taskInfo.setTaskNo(taskData.getTaskNo());
                taskInfo.setTelNo(taskData.getTelNo());
                taskInfoList.add(taskInfo);
            }

            // 7. 组装响应
            BatchTaskResponse batchTaskResponse = new BatchTaskResponse();
            batchTaskResponse.setBatchNo(batchData.getBatchNo());
            batchTaskResponse.setBatchUid(batchUid);
            batchTaskResponse.setTaskInfoList(taskInfoList);

            ZhongYuanBaseResponse<BatchTaskResponse> response = ZhongYuanBaseResponse.success(batchTaskResponse);

            log.warn("中原消金批量任务上报成功，batchNo: {}, batchUid: {}, taskCount: {}",
                    batchData.getBatchNo(), batchUid, taskInfoList.size());
            return response;

        } catch (Exception e) {
            log.error("中原消金批量任务上报接口异常", e);
            return ZhongYuanBaseResponse.fail(ZhongYuanResponseCodeEnum.SYSTEM_ERROR.getCode(), ZhongYuanResponseCodeEnum.SYSTEM_ERROR.getMessage() + "：" + e.getMessage());
        }
    }

    /**
     * 解析客户原始数据 组装成标准上传数据
     *
     * @param apiCode    商户编号
     * @param batchData  批次任务数据
     * @return Map包含batchUid和taskUidMap，taskUidMap的key是taskNo，value是batchNumber（taskUid）
     */
    private Map<String, Object> buildPushUpload(String apiCode, BatchTaskRequest batchData) {
        Map<String, Object> result = new HashMap<>();
        try {
            // 1. 构建标准上传数据结构
            JSONObject pushData = new JSONObject();
            
            // 2. 生成taskId: yyyymmdd_apicode_sceneCode，作为batchUid
            String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            String sceneCode = StringUtils.hasText(batchData.getSceneCode()) ? batchData.getSceneCode() : "";
            String taskId = dateStr + "_" + apiCode + "_" + sceneCode;
            pushData.put("taskId", taskId);
            // batchUid的值是taskId
            result.put("batchUid", taskId);
            
            // 3. 生成requestId: yyyymmdd_apicode_ + 5位随机数 + 毫秒时间戳
            String random5Digits = String.format("%05d", new Random().nextInt(100000));
            String requestId = dateStr + "_" + apiCode + "_" + random5Digits + System.currentTimeMillis();
            pushData.put("requestId", requestId);
            
            // 4. 设置total
            if (batchData.getTaskDataList() != null) {
                pushData.put("total", String.valueOf(batchData.getTaskDataList().size()));
            }
            
            // 5. 构建dataItems数组，同时收集taskUid映射（taskNo -> batchNumber，如果没有taskNo则使用索引）
            List<JSONObject> dataItems = new ArrayList<>();
            Map<String, String> taskUidMap = new HashMap<>();
            if (batchData.getTaskDataList() != null) {
                for (BatchTaskRequest.TaskData taskData : batchData.getTaskDataList()) {
                    Map<String, Object> itemResult = buildDataItem(taskData, batchData, apiCode);
                    if (itemResult != null) {
                        JSONObject dataItem = (JSONObject) itemResult.get("dataItem");
                        String batchNumber = (String) itemResult.get("batchNumber");
                        if (dataItem != null) {
                            dataItems.add(dataItem);
                        }
                        // 记录taskNo到batchNumber的映射（batchNumber就是taskUid）
                        if (StringUtils.hasText(taskData.getTaskNo()) && StringUtils.hasText(batchNumber)) {
                            taskUidMap.put(taskData.getTaskNo(), batchNumber);
                        }
                    }
                }
            }
            pushData.put("dataItems", dataItems);
            result.put("taskUidMap", taskUidMap);
            
            // 6. 转换为JSON字符串并保存
            String jsonData = JSON.toJSONString(pushData);
            log.warn("中原消金数据清洗推送成功，jsonData: {}", jsonData);

            MarketingCustomerOriginalData originalData = new MarketingCustomerOriginalData();
            originalData.setApiCode(apiCode);
            originalData.setRequestId(requestId);
            originalData.setJsonData(jsonData);
            originalData.setDataType(DataProcessEnum.DataTypeEnum.UPLOAD.getCode());
            originalData.setAcceptType(DataProcessEnum.AcceptTypeEnum.CUSTOM.getCode());
            originalData.setReceiveDate(LocalDate.now().toString());
            marketingCustomerOriginalDataMapper.insertSelective(originalData);
            
            log.warn("中原消金数据清洗推送成功，taskId: {}, requestId: {}, dataItemsCount: {}", 
                    taskId, requestId, dataItems.size());
        } catch (Exception e) {
            log.error("中原消金数据清洗推送异常", e);
        }
        return result;
    }

    /**
     * 构建单个dataItem对象
     *
     * @param taskData  任务数据
     * @param batchData 批次数据
     * @return Map包含dataItem和batchNumber
     */
    private Map<String, Object> buildDataItem(BatchTaskRequest.TaskData taskData,
                                              BatchTaskRequest batchData, String apiCode) {
        Map<String, Object> result = new HashMap<>();
        try {
            JSONObject dataItem = new JSONObject();
            
            // 1. cell: 电话号码的MD5值
            if (StringUtils.hasText(taskData.getTelNo())) {
                dataItem.put("cell", taskData.getTelNo());
            }
            
            // 2. custNum: 唯一用户ID（使用taskNo或生成taskUid）
            if (StringUtils.hasText(taskData.getTaskNo())) {
                dataItem.put("custNum", taskData.getTaskNo());
            }
            
            // 3. operateType: 固定值"6"
            dataItem.put("operateType", "6");
            
            // 4. 构建reserveField1，同时获取batchNumber
            Map<String, Object> reserveFieldResult = buildReserveField1(taskData, batchData, apiCode);
            JSONObject reserveField1 = (JSONObject) reserveFieldResult.get("reserveField1");
            String batchNumber = (String) reserveFieldResult.get("batchNumber");
            dataItem.put("reserveField1", reserveField1);
            
            // 5. reserveField2: 可选，默认为空字符串
            dataItem.put("reserveField2", "");

            result.put("dataItem", dataItem);
            result.put("batchNumber", batchNumber);
            return result;
        } catch (Exception e) {
            log.error("构建dataItem异常，taskNo: {}", taskData.getTaskNo(), e);
            return null;
        }
    }

    /**
     * 构建reserveField1对象
     *
     * @param taskData  任务数据
     * @param batchData 批次数据
     * @return Map包含reserveField1和batchNumber
     */
    private Map<String, Object> buildReserveField1(BatchTaskRequest.TaskData taskData,
                                                   BatchTaskRequest batchData,String apiCode) {
        Map<String, Object> result = new HashMap<>();
        JSONObject reserveField1 = new JSONObject();
        
        // 从变量列表中提取字段值
        Map<String, String> variableMap = new HashMap<>();
        if (taskData.getVariableList() != null) {
            variableMap = taskData.getVariableList().stream()
                    .collect(Collectors.toMap(
                            BatchTaskRequest.Variable::getCode,
                            BatchTaskRequest.Variable::getValue,
                            (v1, v2) -> v1));
        }

        // 记录已处理的字段名，用于后续过滤
        Set<String> processedFields = new HashSet<>();

        // firstName: 用户姓(明文) - 从custName变量提取
        String firstName = variableMap.get("custName");
        if (StringUtils.hasText(firstName)) {
            reserveField1.put("firstName", firstName);
            processedFields.add("custName");
        }

        // userType: 机构运营场景(数字枚举)，必填，对应sceneCode
        String userType = StringUtils.hasText(batchData.getSceneCode()) ? batchData.getSceneCode() : "";
        reserveField1.put("userType", userType);
        
        // gender: 用户性别(数字枚举)，0女1男
        String gender = variableMap.get("gender");
        if (StringUtils.hasText(gender)) {
            reserveField1.put("gender", gender);
            processedFields.add("gender");
        }
        
        // overDays: 逾期天数
        String overDays = variableMap.get("overDays");
        reserveField1.put("overDays", StringUtils.hasText(overDays) ? overDays : "3");
        processedFields.add("overDays");
        
        // overAmt: 逾期金额
        String overAmt = variableMap.get("overAmt");
        if (StringUtils.hasText(overAmt)) {
            reserveField1.put("overAmt", overAmt);
            processedFields.add("overAmt");
        }
        
        // compName: 企业名称
        String compName = variableMap.get("compName");
        reserveField1.put("compName", StringUtils.hasText(compName) ? compName : "中原消费金融");
        processedFields.add("compName");

        // compTel: 客服电话
        String compTel = variableMap.get("compTel");
        reserveField1.put("compTel", StringUtils.hasText(compTel) ? compTel : "4001112233");
        processedFields.add("compTel");
        
        // batchNo: 批量编码（话术变量）
        String batchNo = StringUtils.hasText(batchData.getBatchNo()) ? batchData.getBatchNo() : "";
        reserveField1.put("batchNo", batchNo);
        processedFields.add("batchNo");
        
        // batchNumber: 数据集编号
        String yyyyMMdd = LocalDate.now().format(DateTimeFormatter.ofPattern(DateHelper.SHORT_DATE_FORMAT));
        String batchNumber = operateSixProcessor.getBatchNumber(yyyyMMdd, apiCode,
                userType, 1);

        reserveField1.put("batchNumber", batchNumber);
        reserveField1.put("zyxj", batchNumber);
        processedFields.add("batchNumber");

        // 将其他未处理的变量也放入reserveField1
        for (Map.Entry<String, String> entry : variableMap.entrySet()) {
            String code = entry.getKey();
            String value = entry.getValue();
            // 只处理未在reserveField1中设置的字段
            if (!processedFields.contains(code) && StringUtils.hasText(value)) {
                reserveField1.put(code, value);
            }
        }
        
        result.put("reserveField1", reserveField1);
        result.put("batchNumber", batchNumber);
        return result;
    }

    @Override
    public ZhongYuanBaseResponse<?> sceneVariable(String jsonData) {
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

}
