package com.br.marketing.strategy;

import com.alibaba.fastjson.JSONObject;
import com.br.marketing.client.didi.DidiCallBackDataDTO;
import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.entity.DidiCallBackData;
import com.br.marketing.entity.DidiCallRecord;
import com.br.marketing.mapper.DidiCallBackDataMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 *
 * @Author xiong.luo
 * @Date 2025-12-18
 */
@Service
@Slf4j
public class DidiCallRecordHandler extends AbstractExternalInterfaceHandler<DidiCallBackDataDTO> {

    @Autowired
    private DidiCallBackDataMapper didiCallBackDataMapper;

    @Override
    JSONObject call(List<DidiCallBackDataDTO> list, ProcessHandlerContext context) {
        List<DidiCallBackData> dataList = new ArrayList<>();
        for (DidiCallBackDataDTO dto : list) {
            Date date = new Date();
            DidiCallBackData data = new DidiCallBackData();
            BeanUtils.copyProperties(dto, data);
            data.setCreateTime(date);
            data.setUpdateTime(date);
            dataList.add(data);
        }
        didiCallBackDataMapper.batchAdd(dataList);
        return null;
    }

    @Override
    InterfaceHandlerEnum handlerEnum() {
        return InterfaceHandlerEnum.DIDI_CALL_RECORD;
    }
}
