package com.br.marketing.mapper;

import org.apache.ibatis.annotations.Param;

public interface FileSyncTaskMapper extends FileSyncTaskMapperBase {
    
    /**
     * 执行后置SQL
     * @param sql SQL语句
     */
    void postExecuteSql(@Param("sql") String sql);
}
