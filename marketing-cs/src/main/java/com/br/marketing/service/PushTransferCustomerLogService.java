package com.br.marketing.service;

import com.br.marketing.entity.PushTransferCustomerLog;

import java.util.Date;
import java.util.List;

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
     * @param page               页号
     * @param pageSize           页大小
     * @param shardingTotalCount 总分片数
     * @param shardingItems      获取到分片
     * @return List<PushTransferCustomerFailLog>
     * @author Guo Zeqiang
     * @dateTime 2021/10/14 17:53
     */
    List<PushTransferCustomerLog> findListByStatusIs1(int page, int pageSize, int shardingTotalCount, List<Integer> shardingItems
            , int transferStatus);

    /**
     * 获取需要补偿的转化数据
     *
     * @param page            页号
     * @param pageSize        页大小
     * @param dateYYYYDDMMStr 当前时间
     * @return List<PushTransferCustomerFailLog>
     * @author Guo Zeqiang
     * @dateTime 2021/10/14 17:53
     */
    List<PushTransferCustomerLog> findListByStatusIs1(int page, int pageSize, String dateYYYYDDMMStr
            , int transferStatus);

    /**
     * 获取需要补偿的转化数据
     *
     * @param page               页号
     * @param pageSize           页大小
     * @param shardingTotalCount 总分片数
     * @param shardingItems      获取到分片
     * @return List<PushTransferCustomerFailLog>
     * @author Guo Zeqiang
     * @dateTime 2021/10/14 17:53
     */
    List<PushTransferCustomerLog> findListByStatusAndCodeAndDate(int page, int pageSize, int shardingTotalCount, List<Integer> shardingItems
            , int transferStatus, Date transferInfoTime, String apiCode, int pushStatus);

    /**
     * 更新数据
     *
     * @author Guo Zeqiang
     * @dateTime 2021/10/14 17:53
     */
    int updateByPrimaryKeySelective(PushTransferCustomerLog pushTransferCustomerLog);
}
