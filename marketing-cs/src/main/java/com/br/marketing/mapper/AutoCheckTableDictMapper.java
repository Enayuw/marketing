package com.br.marketing.mapper;

import com.br.marketing.entity.AutoCheckTableDict;
import com.br.marketing.vo.autocheck.AutoCheckAssociationTableVO;
import com.br.marketing.vo.autocheck.AutoCheckTableColumnVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface AutoCheckTableDictMapper extends AutoCheckTableDictMapperBase {

    List<AutoCheckAssociationTableVO> getAssociationTable(@Param("tableName") String tableName);

    /**
     * 批量查询指定表的字段信息（字段名/字段注释）。
     * <p>注意：表不存在时 information_schema 无记录返回（不会抛 1146）。</p>
     */
    List<AutoCheckTableColumnVO> getAssociationTableColumns(@Param("tableNameList") List<String> tableNameList);

    /**
     * 批量保存（仅插入，不做幂等处理；幂等由 service 层控制）。
     */
    void batchInsert(@Param("saveList") List<AutoCheckTableDict> saveList);
}