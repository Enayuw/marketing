package com.br.marketing.service;

import com.br.marketing.commonentity.PageResultReturn;
import com.br.marketing.dto.SoleRuleSearchDTO;
import com.br.marketing.dto.userinfo.UserDetail;
import com.br.marketing.vo.MarketingCustomerVO;
import com.br.marketing.vo.SoleOptLogVO;
import com.br.marketing.vo.SoleRuleDetailVO;
import org.springframework.transaction.annotation.Transactional;

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
    boolean getNameOnly(String soleName,String soleId);

    /**
     * 匹配商户列表,支持模糊搜索
     * @param search
     * @return
     */
    List<MarketingCustomerVO> getCustomer(String search);

    /**
     * 判断商户是否已经被其他规则匹配
     * @param soleId
     * @param customerId
     * @return
     */
    boolean getCusUserType(String soleId, String customerId);

    /**
     * 操作去重规则状态--开启/关闭
     * @param id
     * @param status
     * @return
     */
    boolean updateStatusById(String id, Integer status);

    /**
     * 变更记录查看
     * @param id
     * @return
     */
    List<SoleOptLogVO> getUpdateRecord(String id);

    /**
     * 新增/变更去重规则
     * @param vo
     * @return
     */
    @Transactional
    boolean saveOrUpdate(SoleRuleDetailVO vo, UserDetail userDetail);

    /**
     * 查看去重规则
     * @param id
     * @return
     */
    SoleRuleDetailVO getSoleById(String id);
}
