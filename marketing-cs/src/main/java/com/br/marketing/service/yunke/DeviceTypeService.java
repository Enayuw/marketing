package com.br.marketing.service.yunke;

import com.br.marketing.dto.LogEncryptionCellsDto;
import com.br.marketing.vo.yunke.DeviceTypeVO;

import java.util.List;

/**
 * @author peng.kang
 * @date 2025/5/26 18:20
 */
public interface DeviceTypeService {
    List<DeviceTypeVO> getDeviceTypeByLog(List<LogEncryptionCellsDto> cellsDto);
}
