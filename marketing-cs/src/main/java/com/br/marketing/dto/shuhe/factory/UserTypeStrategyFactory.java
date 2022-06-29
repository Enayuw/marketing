package com.br.marketing.dto.shuhe.factory;

import com.br.marketing.dto.shuhe.strategy.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 场景策略工厂
 *
 * @author Guo Zeqiang
 * @dateTime 2022/2/11 10:13
 */
public class UserTypeStrategyFactory {
    private final static Map<String, IUserType> USER_TYPE_CACHE = new HashMap<>();

    static {
        USER_TYPE_CACHE.put("促首登", new CuShouDeng());
        USER_TYPE_CACHE.put("促申完", new CuShenWan());
        USER_TYPE_CACHE.put("促首借", new CuShouJie());
        USER_TYPE_CACHE.put("促复借", new CuFuJie());
        USER_TYPE_CACHE.put("重申", new ChongShen());
    }

    public Set<String> getUserTypes() {
        return USER_TYPE_CACHE.keySet();
    }

    public static IUserType getUserTypeStrategy(String userType) {
        return USER_TYPE_CACHE.getOrDefault(userType, new UnknownUserType()).setUserType(userType);
    }

}
