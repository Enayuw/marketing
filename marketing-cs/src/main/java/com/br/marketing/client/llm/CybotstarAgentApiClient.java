package com.br.marketing.client.llm;

import com.alibaba.fastjson.JSONObject;
import com.br.cloud.web.MethodType;
import com.br.cloud.web.PrometheusTimeMethod;
import com.br.marketing.client.HttpProxyClient;
import com.br.marketing.common.annoation.RetryMethod;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.utils.Constants;
import com.br.marketing.entity.llm.CybotstarAgentConfig;
import com.br.marketing.entity.llm.CybotstarAgentConfigExample;
import com.br.marketing.mapper.llm.CybotstarAgentConfigMapperBase;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.message.BasicHeader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * Cybotstar 智能体 API 客户端
 * 统一调用大模型接口入口
 */
@Slf4j
@Component
public class CybotstarAgentApiClient {

    private static final String LOG_PREFIX = "[Cybotstar] ";

    @Resource
    private CybotstarAgentConfigMapperBase cybotstarAgentConfigMapper;

    @Autowired
    private HttpProxyClient httpProxyClient;

    @Value("${api.cybotstar.dialogUrl:https://www.cybotstar.cn/openapi/v1/conversation/dialog/}")
    private String dialogUrl;

    @Value("${api.cybotstar.isProxy:true}")
    private boolean isProxy;

    /**
     * 智能体配置缓存
     * - 最大缓存100个
     * - 写入后60分钟过期（自动刷新）
     */
    private static LoadingCache<String, CybotstarAgentConfig> configCache = null;

    @PostConstruct
    private void init() {
        configCache = CacheBuilder.newBuilder()
                .maximumSize(100)                           // 最大缓存数量
                .expireAfterWrite(60, TimeUnit.MINUTES)     // 写入后60分钟过期
                .recordStats()                              // 开启统计
                .build(new CacheLoader<String, CybotstarAgentConfig>() {
                    @Override
                    public CybotstarAgentConfig load(String agentCode) throws Exception {
                        return loadFromDb(agentCode);
                    }
                });
    }

    /**
     * 从数据库加载配置
     */
    private CybotstarAgentConfig loadFromDb(String agentCode) {
        CybotstarAgentConfigExample example = new CybotstarAgentConfigExample();
        example.createCriteria()
                .andAgentCodeEqualTo(agentCode)
                .andIsDelEqualTo(Constants.DATA_VALID);  // 1-正常

        List<CybotstarAgentConfig> list = cybotstarAgentConfigMapper.selectByExample(example);

        if (!CollectionUtils.isEmpty(list)) {
            return list.get(0);
        }

        return null;
    }

    /**
     * 获取智能体配置
     */
    private CybotstarAgentConfig getConfig(String agentCode) {
        try {
            return configCache.get(agentCode);
        } catch (ExecutionException e) {
            log.error(LOG_PREFIX + "获取配置失败, agentCode: {}", agentCode, e);
            return null;
        } catch (CacheLoader.InvalidCacheLoadException e) {
            // load() 返回 null 时会抛此异常
            log.error(LOG_PREFIX + "配置不存在, agentCode: {}", agentCode, e);
            return null;
        }
    }

    /**
     * 清除所有缓存（静态方法，可通过类名直接调用）
     */
    public static void invalidateAll() {
        if (configCache != null) {
            log.warn(LOG_PREFIX + "清除所有配置缓存...");
            configCache.invalidateAll();
        }
    }

    /**
     * 清除指定配置缓存（静态方法，可通过类名直接调用）
     */
    public static void invalidate(String agentCode) {
        if (configCache != null) {
            log.warn(LOG_PREFIX + "清除配置缓存, agentCode: {}", agentCode);
            configCache.invalidate(agentCode);
        }
    }

    /**
     * 调用大模型接口
     *
     * @param agentCode 智能体编码
     * @param question  问题内容
     * @return Result<String> 响应结果
     */
    @RetryMethod(retryNowNum = 3)
    @PrometheusTimeMethod(buckets = {0.02d, 0.05d, 0.2d, 0.5d, 1d}, methodType = MethodType.REMOTE)
    public Result<String> dialog(String agentCode, String question) {
        //参数校验
        if (StringUtils.isBlank(agentCode)) {
            return new Result<String>().setCode(ResultCode.FAIL.getValue()).setMessage("agentCode 不能为空");
        }
        if (StringUtils.isBlank(question)) {
            return new Result<String>().setCode(ResultCode.FAIL.getValue()).setMessage("question 不能为空");
        }
        //获取配置（自动从缓存或数据库加载）
        CybotstarAgentConfig config = getConfig(agentCode);
        if (config == null) {
            log.error(LOG_PREFIX + "未找到智能体配置, agentCode: {}", agentCode);
            return new Result<String>().setCode(ResultCode.FAIL.getValue()).setMessage("未找到智能体配置: " + agentCode);
        }
        // 构建请求参数
        JSONObject jsonParam = new JSONObject();
        jsonParam.put("cybertron-robot-token", config.getRobotToken());
        jsonParam.put("username", config.getUsername());
        jsonParam.put("question", question);
        Header[] headers = new Header[]{
                new BasicHeader("Content-Type", "application/json"),
                new BasicHeader("cybertron-robot-key", config.getRobotKey())
        };
        //发送请求
        HashMap<String, String> response = httpProxyClient.sendByCodeWithLogWithHeader(
                jsonParam,
                dialogUrl,
                isProxy,
                MediaType.APPLICATION_JSON_UTF8_VALUE,
                jsonParam.toJSONString(),
                true,
                true,
                headers
        );
        String code = response.get("httpcode");
        if ("200".equals(code)) {
            JSONObject jsonResult = JSONObject.parseObject(response.get("content"));
            if ("000000".equals(jsonResult.getString("code"))) {
                JSONObject data = jsonResult.getJSONObject("data");
                return new Result<String>().setCode(ResultCode.SUCCESS.getValue()).setDate(data.getString("answer"));
            }else{
                return new Result<String>().setCode(ResultCode.FAIL.getValue());
            }
        } else {
            return new Result<String>().setCode(ResultCode.INTERNAL_SERVER_ERROR.getValue());
        }
    }
}
