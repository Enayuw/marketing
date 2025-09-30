package com.br.marketing.mapper;

import org.apache.ibatis.annotations.Param;

public interface SftpUploadTaskMapper extends SftpUploadTaskMapperBase {

    void postExecuteSql(@Param("sql") String sql);


}
