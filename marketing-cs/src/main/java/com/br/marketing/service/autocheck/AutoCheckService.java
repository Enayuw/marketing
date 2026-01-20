package com.br.marketing.service.autocheck;

import com.br.marketing.dto.autocheck.QueryAssociationTableFieldDto;
import com.br.marketing.dto.autocheck.BatchInitAutoCheckSceneDictDto;
import com.br.marketing.dto.autocheck.BatchInitAutoCheckTableDictDto;
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

    List<AutoCheckResultVO> getResultList(String apiCodes, String sceneCodes, String startTime, String endTime);

    List<AutoCheckAssociationTableVO> getAssociationTable(String tableName);

    List<AutoCheckAssociationTableFieldVO> getAssociationTableFields(QueryAssociationTableFieldDto dto);

    /**
     * 初始化/维护场景字典（批量）。
     * <p>按 sceneCode 幂等写入：不存在则新增，存在则更新名称并恢复 is_deleted=0。</p>
     */
    AutoCheckDictInitResultVO initSceneDictBatch(BatchInitAutoCheckSceneDictDto dto);

    /**
     * 初始化/维护关联表字典（批量）。
     * <p>按 tableName 幂等写入：不存在则新增，存在则更新描述并恢复 is_deleted=0。</p>
     */
    AutoCheckDictInitResultVO initTableDictBatch(BatchInitAutoCheckTableDictDto dto);
}
