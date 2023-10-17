package com.br.marketing.service.Impl.guomei;

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
    public Long saveTransferDataHandler(GuoMeiTransferData guoMeiTransferData) {
        int i = guoMeiTransferDataMapper.insertSelective(guoMeiTransferData);
        if (i > 0) {
            // TODO: 2023-10-17 推送转化数据接入标准逻辑
            return 0L;
        }
        return null;
    }
}
