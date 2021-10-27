package com.br.marketing.service;

import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.commonentity.PageResultReturn;
import com.br.marketing.dto.userinfo.UserDetail;
import com.br.marketing.vo.VariableDicListVO;
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
    ApiResult<Boolean> saveOrUpdateVariableDic(VariableDicListVO vo, UserDetail user);

    /**
     * 删除客户配置变量值
     * @param id
     * @return
     */
    //ApiResult<Boolean> delete(Integer id);
}
