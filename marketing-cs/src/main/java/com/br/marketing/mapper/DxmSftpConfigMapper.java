package com.br.marketing.mapper;


import com.br.marketing.entity.DxmSftpConfig;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface DxmSftpConfigMapper extends DxmSftpConfigMapperBase{

    /**
     * 根据API编码查询启用的配置
     *
     * @param apiCode API编码
     * @return SFTP配置
     */
    DxmSftpConfig selectByApiCode(@Param("apiCode") String apiCode,
                                  @Param("type") int type);
}