package com.br.marketing.strategy;

import com.alibaba.fastjson.JSONObject;
import com.br.marketing.client.zhongan.input.ZaRosterLockingDataDTO;
import com.br.marketing.common.utils.DateHelper;
import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.entity.ZhonganRosterLockingData;
import com.br.marketing.mapper.ZhonganRosterLockingDataMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;

/**
 * @Description : 众安落库
 * @Author : juanjuan.song
 * @Date : Create in 2022/11/11 11:31
 */
@Service
public class ZhongAnDataHandler extends AbstractExternalInterfaceHandler<ZaRosterLockingDataDTO>{

    final static DateTimeFormatter YYYYMMDDSHORTDF = DateTimeFormatter.ofPattern(DateHelper.SHORT_DATE_FORMAT);

    @Autowired
    private ZhonganRosterLockingDataMapper zhonganRosterLockingDataMapper;

    @Override
    public JSONObject call(List<ZaRosterLockingDataDTO> transferData, ProcessHandlerContext context) {
        for(ZaRosterLockingDataDTO dataDTO : transferData){
            String today = new Date().toInstant().atZone(ZoneId.systemDefault()).toLocalDate().format(YYYYMMDDSHORTDF);
            Integer createDate = Integer.valueOf(today);
            Date date = new Date();
            ZhonganRosterLockingData data = new ZhonganRosterLockingData();
            BeanUtils.copyProperties(dataDTO, data);
            data.setCreateDate(createDate);
            data.setCreateTime(date);
            data.setUpdateTime(date);
            zhonganRosterLockingDataMapper.insertSelective(data);
        }
        return null;
    }

    @Override
    InterfaceHandlerEnum handlerEnum() {
        return InterfaceHandlerEnum.ZHONGAN_LOCK_DATA_INSERT;
    }
}
