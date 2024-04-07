package com.br.marketing.service;

import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.commonentity.PageResultReturn;
import com.br.marketing.entity.auth.MarketingUserDetail;
import com.br.marketing.vo.CustomerSelectVO;
import com.br.marketing.vo.VariableDicListVO;
import com.br.marketing.vo.VariableDicSelectVO;

import java.util.List;
import java.util.Map;

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

    /**
     * 客户配置变量值列表数据
     * @param page
     * @param pageSize
     * @param cid
     * @param apiCode
     * @return
     */
    PageResultReturn getVariableDicList(int page, int pageSize, String cid, String apiCode);

    /**
     * 新增/变更客户配置变量值字典
     * @param vo
     * @param user
     * @return
     */
    ApiResult<Boolean> saveOrUpdateVariableDic(VariableDicListVO vo, MarketingUserDetail user);

    /**
     * 场景列表，支持apicode多选
     * @param vos
     * @return
     */
    List<Map> findListByCidsAndApiCodes(List<CustomerSelectVO> vos);


    /**
     * 删除客户配置变量值
     * @param id
     * @return
     */
    //ApiResult<Boolean> delete(Integer id);

    /**
     * 2023-07-07 15:18
     * 批量新增场景，已存在的场景不再添加
     *
     * @param msgStr 场景集合json字符串
     * @return Boolean
     */
    Result<Boolean> batchAddUserTypeVariableDicTry(String msgStr);

    /**
     * 2023-07-07 15:18
     * 延迟发送的消息主键
     *
     * @param redisKey 消息
     * @return Boolean
     */
    Result<Boolean> delaySendUserTypeMessage(String redisKey);
}
