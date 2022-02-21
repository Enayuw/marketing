package com.br.marketing.service.Impl;

import com.br.marketing.entity.PushTransferCustomerLog;
import com.br.marketing.mapper.PushTransferCustomerLogMapper;
import com.br.marketing.service.PushTransferCustomerLogService;
import com.github.pagehelper.PageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

/**
 * @author zeqiang.guo@brgroup.com
 * @dateTime 2021/10/14 17:50
 */
@Service
public class PushTransferCustomerLogServiceImpl implements PushTransferCustomerLogService {

    @Resource
    private PushTransferCustomerLogMapper pushTransferCustomerLogMapper;


    @Override
    public List<PushTransferCustomerLog> findListByStatusIs1(int page, int pageSize, int shardingTotalCount, List<Integer> shardingItems
            , int transferStatus) {
        PageHelper.startPage(page, pageSize);
        return pushTransferCustomerLogMapper.findListByStatusIs1(shardingTotalCount, shardingItems, transferStatus);
    }

    @Override
    public List<PushTransferCustomerLog> findListByStatusIs1(int page, int pageSize, String dateYYYYDDMMStr
            , int transferStatus) {
        PageHelper.startPage(page, pageSize);
        return pushTransferCustomerLogMapper.findListByStatusIs1AndDate(dateYYYYDDMMStr, transferStatus);
    }

    @Override
    public List<PushTransferCustomerLog> findListByStatusAndCodeAndDate(int page, int pageSize, int shardingTotalCount
            , List<Integer> shardingItems, int transferStatus, Date transferInfoTime, String apiCode, int pushStatus) {
        PageHelper.startPage(page, pageSize);
        return pushTransferCustomerLogMapper.findListByStatusAndCodeAndDate(
                shardingTotalCount, shardingItems, apiCode, transferStatus, transferInfoTime, pushStatus);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateByPrimaryKeySelective(PushTransferCustomerLog pushTransferCustomerLog) {
        return pushTransferCustomerLogMapper.updateByPrimaryKeySelective(pushTransferCustomerLog);
    }
}
