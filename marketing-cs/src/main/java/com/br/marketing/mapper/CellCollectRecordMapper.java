package com.br.marketing.mapper;

import com.br.marketing.entity.CellCollectRecord;
import com.br.marketing.entity.CellCollectRecordExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface CellCollectRecordMapper {
    int countByExample(CellCollectRecordExample example);

    int deleteByExample(CellCollectRecordExample example);

    int deleteByPrimaryKey(Long id);

//This method inserts a CellCollectRecord object into the database
    int insert(CellCollectRecord record);

    int insertSelective(CellCollectRecord record);

    List<CellCollectRecord> selectByExample(CellCollectRecordExample example);

    CellCollectRecord selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") CellCollectRecord record, @Param("example") CellCollectRecordExample example);

    int updateByExample(@Param("record") CellCollectRecord record, @Param("example") CellCollectRecordExample example);

    int updateByPrimaryKeySelective(CellCollectRecord record);

    int updateByPrimaryKey(CellCollectRecord record);

    int updateStatusByApiCode(@Param("apiCode") String apiCode, @Param("status") Integer status);
    int updateStatus(@Param("status") Integer status);

    void updateMaxIdByApiCode(@Param("apiCode") String apiCode, @Param("maxId") Long maxId);
}