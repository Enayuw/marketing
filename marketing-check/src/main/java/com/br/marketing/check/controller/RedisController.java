package com.br.marketing.check.controller;

import com.alibaba.fastjson.JSON;
import com.br.common.util.DateUtils;
import com.br.marketing.check.CkeckApplication;
import com.br.marketing.service.IFileToMarketingRuleService;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.client.haier.HaierServiceClient;
import com.br.marketing.client.haier.output.PushDTO;
import com.br.marketing.client.haier.output.Response2Entity;
import com.br.marketing.client.haier.output.ResponseInfoEntity;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.constants.rediskey.RedisKeyConstant;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.dangdang.ddframe.reg.base.CoordinatorRegistryCenter;
import com.dangdang.ddframe.reg.zookeeper.ZookeeperConfiguration;
import com.dangdang.ddframe.reg.zookeeper.ZookeeperRegistryCenter;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.recipes.cache.TreeCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.function.Function;

/**
 * Created by Bairong on 2020/6/16.
 */
@RestController
@RequestMapping("/redis/")
@Slf4j
public class RedisController {

    @Resource
    RedisChgService redisChgService;

    @Resource
    private HaierServiceClient haierServiceClient;

    @GetMapping("get")
    public String get(String key) {
        log.info("key ----{}----", key);
        return redisChgService.get(key);
    }

    @Value("${SERVER_LISTS}")
    private String zklist;

    @Value("${NAMESPACE}")
    private String namespace;

    @GetMapping("get2")
    public String get2(String key) {
        ZookeeperConfiguration zkConfig = new ZookeeperConfiguration(zklist, namespace);
        CoordinatorRegistryCenter result = new ZookeeperRegistryCenter(zkConfig);
        result.init();
        TreeCache treeCache = (TreeCache) result.getRawCache("/");
        if (treeCache == null) {
            result.addCacheData("/");
        }

        result.persist("/TaskTransferSyncReportJob/leader/sharding/necessary","");
//        result.persist(new JobNodePath("taskTransferSyncReportJob").getExecutionNodePath(),"");
//        new CoordinatorRegistryCenter().getChildrenKeys()
//        ContainerContext.applicationContext.getBeansOfType(AbstractElasticJob.class).get("taskTransferSyncReportJob").getJobFacade()
        return "test";
    }



    @GetMapping("set")
    public String set(String key, String value) {
        log.info("key ----{}----{}", key, value);
        redisChgService.set(key, value);
        return "success";
    }

    @GetMapping("del")
    public String del(String key, String value) {
        log.info("key ----{}----{}", key, value);
        redisChgService.del(key);
        return "success";
    }

    @GetMapping("delBigHash")
    public String delBigHash(String key){
        redisChgService.delBigHash(key,3000);
        return "success";
    }

    private volatile int count = 0;

    @GetMapping("test")
    public Result<Response2Entity> testClient() {
        List<Map<String, String>> list = new ArrayList<>();
        SecureRandom random = new SecureRandom();
        try {
            Function<List<Map<String, String>>, Set<PushDTO.DataItems>> function = maps -> {
                Set<PushDTO.DataItems> dataItemsSet = new HashSet<>();
                final String format = DateUtils.format(Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant()), DateUtils.yyyyMMddHHmmss);
                dataItemsSet.add(new PushDTO.DataItems("别害怕", format.concat("-我是测试-" + count)));
                dataItemsSet.add(new PushDTO.DataItems("don'tBeAfraid", format.concat("-I'mTesting-" + count)));
                return dataItemsSet;
            };
            final PushDTO.FormData formData = new PushDTO.FormData(UUID.randomUUID().toString().concat("-").concat(String.valueOf(count)), String.valueOf(random.nextInt(3) + 1), list, function);
            final Result<Response2Entity> result = haierServiceClient.pushToTeleSales(formData);
            final Result<ResponseInfoEntity> result1 = haierServiceClient.resultQueryPushToTeleSales(formData.getRequestId());
            log.warn("请求++++：" + result.getData().toString());
            log.warn("查询----：" + result1.getData().toString());
            return result;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return new Result<>().setMessage(e.getMessage());
        }
    }

    @Autowired
    MarketingCommonConfig marketingCommonConfig;

    /**
     * 获取speed的配置信息 方便验证speed是否推送成功
     * @return
     */
    @GetMapping("/getSpeedInfo")
    public String getSpeedInfo(){
        return marketingCommonConfig.toString();
    }

    @GetMapping("/getRuleFile")
    public String getRuleFile(){
        Map<String, IFileToMarketingRuleService> beansOfType = CkeckApplication.ac.getBeansOfType(IFileToMarketingRuleService.class);
        return "123";
    }

    @GetMapping("/getScoreToCustomerBigKey")
    public String getScoreToCustomerBigKey(Long fileId){
        HashMap<String, Boolean> res = new HashMap<>();
        for (int i = 0; i < 4; i++) {
            String key = RedisKeyConstant.SCORE_TO_CUSTOMER_SORT_KEY
                    .concat(":").concat(fileId.toString())
                    .concat(":").concat("" + i);
            Boolean exists = redisChgService.exists(key);
            res.put(key,exists);
        }
        return JSON.toJSONString(res);
    }
}
