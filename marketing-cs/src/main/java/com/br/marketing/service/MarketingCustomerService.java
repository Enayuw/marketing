package com.br.marketing.service;

import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.commonentity.PageResultReturn;
import com.br.marketing.entity.MarketingCustomer;
import com.br.marketing.entity.auth.MarketingUserDetail;
import com.br.marketing.vo.CustomerSelectVO;
import com.br.marketing.vo.MarketingCustomerListVO;
import com.br.marketing.vo.MarketingCustomerVO;

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

    /**
     * 获取客户信息列表数据
     * @param page
     * @param pageSize
     * @param cid
     * @param apiCode
     * @return
     */
    PageResultReturn getCustomerList(int page, int pageSize, String cid, String apiCode,String accountType,String accountStatus);

    /**
     * 新增/变更用户信息
     * @param vo
     * @param user
     * @return
     */
    ApiResult<Boolean> saveOrUpdateCustomer(MarketingCustomerListVO vo, MarketingUserDetail user);

    /**
     * apiCode是否重复
     * @param apiCode
     * @return
     */
    ApiResult<Boolean> apiCodeOnly(String id,String apiCode);

    /**
     * ApiCode列表,支持联想输入
     * @param apiCode
     * @return
     */
    List<MarketingCustomerVO> getApiCodeList(String apiCode);

    /**
     * ApiCode列表
     * @param apiCodeList
     * @return
     */
    List<MarketingCustomerVO> getApiCodeList(List<String> apiCodeList);

    /**
     * 客户名称/客户编号,支持联想输入
     *
     * @param search
     * @return
     */
    List<MarketingCustomerVO> getCidOrName(String search);


    /**
     * 根据apiCode获取客户信息
     *
     * @param apiCode apiCode
     * @return {@link MarketingCustomer}
     * @author Hua Qiang
     * @dateTime 2024/3/12 10:35
     */
    MarketingCustomer getCacheCustomerByApiCode(String apiCode);

    /**
     * 获取所有正式的apiCode
     * @return
     */
    List<String> getApiCodeByProd(List<String> apiCodePrefix);

    String getThreeKEncryptType();

}
