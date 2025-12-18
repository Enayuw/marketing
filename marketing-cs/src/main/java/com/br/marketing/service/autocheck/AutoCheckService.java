package com.br.marketing.service.autocheck;

import com.br.marketing.dto.autocheck.SaveAutoCheckConfigDto;
import com.br.marketing.vo.autocheck.AutoConfigVO;
import com.br.marketing.vo.autocheck.SenceVO;

import java.util.List;

public interface AutoCheckService {
    List<AutoConfigVO> getConfigList(String apiCodes, String senceCodes);

    List<SenceVO> searchSenceList(String searchContent);

    Boolean saveAutoCheckConfig(SaveAutoCheckConfigDto dto);
}
