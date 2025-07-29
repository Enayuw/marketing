package com.br.marketing.strategy;

import com.alibaba.fastjson.JSONObject;
import com.br.marketing.client.zhongan.input.ZaSmsRosterLockingDataDTO;
import com.br.marketing.common.utils.DateHelper;
import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.entity.ZhongAnSmsRosterLockingData;
import com.br.marketing.mapper.ZhongAnSmsRosterLockingDataMapper;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Zhong A SMS数据处理程序
 *
 * @author senyang.zheng
 * @date 2025/07/16
 */
@Service
public class ZhongAnSmsDataHandler extends AbstractExternalInterfaceHandler<ZaSmsRosterLockingDataDTO>{

    final static DateTimeFormatter YYYYMMDDSHORTDF = DateTimeFormatter.ofPattern(DateHelper.SHORT_DATE_FORMAT);

    @Autowired
    private ZhongAnSmsRosterLockingDataMapper zhongAnSmsRosterLockingDataMapper;

    @Override
    public JSONObject call(List<ZaSmsRosterLockingDataDTO> transferData, ProcessHandlerContext context) {
        for(ZaSmsRosterLockingDataDTO dataDTO : transferData){
            String today = new Date().toInstant().atZone(ZoneId.systemDefault()).toLocalDate().format(YYYYMMDDSHORTDF);
            Integer createDate = Integer.valueOf(today);
            Date date = new Date();
            ZhongAnSmsRosterLockingData data = new ZhongAnSmsRosterLockingData();
            BeanUtils.copyProperties(dataDTO, data);
            data.setCreateDate(createDate);
            data.setCreateTime(date);
            data.setUpdateTime(date);
            zhongAnSmsRosterLockingDataMapper.insertSelective(data);
        }
        return null;
    }

    @Override
    InterfaceHandlerEnum handlerEnum() {
        return InterfaceHandlerEnum.ZHONGAN_SMS_LOCK_DATA_INSERT;
    }
}
