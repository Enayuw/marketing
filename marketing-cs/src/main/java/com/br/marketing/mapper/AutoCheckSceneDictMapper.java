package com.br.marketing.mapper;


import com.br.marketing.vo.autocheck.AutoCheckSceneVO;
import org.apache.ibatis.annotations.Param;


import java.util.List;

public interface AutoCheckSceneDictMapper extends AutoCheckSceneDictMapperBase {

    List<AutoCheckSceneVO> searchSceneList(@Param("searchContent") String searchContent);

    /**
     * 根据场景编码列表查询场景信息
     * @param sceneCodeList 场景编码列表
     * @return 场景列表
     */
    List<AutoCheckSceneVO> selectBySceneCodes(@Param("sceneCodeList") List<String> sceneCodeList);
}