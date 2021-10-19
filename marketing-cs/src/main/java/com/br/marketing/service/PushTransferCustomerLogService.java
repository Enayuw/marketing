package com.br.marketing.service;

import com.br.marketing.commonentity.PageResultReturn;

/**
 * 接口转化推送客服失败记录
 *
 * @author zeqiang.guo@brgroup.com
 * @dateTime 2021/10/14 17:48
 */
public interface PushTransferCustomerLogService {

    /**
     * 获取需要补偿的转化数据
     *
     * @param page     页号
     * @param pageSize 页大小
     * @return List<PushTransferCustomerFailLog>
     * @author Guo Zeqiang
     * @dateTime 2021/10/14 17:53
     */
    PageResultReturn findListByStatusIs0(int page, int pageSize);
}
