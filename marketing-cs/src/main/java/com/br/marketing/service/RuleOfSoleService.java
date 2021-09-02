package com.br.marketing.service;

import com.br.marketing.commonentity.PageResultReturn;
import com.br.marketing.dto.SoleRuleSearchDTO;
import com.br.marketing.entity.CustomerSole;
import com.br.marketing.entity.MarketingCustomer;
import com.br.marketing.vo.SoleOptLogVO;

import java.util.List;

public interface RuleOfSoleService {

    /**
     * 查询去重规则列表
     * @param dto
     * @return
     */
    PageResultReturn list(SoleRuleSearchDTO dto, int page, int pageSize);

    /**
     * 判断规则名称是否重复
     * @param soleName
     * @return
     */
    boolean getNameOnly(String soleName);

    /**
     * 匹配商户列表,支持模糊搜索
     * @param search
     * @return
     */
    List<MarketingCustomer> getCustomer(String search);

    /**
     * 判断商户是否已经被其他规则匹配
     * @param soleId
     * @param customerId
     * @return
     */
    boolean getCusUserType(Long soleId, Long customerId);

    /**
     * 操作去重规则状态--开启/关闭
     * @param id
     * @param status
     * @return
     */
    boolean updateStatusById(Long id, Integer status);

    /**
     * 变更记录查看
     * @param id
     * @return
     */
    List<SoleOptLogVO> getUpdateRecord(Long id);
}
