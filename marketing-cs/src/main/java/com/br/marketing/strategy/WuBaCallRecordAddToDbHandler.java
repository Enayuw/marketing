package com.br.marketing.strategy;

import com.alibaba.fastjson.JSONObject;
import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.dto.wuba.WuBaSubmitConversionDataDto;
import com.br.marketing.entity.WubaSubmitConversionData;
import com.br.marketing.mapper.WubaSubmitConversionDataMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 推送携程(3710058/3710078)
 *
 * @Author lixiang
 * @Date 2024-07-23
 */
@Service
public class WuBaCallRecordAddToDbHandler extends AbstractExternalInterfaceHandler<WuBaSubmitConversionDataDto> {

    @Resource
    private WubaSubmitConversionDataMapper dataMapper;

    @Override
    JSONObject call(List<WuBaSubmitConversionDataDto> list, ProcessHandlerContext context) {
        List<WubaSubmitConversionData> dataList = list.stream().map(WuBaSubmitConversionDataDto::getWubaSubmitConversionData)
                .collect(Collectors.toList());
        return null;
    }

    @Override
    InterfaceHandlerEnum handlerEnum() {
        return InterfaceHandlerEnum.WUBA_CALL_RECORD_ADD_DB;
    }
}
