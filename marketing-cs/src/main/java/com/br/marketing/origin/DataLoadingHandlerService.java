package com.br.marketing.origin;

import com.br.marketing.client.RedisChgService;
import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.mapper.CustomerRuleMapper;
import com.br.marketing.rule.AssembleData;
import com.br.marketing.rule.common.CommonRuleLabelEnum;
import com.br.marketing.service.Impl.TableCreateServiceImpl;
import com.br.marketing.service.customertagsprocess.CustomerTagsProcessServiceImpl;
import com.br.marketing.service.customertagsprocess.vo.CustomerTagsVO;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.strategy.InterfaceHandlerService;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * code is far away from bug with the animal protecting
 * ┏┓　　　┏┓
 * ┏┛┻━━━┛┻┓
 * ┃　　　　　　　┃
 * ┃　　　━　　　┃
 * ┃　┳┛　┗┳　┃
 * ┃　　　　　　　┃
 * ┃　　　┻　　　┃
 * ┃　　　　　　　┃
 * ┗━┓　　　┏━┛
 * 　　┃　　　┃神兽保佑
 * 　　┃　　　┃代码无BUG！
 * 　　┃　　　┗━━━┓
 * 　　┃　　　　　　　┣┓
 * 　　┃　　　　　　　┏┛
 * 　　┗┓┓┏━┳┓┏┛
 * 　　　┃┫┫　┃┫┫
 * 　　　┗┻┛　┗┻┛
 *
 * @Description : 特殊数据加载处理类
 * ---------------------------------
 * @Author : jilong.xu
 * @Date : Create in 2022/3/18 10:36
 */

@Component
@Slf4j
public class DataLoadingHandlerService {

    private static final String cidKey = "marketing:innerapi:transfer:cid:";

    @Resource
    private TableCreateServiceImpl tableCreateService;

    @Resource
    RedisChgService redisChgService;

    @Resource
    private CustomerRuleMapper customerRuleMapper;

    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Autowired
    private InterfaceHandlerService interfaceHandlerService;

    @Resource
    CustomerTagsProcessServiceImpl customerTagsProcessService;

    public String getTcIdFromRedis(String apiCode) {
        // 1 获取分表后缀
        String key = cidKey.concat(apiCode);
        String tcId;
        try {
            tcId = redisChgService.get(key);
            if (StringUtils.isEmpty(tcId)) {
                tcId = tableCreateService.getTcId(apiCode);
                // 缓存一周
                redisChgService.setex(key, tcId, 7 * 24 * 3600);
            }
        } catch (Exception e) {
            tcId = tableCreateService.getTcId(apiCode);
            log.error("根据客户apiCode -- {} 查询tcId失败 --", apiCode, e);
        }
        return tcId;
    }

    private final static Pattern PATTERN = Pattern.compile("[-+]?\\d+(\\.\\d+)?");



    /**
     * 2022/3/22 16:22
     * 数禾获取场景有效期，有效期包含当天
     *
     * @param userType 场景
     * @return null时为当前月底
     * @deprecated 已弃用，判断规则使用{@link com.br.marketing.service.TransferDataValidityPeriodService}
     */
    @Deprecated
    public Integer getShuHePeriodOfValidityDay(String userType) throws IllegalAccessException {
        Assert.notNull(userType, "场景不可为null");
        Map<String, String> shuHePeriodOfValidityDayMap = marketingCommonConfig.getShuHePeriodOfValidityDayMap();
        if (shuHePeriodOfValidityDayMap == null) {
            shuHePeriodOfValidityDayMap = new HashMap<>(5);
            shuHePeriodOfValidityDayMap.put("促首登", "T");
            shuHePeriodOfValidityDayMap.put("促申完", "T+15");
            shuHePeriodOfValidityDayMap.put("促首借", "T+31");
            shuHePeriodOfValidityDayMap.put("促复借", "T");
            shuHePeriodOfValidityDayMap.put("重申", "T");
        }
        try {
            return getPeriodOfValidityDay(shuHePeriodOfValidityDayMap, userType);
        } catch (IllegalAccessException e) {
            throw new IllegalAccessException("未知的场景类:" + userType);
        }
    }

    /**
     * 2022/3/22 16:22
     * 获取有效期，有效期包含当天
     *
     * @param periodOfValidityDayMap 有效期配置 eg:{"test":"T+30"}
     * @param key                    配置有效期key eg:test
     * @return null时为当前月底
     */
    public Integer getPeriodOfValidityDay(Map<String, String> periodOfValidityDayMap, String key)
            throws IllegalAccessException {
        if (periodOfValidityDayMap.containsKey(key)) {
            return periodOfValidityDay(periodOfValidityDayMap.get(key));
        }
        throw new IllegalAccessException("有效期配置，未知的配置有效期key:" + key);
    }

    /**
     * 2022/3/22 16:22
     * 获取有效期，有效期包含当天，其中“T+n” n表示共多少天
     * 计算时需要将配置的时间减去一天
     * eg：T+30;当天日期时2022/3/1,有效期范围为：2022/3/1至2022/3/29,共30天
     *
     * @param periodOfValidity 有效期配置,T 代表当前天到月底; T+/-day 代表当前天到day-1天; day为0时为当天，共day天，eg：T+30
     * @return null时为当前月底
     */
    public Integer periodOfValidityDay(String periodOfValidity) {
        Matcher matcher = PATTERN.matcher(periodOfValidity);
        if (matcher.find()) {
            String dayStr = matcher.group();
            int day = new BigDecimal(dayStr).setScale(0, BigDecimal.ROUND_HALF_UP).intValue();
            return day > 0 ? (day - 1) : day == 0 ? day : (day + 1);
        } else {
            return null;
        }
    }


    /**
     * 客户规则缓存
     */
    private static LoadingCache<String, Set<String>> ruleCache = null;


    @PostConstruct
    private void init() {
        ruleCache = CacheBuilder.newBuilder()
                .maximumSize(100)
                .expireAfterWrite(60, TimeUnit.MINUTES)
                .recordStats()
                .build(new CacheLoader<String, Set<String>>() {
                    @Override
                    public Set<String> load(String key) {
                        return customerRuleMapper.customerRuleLabels(key);
                    }
                });
    }

    /**
     * 获取客户规则
     */
    public static void invalidateAll() {
        if (ruleCache != null) {
            log.warn("客户规则清理...");
            ruleCache.invalidateAll();
        }
    }

    /**
     * @param apiCode
     * @return
     */
    public Set<String> customerRules(String apiCode) {
        try {
            return ruleCache.get(apiCode);
        } catch (ExecutionException e) {
            log.error("获取客户规则失败", e);
        }
        return new HashSet<>();
    }


    /**
     * 2022/8/29 16:22
     * 同程金融获取有效期，有效期包含当天
     *
     * @return null时为当前月底
     */
    public Integer getTongChengPeriodOfValidityDay() {
        String tongChengPeriodOfValidityDay = marketingCommonConfig.getTongChengPeriodOfValidityDay();
        if (tongChengPeriodOfValidityDay == null) {
            tongChengPeriodOfValidityDay = "T+30";
        }
        Matcher matcher = PATTERN.matcher(tongChengPeriodOfValidityDay);
        if (matcher.find()) {
            String day = matcher.group();
            return new BigDecimal(day).setScale(0, BigDecimal.ROUND_HALF_UP).intValue();
        } else {
            return null;
        }
    }

    public void commonRuleContextAction(Integer source, List<AssembleData> assembleDataList, ProcessHandlerContext context) {
        Set<String> enumCodes = Arrays.stream(CommonRuleLabelEnum.values())
                .map(CommonRuleLabelEnum::getCode)
                .collect(Collectors.toSet());

        if(TransferSource.INIT_DATA_SET_PROCESS.getCode().equals(source)){
            if (assembleDataList.stream().anyMatch(t-> enumCodes.contains(t.label()))) {
                CustomerTagsVO tags = customerTagsProcessService.getTags(context.getApiCode());
                context.setCustomerTagsVO(tags);
            }
        }
    }

}
