package com.br.marketing.dto.shuhe.factory;

import com.br.marketing.dto.shuhe.strategy.*;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 场景策略工厂
 *
 * @author Guo Zeqiang
 * @dateTime 2022/2/11 10:13
 */
@Component
public class UserTypeStrategyFactory {

    private static MarketingCommonConfig marketingCommonConfig;

    private final static Map<String, BaseUserType> USER_TYPE_CACHE = new ConcurrentHashMap<>();

    static {
        USER_TYPE_CACHE.put("促首登", new CuShouDeng());
        USER_TYPE_CACHE.put("促申完", new CuShenWan());
        USER_TYPE_CACHE.put("促首借", new CuShouJie());
        USER_TYPE_CACHE.put("促复借", new CuFuJie());
        USER_TYPE_CACHE.put("重申", new ChongShen());
    }

    @Resource
    public void setMarketingCommonConfig(MarketingCommonConfig marketingCommonConfig) {
        UserTypeStrategyFactory.marketingCommonConfig = marketingCommonConfig;
    }

    public Set<String> getUserTypes() {
        return USER_TYPE_CACHE.keySet();
    }

    public static BaseUserType getUserTypeStrategy(String userType) {
        if (userType == null) {
            return new DefaultUserType();
        }
        BaseUserType baseUserType = USER_TYPE_CACHE.getOrDefault(userType, new DefaultUserType()).setUserType(userType);
        try {
            Map<String, List<String>> mappingMap = marketingCommonConfig.getShuHeUserTypeAndApiCodeMappingMap();
            List<String> apiCodeList = mappingMap.get(userType);
            if (apiCodeList == null) {
                return baseUserType;
            }
            List<String> apiCodes = baseUserType.getApiCodes();
            if (apiCodeList.size() == 0) {
                apiCodes.clear();
            } else {
                for (String apiCode : apiCodeList) {
                    if (apiCodes.contains(apiCode)) {
                        continue;
                    }
                    apiCodes.add(apiCode);
                }
            }
        } catch (Exception ignored) {
        }
        return baseUserType;
    }

}
