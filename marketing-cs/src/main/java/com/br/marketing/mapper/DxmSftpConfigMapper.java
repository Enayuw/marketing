package com.br.marketing.mapper;


import com.br.marketing.entity.DxmSftpConfig;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface DxmSftpConfigMapper extends DxmSftpConfigMapperBase{

    List<DxmSftpConfig> selectAllEnabled();
}