package com.br.marketing.service;

import com.br.marketing.vo.CustomerSelectVO;

import java.util.List;

/**
 * 客户业务接口
 *
 * @author zeqiang.guo@brgroup.com
 * @dateTime 2021/9/1 15:33
 */
public interface MarketingCustomerService {

    /**
     * 获取客户cid或apiCode
     * 当参数{@code cid} 不为空时，结果集为apiCode集合
     * 为空时，结果集为cid集合
     *
     * @param cid 客户编号
     * @return {@link List<CustomerSelectVO>}
     * @author zeqiang.guo@brgroup.com
     * @dateTime 2021/9/1 15:35
     */
    List<CustomerSelectVO> getCidOrApiCodeList(String cid);
}
