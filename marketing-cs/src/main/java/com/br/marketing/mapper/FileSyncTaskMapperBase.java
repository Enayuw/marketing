package com.br.marketing.mapper;

import com.br.marketing.entity.FileSyncTask;
import com.br.marketing.entity.FileSyncTaskExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface FileSyncTaskMapperBase {
    long countByExample(FileSyncTaskExample example);

    int deleteByExample(FileSyncTaskExample example);

    int deleteByPrimaryKey(Long id);

    int insert(FileSyncTask record);

    int insertSelective(FileSyncTask record);

    List<FileSyncTask> selectByExample(FileSyncTaskExample example);

    FileSyncTask selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") FileSyncTask record, @Param("example") FileSyncTaskExample example);

    int updateByExample(@Param("record") FileSyncTask record, @Param("example") FileSyncTaskExample example);

    int updateByPrimaryKeySelective(FileSyncTask record);

    int updateByPrimaryKey(FileSyncTask record);
}