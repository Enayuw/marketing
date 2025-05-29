package com.br.marketing.service.Impl.yunke;

import com.br.marketing.common.commondto.Result;
import com.br.marketing.dto.LogEncryptionCellsDto;
import com.br.marketing.dto.TxtToDbDTO;
import com.br.marketing.mapper.MarketingDeviceTypeMapper;
import com.br.marketing.service.yunke.DeviceTypeService;
import com.br.marketing.vo.yunke.DeviceTypeVO;
import org.checkerframework.checker.units.qual.A;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author peng.kang
 * @date 2025/5/26 18:22
 */
@Service
public class DeviceTypeServiceImpl implements DeviceTypeService {

    @Resource
    MarketingDeviceTypeMapper marketingDeviceTypeMapper;
    @Override
    public List<DeviceTypeVO> getDeviceTypeByLog(List<LogEncryptionCellsDto> logCells) {
       return marketingDeviceTypeMapper.getDeviceTypesByLogCells(logCells);
    }
}
