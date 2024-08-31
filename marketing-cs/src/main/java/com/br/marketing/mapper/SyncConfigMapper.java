package com.br.marketing.mapper;

import com.br.marketing.entity.SyncConfig;
import com.br.marketing.mysqlInterceptor.AddDataAuth;
import com.br.marketing.vo.SyncConfigEditVO;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SyncConfigMapper extends SyncConfigMapperBase {
    /**
     * 插入文件同步配置
     * @param loanSyncConfig 配置对象
     */
    void insertConfig(SyncConfig loanSyncConfig);

    /**
     * 查询可用的配置
     *
     * @return 可用配置列表
     */
    List<SyncConfig> queryConfig(String type);

    List<SyncConfig> queryConfigByTypeAndTargetType(@Param("type") String type, @Param("targetTypes") List<String> targetTypes);

    SyncConfig queryConfigByConditaion(SyncConfig loanSyncConfig);

    /**
     * 客户sftp账号列表
     *
     * @param apiCode
     * @return
     */
    @AddDataAuth
    List<SyncConfigEditVO> getSftpList(@Param("apiCode") String apiCode, @Param("dataType") Integer dataType);
}