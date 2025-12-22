package com.br.marketing.mapper;

import com.br.marketing.entity.DidiCallBackData;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface DidiCallBackDataMapper extends DidiCallBackDataMapperBase {

    void batchAdd(List<DidiCallBackData> list);

    List<DidiCallBackData> queryDidiCellSuccessData(@Param("pageSize") Integer pageSize,
                                                    @Param("lastId") Long lastId);

    List<DidiCallBackData> queryDidiSmsSuccessData(@Param("pageSize") Integer pageSize,
                                                   @Param("lastId") Long lastId);

    List<DidiCallBackData> queryDidiCellConstructData(@Param("pageSize") Integer pageSize,
                                                      @Param("lastId") Long lastId);

    List<DidiCallBackData> queryDidiSmsConstructData(@Param("pageSize") Integer pageSize,
                                                      @Param("lastId") Long lastId);


    void updateStatusByIds(@Param("ids") List<Long> ids, @Param("status") Integer status, @Param("pushStatus") Integer pushStatus);
}