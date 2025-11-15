package com.br.marketing.datarelayservice.service.impl;

import com.alibaba.fastjson.JSON;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.datarelayservice.service.ZhongYuanUploadDataService;
import com.br.marketing.dto.zhongyuan.*;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
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
    private MarketingCommonConfig marketingCommonConfig;
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
                return ZhongYuanBaseResponse.fail("1000001", "参数错误");
            }

            LoginRequest loginData = baseRequest.getData();

            // 2. 参数校验
            if (!StringUtils.hasText(loginData.getAppUser()) || !StringUtils.hasText(loginData.getAppKey())) {
                return ZhongYuanBaseResponse.fail("1000001", "参数错误：appUser或appKey为空");
            }

            // 3. 验证appUser和appKey
            if (!validateCredentials(loginData.getAppUser(), loginData.getAppKey())) {
                log.warn("登录失败，用户名或密码错误，appUser: {}", loginData.getAppUser());
                return ZhongYuanBaseResponse.fail("1000001", "用户名或密码错误");
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
            return ZhongYuanBaseResponse.fail("1000006", "系统异常：" + e.getMessage());
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
                return ZhongYuanBaseResponse.fail("1000001", "参数错误");
            }

            // 2. Token验证
            ZhongYuanBaseResponse<?> tokenResponse = validateTokenFromRequest(baseRequest, request);
            if (!"0000000".equals(tokenResponse.getCode())) {
                return tokenResponse;
            }

            BatchTaskRequest batchData = baseRequest.getData();

            // 3. 参数校验
            if (!StringUtils.hasText(batchData.getBatchNo()) || batchData.getTaskDataList() == null
                    || batchData.getTaskDataList().isEmpty()) {
                return ZhongYuanBaseResponse.fail("1000001", "参数错误：批次编号或任务数据列表为空");
            }

            // 4. 生成batchUid和taskUid
            String batchUid = generateBatchUid();
            List<BatchTaskResponse.TaskInfo> taskInfoList = new ArrayList<>();
            for (BatchTaskRequest.TaskData taskData : batchData.getTaskDataList()) {
                BatchTaskResponse.TaskInfo taskInfo = new BatchTaskResponse.TaskInfo();
                taskInfo.setTaskUid(generateTaskUid());
                taskInfo.setTaskNo(taskData.getTaskNo());
                taskInfo.setTelNo(taskData.getTelNo());
                taskInfoList.add(taskInfo);
            }

            // 5. 构建响应
            BatchTaskResponse batchTaskResponse = new BatchTaskResponse();
            batchTaskResponse.setBatchNo(batchData.getBatchNo());
            batchTaskResponse.setBatchUid(batchUid);
            batchTaskResponse.setTaskInfoList(taskInfoList);

            ZhongYuanBaseResponse<BatchTaskResponse> response = ZhongYuanBaseResponse.success(batchTaskResponse);

            log.warn("中原消金批量任务上报成功，batchNo: {}, batchUid: {}, taskCount: {}",
                    batchData.getBatchNo(), batchUid, taskInfoList.size());

            // TODO: 保存原始数据到b_marketing_zhongyuan_upload表
            // TODO: 数据清洗和推送逻辑

            return response;

        } catch (Exception e) {
            log.error("中原消金批量任务上报接口异常", e);
            return ZhongYuanBaseResponse.fail("1000006", "系统异常：" + e.getMessage());
        }
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
                return ZhongYuanBaseResponse.fail("1000001", "参数错误");
            }

            // 2. Token验证
            ZhongYuanBaseResponse<?> tokenResponse = validateTokenFromRequest(baseRequest, request);
            if (!"0000000".equals(tokenResponse.getCode())) {
                return tokenResponse;
            }

            SceneVariableRequest sceneData = baseRequest.getData();

            // 3. 参数校验
            if (!StringUtils.hasText(sceneData.getSceneCode())) {
                return ZhongYuanBaseResponse.fail("1000001", "参数错误：场景代码为空");
            }

            // 4. 查询场景变量
            List<SceneVariableResponse> sceneVariableList = getSceneVariables(sceneData.getSceneCode());

            // 5. 构建响应
            ZhongYuanBaseResponse<List<SceneVariableResponse>> response = ZhongYuanBaseResponse.success(sceneVariableList);

            log.warn("中原消金场景变量查询成功，sceneCode: {}, variableCount: {}",
                    sceneData.getSceneCode(), sceneVariableList.size());

            return response;

        } catch (Exception e) {
            log.error("中原消金场景变量查询接口异常", e);
            return ZhongYuanBaseResponse.fail("1000006", "系统异常：" + e.getMessage());
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
     * @param request     HTTP请求对象
     * @return 验证结果响应，成功返回success响应，失败返回fail响应
     */
    private ZhongYuanBaseResponse<?> validateTokenFromRequest(ZhongYuanBaseRequest<?> baseRequest, HttpServletRequest request) {
        // 1. 从baseRequest获取token
        String token = baseRequest != null ? baseRequest.getToken() : null;
        
        // 2. Token为空检查
        if (!StringUtils.hasText(token)) {
            return ZhongYuanBaseResponse.fail("1000002", "Token无效");
        }
        
        // 3. 验证Token
        try {
            validateToken(token);
            // 验证成功，返回success响应
            return ZhongYuanBaseResponse.success(null);
        } catch (RuntimeException e) {
            String errorMsg = e.getMessage();
            if (errorMsg != null && errorMsg.contains("1000002")) {
                return ZhongYuanBaseResponse.fail("1000002", errorMsg.substring(errorMsg.indexOf(":") + 1));
            }
            return ZhongYuanBaseResponse.fail("1000002", "Token无效");
        }
    }

    /**
     * 获取场景变量列表
     *
     * @param sceneCode 场景代码
     * @return 场景变量列表
     */
    private List<SceneVariableResponse> getSceneVariables(String sceneCode) {
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
     * 生成批次唯一标识
     *
     * @return batchUid
     */
    private String generateBatchUid() {
        // 使用Base64编码生成唯一标识
        String raw = UUID.randomUUID().toString() + System.currentTimeMillis();
        return Base64.getEncoder().encodeToString(raw.getBytes()).substring(0, 32);
    }

    /**
     * 生成任务唯一标识
     *
     * @return taskUid
     */
    private String generateTaskUid() {
        // 使用Base64编码生成唯一标识
        String raw = UUID.randomUUID().toString() + System.currentTimeMillis();
        return Base64.getEncoder().encodeToString(raw.getBytes()).substring(0, 32);
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
            throw new RuntimeException("1000002:Token无效");
        }

        // 2. Token格式校验
        if (!isValidTokenFormat(token)) {
            throw new RuntimeException("1000002:Token格式错误");
        }

        // 3. 从配置获取appUser，然后查询Redis验证token
        Map<String, String> zhongYuanIdentity = marketingCommonConfig.getZhongYuanIdentity();
        String appUser = zhongYuanIdentity.get("appUser");
        
        if (!StringUtils.hasText(appUser)) {
            throw new RuntimeException("1000002:系统配置错误");
        }

        // 4. 从Redis获取存储的token
        String userTokenKey = TOKEN_PREFIX + appUser;
        String storedToken = redisChgService.get(userTokenKey);

        if (!StringUtils.hasText(storedToken)) {
            throw new RuntimeException("1000002:Token无效或已过期");
        }

        // 5. 验证token是否匹配
        if (!token.equals(storedToken)) {
            throw new RuntimeException("1000002:Token无效");
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
