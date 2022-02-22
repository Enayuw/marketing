package com.br.marketing.dto.shuhe.strategy;

import com.br.marketing.dto.shuhe.ShuheTransferJsonDTO;
import com.br.marketing.entity.CaseShuheUserWithBLOBs;

/**
 * 场景策略上下文
 *
 * @author Guo Zeqiang
 * @dateTime 2022/2/10 16:14
 */
public class UserTypeContext {

    private IUserType iUserType;
    private volatile static UserTypeContext USER_TYPE_STRATEGY_CONTEXT;

    private UserTypeContext() {
    }

    private UserTypeContext(IUserType iUserType) {
        this.iUserType = iUserType;
    }

    public CaseShuheUserWithBLOBs execute(ShuheTransferJsonDTO jsonDTO, String apiCode, String jsonData) {
        final CaseShuheUserWithBLOBs caseUser = this.iUserType.initCaseUser(jsonDTO, apiCode, jsonData);
        this.iUserType.setTotalField(jsonDTO.getDataItem(), caseUser);
        this.iUserType.getCaseUser(jsonDTO.getDataItem(), caseUser);
        return caseUser;
    }

    public static UserTypeContext newInstance(IUserType iUserType) {
        if (USER_TYPE_STRATEGY_CONTEXT == null) {
            synchronized (UserTypeContext.class) {
                if (USER_TYPE_STRATEGY_CONTEXT == null) {
                    USER_TYPE_STRATEGY_CONTEXT = new UserTypeContext(iUserType);
                }
            }
        }
        USER_TYPE_STRATEGY_CONTEXT.iUserType = iUserType;
        return USER_TYPE_STRATEGY_CONTEXT;
    }


}
