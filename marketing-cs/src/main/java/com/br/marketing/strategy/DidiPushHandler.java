package com.br.marketing.strategy;

import com.alibaba.fastjson.JSONObject;
import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.entity.DidiCallRecord;
import com.br.marketing.mapper.DidiCallRecordMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;


/**
 * 推送滴滴
 *
 * @author guangchao.zhang
 * @dateTime 2023/04/26 16:53
 */
@Service
public class DidiPushHandler extends AbstractExternalInterfaceHandler<DidiCallRecord> {

    @Resource
    private DidiCallRecordMapper  didiCallRecordMapper;


    @Override
    JSONObject call(List<DidiCallRecord> list, ProcessHandlerContext context) {
        for (DidiCallRecord dto : list) {
            if (dto == null) {
                continue;
            }
            int i = didiCallRecordMapper.insertSelective(dto);
        }
        return null;
    }

    @Override
    InterfaceHandlerEnum handlerEnum() {
        return InterfaceHandlerEnum.DIDI_CALL_RECORD_INSERT_DB;
    }
}
