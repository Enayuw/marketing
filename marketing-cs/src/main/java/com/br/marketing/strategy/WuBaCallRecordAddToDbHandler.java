package com.br.marketing.strategy;

import com.alibaba.fastjson.JSONObject;
import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.dto.wuba.WuBaSubmitConversionDataDto;
import com.br.marketing.entity.WubaSubmitConversionData;
import com.br.marketing.mapper.WubaSubmitConversionDataMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * 58新客通话明细入库-3710155
 *
 * @Author lixiang
 * @Date 2024-07-23
 */
@Service
@Slf4j
public class WuBaCallRecordAddToDbHandler extends AbstractExternalInterfaceHandler<WuBaSubmitConversionDataDto> {
    private final static String TITLE = "【58新客通话明细入库-3710155】";

    @Resource
    private WubaSubmitConversionDataMapper dataMapper;

    @Override
    JSONObject call(List<WuBaSubmitConversionDataDto> list, ProcessHandlerContext context) {
        List<WubaSubmitConversionData> dataList = new ArrayList<>();
        for(WuBaSubmitConversionDataDto dto : list){
            WubaSubmitConversionData data = dto.getWubaSubmitConversionData();
            if(data !=null) {
                dataList.add(data);
            }
        }

        if(!CollectionUtils.isEmpty(dataList)){
            dataMapper.batchAdd(dataList);
            log.warn(TITLE + "批量入库成功");
        }
        return null;
    }

    @Override
    InterfaceHandlerEnum handlerEnum() {
        return InterfaceHandlerEnum.WUBA_CALL_RECORD_ADD_DB;
    }
}
