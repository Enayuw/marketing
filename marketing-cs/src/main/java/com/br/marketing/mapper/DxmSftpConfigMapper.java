package com.br.marketing.mapper;


import com.br.marketing.entity.DxmSftpConfig;

import java.util.List;

public interface DxmSftpConfigMapper extends DxmSftpConfigMapperBase{

    List<DxmSftpConfig> selectAllEnabled();
}