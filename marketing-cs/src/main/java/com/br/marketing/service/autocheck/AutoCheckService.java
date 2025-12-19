package com.br.marketing.service.autocheck;

import com.br.marketing.dto.autocheck.SaveAutoCheckConfigDto;
import com.br.marketing.vo.autocheck.AutoCheckResultVO;
import com.br.marketing.vo.autocheck.AutoCheckConfigVO;
import com.br.marketing.vo.autocheck.AutoCheckSenceVO;

import java.util.List;

public interface AutoCheckService {
    List<AutoCheckConfigVO> getAutoCheckConfigList(String apiCodes, String senceCodes);

    List<AutoCheckSenceVO> getAutoCheckSenceList(String searchContent);

    Boolean saveAutoCheckConfig(SaveAutoCheckConfigDto dto);

    Boolean delAutoCheckConfig(Long id);

    List<AutoCheckResultVO> getResultList(String apiCodes, String senceCodes);
}
