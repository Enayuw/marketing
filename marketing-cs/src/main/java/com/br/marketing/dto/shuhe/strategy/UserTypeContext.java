package com.br.marketing.dto.shuhe.strategy;

import com.br.marketing.dto.shuhe.ShuheTransferJsonDTO;
import com.br.marketing.entity.CaseShuheUser;

/**
 * 场景策略上下文
 *
 * @author Guo Zeqiang
 * @dateTime 2022/2/10 16:14
 */
public class UserTypeContext {

    private final ThreadLocal<BaseUserType> baseUserTypeThreadLocal = new ThreadLocal<>();
    private volatile static UserTypeContext USER_TYPE_STRATEGY_CONTEXT;

    private UserTypeContext() {
    }

    private UserTypeContext(BaseUserType baseUserType) {
        this.baseUserTypeThreadLocal.set(baseUserType);
    }

    public CaseShuheUser execute(ShuheTransferJsonDTO jsonDTO, String apiCode, String jsonData) {
        final CaseShuheUser caseUser = baseUserTypeThreadLocal.get().initCaseUser(jsonDTO, apiCode, jsonData);
        baseUserTypeThreadLocal.get().setTotalField(jsonDTO.getDataItem(), caseUser);
        removeBaseUserType();
        return caseUser;
    }

    public void removeBaseUserType() {
        baseUserTypeThreadLocal.remove();
    }

    public static UserTypeContext newInstance(BaseUserType baseUserType) {
        if (USER_TYPE_STRATEGY_CONTEXT == null) {
            synchronized (UserTypeContext.class) {
                if (USER_TYPE_STRATEGY_CONTEXT == null) {
                    USER_TYPE_STRATEGY_CONTEXT = new UserTypeContext(baseUserType);
                } else {
                    USER_TYPE_STRATEGY_CONTEXT.baseUserTypeThreadLocal.set(baseUserType);
                }
            }
        } else {
            USER_TYPE_STRATEGY_CONTEXT.baseUserTypeThreadLocal.set(baseUserType);
        }
        return USER_TYPE_STRATEGY_CONTEXT;
    }


}
