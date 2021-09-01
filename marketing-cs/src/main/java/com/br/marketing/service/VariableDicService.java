package com.br.marketing.service;

import com.br.marketing.vo.VariableDicSelectVO;

import java.util.List;

/**
 * 客户配置变量值字典
 *
 * @author zeqiang.guo@brgroup.com
 * @dateTime 2021/9/1 17:28
 */
public interface VariableDicService {
    /**
     * 通过cid、apiCode查询字典集合
     *
     * @param cid     合作客户id
     * @param apiCode 接口编号
     * @return {@link List<VariableDicSelectVO>}
     * @author zeqiang.guo@brgroup.com
     * @dateTime 2021/9/1 17:55
     */
    List<VariableDicSelectVO> findListByCidAndApiCode(String cid, String apiCode);
}
