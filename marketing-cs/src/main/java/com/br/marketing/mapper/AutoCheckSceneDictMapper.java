package com.br.marketing.mapper;


import com.br.marketing.vo.autocheck.AutoCheckSceneVO;
import org.apache.ibatis.annotations.Param;

import com.br.marketing.entity.AutoCheckSceneDict;

import java.util.List;

public interface AutoCheckSceneDictMapper extends AutoCheckSceneDictMapperBase {

    List<AutoCheckSceneVO> searchSceneList(@Param("searchContent") String searchContent);

    /**
     * 根据场景编码列表查询场景信息
     * @param sceneCodeList 场景编码列表
     * @return 场景列表
     */
    List<AutoCheckSceneVO> selectBySceneCodes(@Param("sceneCodeList") List<String> sceneCodeList);

    /**
     * 批量保存（仅插入，不做幂等处理；幂等由 service 层控制）。
     */
    void batchInsert(@Param("saveList") List<AutoCheckSceneDict> saveList);
}