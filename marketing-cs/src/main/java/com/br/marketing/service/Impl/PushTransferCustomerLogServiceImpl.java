package com.br.marketing.service.Impl;

import com.br.marketing.commonentity.PageResultReturn;
import com.br.marketing.entity.PushTransferCustomerLog;
import com.br.marketing.mapper.PushTransferCustomerLogMapper;
import com.br.marketing.service.PushTransferCustomerLogService;
import com.github.pagehelper.PageHelper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
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
    public PageResultReturn findListByStatusIs0(int page, int pageSize) {
        PageHelper.startPage(page, pageSize);
        List<PushTransferCustomerLog> list = pushTransferCustomerLogMapper.findListByStatusIs0();
        return PageResultReturn.setPageResult(list, page);
    }
}
