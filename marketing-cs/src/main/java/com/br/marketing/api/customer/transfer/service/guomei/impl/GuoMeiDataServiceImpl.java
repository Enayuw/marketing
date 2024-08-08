package com.br.marketing.api.customer.transfer.service.guomei.impl;

import com.br.marketing.api.customer.transfer.service.guomei.IGuoMeiDataService;
import com.br.marketing.entity.GuoMeiTransferData;
import com.br.marketing.mapper.GuoMeiTransferDataMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

/**
 * 国美数据入库
 *
 * @author Guo Zeqiang
 * @dateTime 2023/10/16 16:24
 */
@Service
public class GuoMeiDataServiceImpl implements IGuoMeiDataService {
    @Resource
    private GuoMeiTransferDataMapper guoMeiTransferDataMapper;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public int saveTransferDataHandler(GuoMeiTransferData guoMeiTransferData) {
        return guoMeiTransferDataMapper.insertSelective(guoMeiTransferData);
    }
}
