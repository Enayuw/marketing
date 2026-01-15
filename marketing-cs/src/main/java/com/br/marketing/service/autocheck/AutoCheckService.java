package com.br.marketing.service.autocheck;

import com.br.marketing.dto.autocheck.QueryAssociationTableFieldDto;
import com.br.marketing.dto.autocheck.SaveAutoCheckConfigDto;
import com.br.marketing.dto.autocheck.SaveAutoCheckConfigResDto;
import com.br.marketing.vo.autocheck.*;

import java.util.List;

public interface AutoCheckService {
    List<AutoCheckConfigVO> getAutoCheckConfigList(String apiCodes, String sceneCodes);

    List<AutoCheckSceneVO> getAutoCheckSceneList(String searchContent);

    SaveAutoCheckConfigResDto saveAutoCheckConfig(SaveAutoCheckConfigDto dto);

    Boolean delAutoCheckConfig(String apiCode, String sceneCode);

    void autoCheck();

    List<AutoCheckResultVO> getResultList(String apiCodes, String sceneCodes);

    List<AutoCheckAssociationTableVO> getAssociationTable(String tableName);

    List<AutoCheckAssociationTableFieldVO> getAssociationTableFields(QueryAssociationTableFieldDto dto);
}
