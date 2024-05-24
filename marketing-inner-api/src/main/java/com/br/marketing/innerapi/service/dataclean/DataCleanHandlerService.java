package com.br.marketing.innerapi.service.dataclean;

import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.commonentity.PageResultReturn;
import com.br.marketing.dto.dataclean.DataCleanConfigDTO;
import com.br.marketing.dto.dataclean.DataCleanRuleDetailDTO;
import com.br.marketing.entity.MarketingCleanDataFile;

import java.util.List;

/**
 * 数据清洗service
 */
public interface DataCleanHandlerService {


    Result<MarketingCleanDataFile> getfileMsg(String fileNames, String apiCode);

    List<String> getfileNames(Integer fileType, String apiCode);

    Result<Long> saveTask(DataCleanRuleDetailDTO dto);

    List<String> getfieldMap(Integer fileType);

    PageResultReturn taskList(int current, int size, String apiCode, String fileType, String status);

    PageResultReturn configList(int current, int size, String apiCode, String fileType);

    Result updateConfig(DataCleanConfigDTO dto);

    Result saveConfig(DataCleanConfigDTO dto);
}
