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


    Result<List<MarketingCleanDataFile>> getfileMsg(String fileNames, String apiCode);

    List<MarketingCleanDataFile> getfileNames(Integer fileType, String apiCode);

    Result<Long> saveOrUpdateTask(DataCleanRuleDetailDTO dto);

    List<String> getfieldMap(Integer fileType);

    PageResultReturn taskList(int current, int size, String apiCode, String fileType, String status);

    PageResultReturn configList(int current, int size, String apiCode, String fileType);

    Result updateConfig(DataCleanConfigDTO dto);

    Result saveConfig(DataCleanConfigDTO dto);

    Result<Long> runTask(DataCleanRuleDetailDTO dto);

    Result getfileRules(String fileHeader, String apiCode,String fileType);

    Result<Long> testTask(DataCleanRuleDetailDTO dto);

    Result getRuleByID(Long id);

    Result getTaskByID(Long id);
}
