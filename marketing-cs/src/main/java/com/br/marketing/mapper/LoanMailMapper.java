package com.br.marketing.mapper;

import org.springframework.stereotype.Repository;

/**
 * Created by Bairong on 2020/7/14.
 */
@Repository
public interface LoanMailMapper {
    /**
     *查询客户配置的邮箱账号
     * @param apiCode 客户编号
     * @return
     */
    String queryMails(String apiCode);
}
