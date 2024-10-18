package com.br.marketing.strategy;

import com.alibaba.fastjson.JSONObject;
import com.br.marketing.client.robotaiapi.input.InterfaceData;
import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.entity.MarketingSyncUser;
import com.br.marketing.entity.WubaSubmitConversionData;
import com.br.marketing.mapper.WubaSubmitConversionDataMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * 360促完件用户信息批量查询
 *
 * @Author lixiang
 * @Date 2024-10-19
 */
@Service
@Slf4j
public class QiFuCuWanJianBatQryUserRealHandler extends AbstractExternalInterfaceHandler<InterfaceData<MarketingSyncUser>> {
    private final static String TITLE = "【360促完件用户信息批量查询】";

    @Resource
    private WubaSubmitConversionDataMapper dataMapper;

    @Override
    public JSONObject call(List<InterfaceData<MarketingSyncUser>> list, ProcessHandlerContext context) {
        List<WubaSubmitConversionData> dataList = new ArrayList<>();
//        for(WuBaSubmitConversionDataDto dto : list){
//            WubaSubmitConversionData data = dto.getWubaSubmitConversionData();
//            if(data !=null) {
//                dataList.add(data);
//            }
//        }

        if(!CollectionUtils.isEmpty(dataList)){
            dataMapper.batchAdd(dataList);
            log.warn(TITLE + "批量入库成功");
        }
        return null;
    }

    @Override
    public InterfaceHandlerEnum handlerEnum() {
        return InterfaceHandlerEnum.QIFU_CUWANJIAN_BAT_QRY_USER_REAL;
    }
}
