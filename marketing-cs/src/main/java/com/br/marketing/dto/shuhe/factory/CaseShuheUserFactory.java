package com.br.marketing.dto.shuhe.factory;

import com.br.marketing.dto.shuhe.ShuheTransferJsonDTO;
import com.br.marketing.dto.shuhe.strategy.IUserType;
import com.br.marketing.dto.shuhe.strategy.UserTypeContext;
import com.br.marketing.entity.CaseShuheUserWithBLOBs;
import org.springframework.util.Assert;

/**
 * 数禾转化pojo工厂
 *
 * @author Guo Zeqiang
 * @dateTime 2022/2/11 9:13
 */
public class CaseShuheUserFactory {

    public CaseShuheUserWithBLOBs getCaseShuheUser(IUserType iUserType, ShuheTransferJsonDTO jsonDTO, String apiCode, String jsonData) {
        Assert.notNull(iUserType, "策略不能为空");
        UserTypeContext userTypeContext = UserTypeContext.newInstance(iUserType);
        return userTypeContext.execute(jsonDTO, apiCode, jsonData);
    }

    private CaseShuheUserFactory() {
    }

    private static class CaseShuheUserFactoryInstance {
        private final static CaseShuheUserFactory instance = new CaseShuheUserFactory();
    }

    public static CaseShuheUserFactory newInstance() {
        return CaseShuheUserFactoryInstance.instance;
    }
}
