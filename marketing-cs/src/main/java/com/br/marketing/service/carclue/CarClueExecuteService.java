package com.br.marketing.service.carclue;

import com.br.marketing.dto.CarClueReportDTO;
import com.br.marketing.entity.*;

import java.util.List;
import java.util.Optional;

/**
 * @ClassName CarClueExecuteService
 * @Author kongbx
 * @Date 2025/5/7 14:36
 */
public interface CarClueExecuteService {
    Optional<CarClueManageConfig> getCarClueConfig();
    List<String> getValueByKey(String key);
    List<CarClueInfo> processClueByIds(String clueIds);
    CarClueReportDTO processClueByRange(String clueRangeJson);
    List<CarClueProvincesInformation> getProvincesInfo();
    List<CarClueSeriesInformation> getSeriesInfo();
    List<CarClueRelationalMapping> getRelationalMapping();
    List<CarChannelConfig> getChannelConfig();

}
