package com.br.marketing.mapper;


public interface FlagDataMapper extends FlagDataMapperBase{
    int updateTaskIdByLocalId(Long localId);

    int updateByDynamicEncCell(String cellMd5, String cellSha256, String cellLog, String apiCode, String encType);
}