package com.br.marketing.datarelayservice.service.impl;

import com.alibaba.fastjson.JSON;
import com.br.marketing.datarelayservice.service.ZhongYuanUploadDataService;
import com.br.marketing.dto.zhongyuan.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

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
    private RedisTemplate<String, String> redisTemplate;

    private static final String TOKEN_PREFIX = "znwh:token:";
    private static final long TOKEN_EXPIRE_TIME = 7200; // 2小时


    /**
     * appUser配置（从配置文件读取）
     */
    @Value("${zhongyuan.appUser:zyxfjr_coll}")
    private String configAppUser;

    /**
     * appKey配置（从配置文件读取）
     */
    @Value("${zhongyuan.appKey:87C5FCB80F872B8D67BA3306BB09157C}")
    private String configAppKey;

    private static final String SCENE_VARIABLE_CACHE_PREFIX = "znwh:scene:variable:";
    private static final long SCENE_VARIABLE_CACHE_TIME = 3600; // 1小时

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

            // 4. 生成Token
            String token = generateToken(loginData.getAppUser());

            // 5. 存储Token到Redis
            saveToken(token);

            // 6. 构建响应
            LoginResponse loginResponse = new LoginResponse();
            loginResponse.setToken(token);

            ZhongYuanBaseResponse<LoginResponse> response = ZhongYuanBaseResponse.success(loginResponse);

            log.warn("中原消金登录成功，appUser: {}, token: {}", loginData.getAppUser(), token);
            return response;

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
            String token = baseRequest.getToken();
            if (!StringUtils.hasText(token)) {
                // 尝试从请求参数获取
                token = request.getParameter("token");
            }
            if (!StringUtils.hasText(token)) {
                return ZhongYuanBaseResponse.fail("1000002", "Token无效");
            }

            try {
                validateToken(token);
            } catch (RuntimeException e) {
                String errorMsg = e.getMessage();
                if (errorMsg.contains("1000002")) {
                    return ZhongYuanBaseResponse.fail("1000002", errorMsg.substring(errorMsg.indexOf(":") + 1));
                } else if (errorMsg.contains("1000003")) {
                    return ZhongYuanBaseResponse.fail("1000003", errorMsg.substring(errorMsg.indexOf(":") + 1));
                }
                return ZhongYuanBaseResponse.fail("1000002", "Token无效");
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
            String token = baseRequest.getToken();
            if (!StringUtils.hasText(token)) {
                token = request.getParameter("token");
            }
            if (!StringUtils.hasText(token)) {
                return ZhongYuanBaseResponse.fail("1000002", "Token无效");
            }

            try {
                validateToken(token);
            } catch (RuntimeException e) {
                String errorMsg = e.getMessage();
                if (errorMsg.contains("1000002")) {
                    return ZhongYuanBaseResponse.fail("1000002", errorMsg.substring(errorMsg.indexOf(":") + 1));
                } else if (errorMsg.contains("1000003")) {
                    return ZhongYuanBaseResponse.fail("1000003", errorMsg.substring(errorMsg.indexOf(":") + 1));
                }
                return ZhongYuanBaseResponse.fail("1000002", "Token无效");
            }

            SceneVariableRequest sceneData = baseRequest.getData();

            // 3. 参数校验
            if (!StringUtils.hasText(sceneData.getSceneCode())) {
                return ZhongYuanBaseResponse.fail("1000001", "参数错误：场景代码为空");
            }

            // 4. 查询场景变量（先查缓存，再查数据库）
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
        return configAppUser.equals(appUser) && configAppKey.equals(appKey);
    }

    /**
     * 获取场景变量列表
     *
     * @param sceneCode 场景代码
     * @return 场景变量列表
     */
    private List<SceneVariableResponse> getSceneVariables(String sceneCode) {
        // 1. 先查缓存
        String cacheKey = SCENE_VARIABLE_CACHE_PREFIX + sceneCode;
        String cachedData = redisTemplate.opsForValue().get(cacheKey);

        if (StringUtils.hasText(cachedData)) {
            log.debug("从缓存获取场景变量，sceneCode: {}", sceneCode);
            return JSON.parseArray(cachedData, SceneVariableResponse.class);
        }

        // 2. 查询数据库（这里使用固定数据，实际应该从数据库查询）
        List<SceneVariableResponse> variableList = getDefaultSceneVariables(sceneCode);

        // 3. 缓存结果
        if (variableList != null && !variableList.isEmpty()) {
            redisTemplate.opsForValue().set(cacheKey, JSON.toJSONString(variableList),
                    SCENE_VARIABLE_CACHE_TIME, TimeUnit.SECONDS);
        }

        return variableList;
    }

    /**
     * 获取默认场景变量（SC_IVR场景）
     *
     * @param sceneCode 场景代码
     * @return 场景变量列表
     */
    private List<SceneVariableResponse> getDefaultSceneVariables(String sceneCode) {
        List<SceneVariableResponse> variableList = new ArrayList<>();

        if ("SC_IVR".equals(sceneCode)) {
            // SC_IVR场景的变量配置
            variableList.add(createSceneVariable("custName", "客户姓名", null));
            variableList.add(createSceneVariable("gender", "客户性别", null));
            variableList.add(createSceneVariable("overDays", "逾期天数", null));
            variableList.add(createSceneVariable("overAmt", "逾期金额", null));
            variableList.add(createSceneVariable("compName", "企业名称", "中原消费金融"));
            variableList.add(createSceneVariable("compTel", "客服电话", "4001112233"));
        }

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
     * 存储Token到Redis
     *
     * @param token Token值
     */
    public void saveToken(String token) {
        String tokenKey = TOKEN_PREFIX + token;
        redisTemplate.opsForValue().set(tokenKey, token, TOKEN_EXPIRE_TIME, TimeUnit.SECONDS);
        log.warn("Token存储成功，token: {}", token);
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

        // 3. 从Redis获取Token
        String tokenKey = TOKEN_PREFIX + token;
        String storedToken = redisTemplate.opsForValue().get(tokenKey);

        if (!StringUtils.hasText(storedToken)) {
            throw new RuntimeException("1000002:Token无效或已过期");
        }

        // 4. Token匹配校验
        if (!token.equals(storedToken)) {
            throw new RuntimeException("1000002:Token无效");
        }

        // 5. 更新过期时间（续期）
        redisTemplate.expire(tokenKey, TOKEN_EXPIRE_TIME, TimeUnit.SECONDS);
        log.debug("Token校验成功，token: {}", token);
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
