package com.br.marketing.innerapi.controller;

import com.br.common.util.DateUtils;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.entity.MarketingTransferInfo;
import com.br.marketing.entity.MarketingTransferInfoExample;
import com.br.marketing.entity.MarketingTransferSyncUser;
import com.br.marketing.entity.MarketingTransferSyncUserExample;
import com.br.marketing.mapper.MarketingTransferInfoMapper;
import com.br.marketing.mapper.MarketingTransferSyncUserMapper;
import com.br.marketing.origin.MqFact;
import com.br.marketing.origin.TransferSource;
import com.br.marketing.rabbitmq.RabbitMqProducter;
import com.br.marketing.service.IProductResultSimpleService;
import com.br.marketing.service.Impl.ProductResultByConfigSimpleServiceImpl;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@RestController
@RequestMapping("redis")
@Slf4j
public class RedisController {

    @Autowired
    RedisChgService redisChgService;

    @Autowired
    IProductResultSimpleService productResultSimpleService;

    @GetMapping("get")
    public String get(@RequestParam("key") String key) {
        return redisChgService.get(key);
    }

    @GetMapping("del")
    public String del(@RequestParam("key") String key) {
        long del = redisChgService.del(key);
        return String.valueOf(del);
    }

    @GetMapping("set")
    public String set(@RequestParam("key") String key, @RequestParam("value") String value) {
        redisChgService.set(key, value);
        return "success";
    }

    @GetMapping("/clearInnerCache")
    public String clearInnerCache(@RequestParam("type") Integer type){
        if(Integer.valueOf(1).equals(type)){
            ProductResultByConfigSimpleServiceImpl.flagScoreByinnerList.clear();
        }
        return "success";
    }

    @Autowired
    MarketingCommonConfig marketingCommonConfig;

    /**
     * 获取speed的配置信息 方便验证speed是否推送成功
     *
     * @return
     */
    @GetMapping("/getSpeedInfo")
    public String getSpeedInfo() {
        return marketingCommonConfig.toString();
    }


    @Resource
    private RabbitMqProducter producter;
    @Resource
    private MarketingTransferSyncUserMapper marketingTransferSyncUserMapper;
    @Resource
    private MarketingTransferInfoMapper marketingTransferInfoMapper;
    private static volatile ThreadPoolExecutor pool;
    private static volatile boolean IS_RUN;

    /**
     * 临时处理D20220610数禾重申场景黑名单过滤（营销→客服）-3710051的需求
     * http://c.100credit.cn/pages/viewpage.action?pageId=74820557
     * 首次上线捞回近30天内的数据 触发
     */
    @SneakyThrows
    @GetMapping("/shuHeSendMq")
    public String shuHeSendMq(
            @RequestParam(name = "sTime", required = false) String s
            , @RequestParam(name = "eTime", required = false) String e
            , @RequestParam(name = "days", required = false) Long days
            , @RequestParam(name = "code", required = false) String code
            , @RequestParam(name = "cid", required = false) String cid
            , @RequestParam(name = "type", required = false) String type) {
        String p = "yyyy-MM-dd HH:mm:ss";
        String key = "marketing:inner:api:shuhe:sendmq";
        MarketingTransferSyncUserExample example = new MarketingTransferSyncUserExample();
        Date startDateTime = StringUtils.isBlank(s)
                ? Date.from(LocalDateTime.now().minusDays(ObjectUtils.isEmpty(days) ? 31 : days)
                .toLocalDate().atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()) : DateUtils.parse(s, p);
        Date endDateTime = StringUtils.isBlank(e)
                ? Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant()) : DateUtils.parse(e, p);
        String apiCode = StringUtils.isNotBlank(code) ? code : "3710051";
        String userType = StringUtils.isBlank(s) ? "重申" : type;
        example.createCriteria().andApiCodeEqualTo(apiCode)
                .andUserTypeEqualTo(userType)
                .andCreateTimeBetween(startDateTime, endDateTime);
        example.settCid(StringUtils.isNotBlank(cid) ? cid : "337");
        String startDateTimeStr = LocalDateTime.ofInstant(startDateTime.toInstant(), ZoneId.systemDefault()).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        String endDateTimeStr = LocalDateTime.ofInstant(endDateTime.toInstant(), ZoneId.systemDefault()).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        log.warn("1.1、统计总量检索条件:\nandApiCodeEqualTo={}\nandUserTypeEqualTo={}\nandCreateTimeBetween={},{}"
                , apiCode, userType, startDateTimeStr, endDateTimeStr);
        int count = marketingTransferSyncUserMapper.countByExample(example);
        log.warn("1.2、共有数据量:{}", count);
        int pageSize = 1000;
        int pageSum = count / pageSize + ((count % pageSize) > 0 ? 1 : 0);
        log.warn("1.3、每页:{},共{}页;接下来进入一分钟的冷静期，在这期间可停止本次任务", pageSize, pageSum);
        AtomicInteger page = new AtomicInteger(1);
        IS_RUN = true;
        redisChgService.del(key);
        pool = BrExecutors.getThreadPool(1, 1);
        pool.execute(() -> {
            long l = System.currentTimeMillis();
            MqFact mqFact = new MqFact();
            try {
                for (int i = 60; i > 0; i--) {
                    log.warn(i + "");
                    TimeUnit.SECONDS.sleep(1);
                }
                do {
                    example.setOrderByClause("create_time limit " + ((page.getAndIncrement() - 1) * pageSize) + "," + pageSize);
                    List<MarketingTransferSyncUser> list = marketingTransferSyncUserMapper.selectByExample(example);
                    log.warn("2.1、第【{}】页，数据量:{},检索条件:\nandApiCodeEqualTo={}\nandUserTypeEqualTo={}\n" +
                                    "andCreateTimeBetween={},{}\nsetOrderByClause={}"
                            , page.get(), list.size(), apiCode, userType, startDateTimeStr, endDateTimeStr, "create_time");
                    List<String> collect = list.parallelStream().map(MarketingTransferSyncUser::getRequestId).collect(Collectors.toList());
                    MarketingTransferInfoExample example1 = new MarketingTransferInfoExample();
                    example1.createCriteria().andRequestIdIn(collect).andApiCodeEqualTo(apiCode);
                    example1.setOrderByClause("create_time");
                    List<MarketingTransferInfo> list1 = marketingTransferInfoMapper.selectByExample(example1);
                    log.warn("2.2、检索到info表对应数据量:{}", list1.size());
                    for (MarketingTransferInfo transferInfo : list1) {
                        if (Thread.interrupted() || !IS_RUN || redisChgService.exists(key)) {
                            log.warn("#2.停止操作后的数据信息：\nid:{}\nRequestId:{}\ncreateTime:{}", transferInfo.getId()
                                    , transferInfo.getRequestId(), LocalDateTime.ofInstant(transferInfo.getCreateTime().toInstant(), ZoneId.systemDefault()).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                            return;
                        }
                        mqFact.setSourceId(transferInfo.getId());
                        mqFact.setSource(TransferSource.UNIVERSAL_TRANSFER_PROCESS.getCode());
                        producter.sendToUniversalTransferQueue(mqFact);
                    }
                    log.warn("#1.每页最后一条记录的信息：\nRequestId:{}\ncreateTime:{}"
                            , list.get(list.size() - 1).getRequestId(), LocalDateTime.ofInstant(list.get(list.size() - 1).getCreateTime().toInstant(), ZoneId.systemDefault()).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                } while (!Thread.interrupted() && page.get() <= pageSum && IS_RUN && !redisChgService.exists(key));
            } catch (Exception exception) {
                log.error(exception.getMessage(), exception);
                Thread.currentThread().interrupt();
            }
            log.warn("推送耗时：{}ms", System.currentTimeMillis() - l);
        });
        return count + "";
    }

    /**
     * 2022/6/16 10:44
     * 停止
     */
    @GetMapping("/shuHeSendMqStop")
    public String stop() {
        IS_RUN = false;
        redisChgService.setex("marketing:inner:api:shuhe:sendmq", System.currentTimeMillis() + "", 60);
        if (pool == null) {
            return "线程变量已没有对象引用，值为null";
        }
        pool.shutdownNow();
        if (pool.isShutdown()) {
            pool = null;
            return "true";
        }
        pool = null;
        return "false";
    }
}
