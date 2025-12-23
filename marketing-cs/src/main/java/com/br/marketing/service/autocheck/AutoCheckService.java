package com.br.marketing.service.autocheck;

import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.dto.autocheck.SaveAutoCheckConfigDto;
import com.br.marketing.vo.autocheck.AutoCheckResultVO;
import com.br.marketing.vo.autocheck.AutoCheckConfigVO;
import com.br.marketing.vo.autocheck.AutoCheckSceneVO;

import java.util.List;

public interface AutoCheckService {
    List<AutoCheckConfigVO> getAutoCheckConfigList(String apiCodes, String sceneCodes);

    List<AutoCheckSceneVO> getAutoCheckSceneList(String searchContent);

    ApiResult<Boolean> saveAutoCheckConfig(SaveAutoCheckConfigDto dto);

    Boolean delAutoCheckConfig(Long id);

    void autoCheck();

    List<AutoCheckResultVO> getResultList(String apiCodes, String sceneCodes);
}
