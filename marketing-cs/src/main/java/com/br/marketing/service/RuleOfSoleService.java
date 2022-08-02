package com.br.marketing.service;

import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.commonentity.PageResultReturn;
import com.br.marketing.entity.auth.MarketingUserDetail;
import com.br.marketing.vo.MarketingCustomerVO;
import com.br.marketing.vo.SoleRuleDetailVO;

import java.util.List;
import java.util.Map;

public interface RuleOfSoleService {

    /**
     * 查询去重规则列表
     * @param
     * @return
     */
    PageResultReturn    list(int page, int pageSize, String soleName, Integer status, String apiCodes,String createTimeStart, String createTimeEnd, String updateTimeStart, String updateTimeEnd);

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
     * 操作去重规则状态--开启/关闭
     * @param id
     * @param status
     * @return
     */
    boolean updateStatusById(String id, Integer status, MarketingUserDetail user);

    /**
     * 变更记录查看
     * @param id
     * @return
     */
    PageResultReturn getUpdateRecord(String id,int page,int pageSize);

    /**
     * 新增/变更去重规则
     * @param vo
     * @return
     */
    ApiResult<Boolean> saveOrUpdate(SoleRuleDetailVO vo, MarketingUserDetail userDetail);

    /**
     * 查看去重规则
     * @param id
     * @return
     */
    SoleRuleDetailVO getSoleById(String id);

    /**
     * 根据商户查询usertype
     * @param customerVOs
     * @return
     */
    List<Map> getUserByCus(List<MarketingCustomerVO> customerVOs);
}
