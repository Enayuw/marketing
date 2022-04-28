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

    private final ThreadLocal<IUserType> iUserType = new ThreadLocal<>();
    private volatile static UserTypeContext USER_TYPE_STRATEGY_CONTEXT;

    private UserTypeContext() {
    }

    private UserTypeContext(IUserType iUserType) {
        this.iUserType.set(iUserType);
    }

    public CaseShuheUser execute(ShuheTransferJsonDTO jsonDTO, String apiCode, String jsonData) {
        final CaseShuheUser caseUser = iUserType.get().initCaseUser(jsonDTO, apiCode, jsonData);
        iUserType.get().setTotalField(jsonDTO.getDataItem(), caseUser);
        removeIUserType();
        return caseUser;
    }

    public void removeIUserType() {
        iUserType.remove();
    }

    public static UserTypeContext newInstance(IUserType iUserType) {
        if (USER_TYPE_STRATEGY_CONTEXT == null) {
            synchronized (UserTypeContext.class) {
                if (USER_TYPE_STRATEGY_CONTEXT == null) {
                    USER_TYPE_STRATEGY_CONTEXT = new UserTypeContext(iUserType);
                } else {
                    USER_TYPE_STRATEGY_CONTEXT.iUserType.set(iUserType);
                }
            }
        } else {
            USER_TYPE_STRATEGY_CONTEXT.iUserType.set(iUserType);
        }
        return USER_TYPE_STRATEGY_CONTEXT;
    }


}
