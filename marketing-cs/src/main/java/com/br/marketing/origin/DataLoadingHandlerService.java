package com.br.marketing.origin;

import com.br.marketing.client.RedisChgService;
import com.br.marketing.service.Impl.TableCreateServiceImpl;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

@Service
@Slf4j
public class DataLoadingHandlerService {

    private static final String cidKey = "marketing:innerapi:transfer:cid:";

    @Resource
    private TableCreateServiceImpl tableCreateService;

    @Resource
    RedisChgService redisChgService;

    @Resource
    private MarketingCommonConfig marketingCommonConfig;

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
     */
    public Integer getShuHePeriodOfValidityDay(String userType) throws IllegalAccessException {
        Assert.notNull(userType, "场景不可为null");
        Map<String, String> shuHePeriodOfValidityDayMap = marketingCommonConfig.getShuHePeriodOfValidityDayMap();
        if (shuHePeriodOfValidityDayMap == null) {
            shuHePeriodOfValidityDayMap = new HashMap<>(4);
            shuHePeriodOfValidityDayMap.put("促首登", "T");
            shuHePeriodOfValidityDayMap.put("促申完", "T+15");
            shuHePeriodOfValidityDayMap.put("促首借", "T+31");
        }
        if (shuHePeriodOfValidityDayMap.containsKey(userType)) {
            Matcher matcher = PATTERN.matcher(shuHePeriodOfValidityDayMap.get(userType));
            if (matcher.find()) {
                String day = matcher.group();
                return new BigDecimal(day).setScale(0, BigDecimal.ROUND_HALF_UP).intValue() - 1;
            } else {
                return null;
            }
        }
        throw new IllegalAccessException("未知的场景类:" + userType);
    }
}
