package com.br.marketing.mapper;

import com.br.marketing.entity.DidiCallBackData;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Set;

public interface DidiCallBackDataMapper extends DidiCallBackDataMapperBase {

    void batchAdd(List<DidiCallBackData> list);

    List<DidiCallBackData> queryDidiCellSuccessData(@Param("pageSize") Integer pageSize,
                                                    @Param("lastId") Long lastId,
                                                    @Param("apiCode") String apiCode);

    List<DidiCallBackData> queryDidiSmsSuccessData(@Param("pageSize") Integer pageSize,
                                                   @Param("lastId") Long lastId,
                                                   @Param("apiCode") String apiCode);

    List<DidiCallBackData> queryDidiCellFailData(@Param("pageSize") Integer pageSize,
                                                    @Param("lastId") Long lastId,
                                                    @Param("apiCode") String apiCode);

    List<DidiCallBackData> queryDidiSmsFailData(@Param("pageSize") Integer pageSize,
                                                   @Param("lastId") Long lastId,
                                                   @Param("apiCode") String apiCode);

    List<DidiCallBackData> queryDidiCellConstructData(@Param("pageSize") Integer pageSize,
                                                      @Param("lastId") Long lastId,
                                                      @Param("apiCode") String apiCode);

    List<DidiCallBackData> queryDidiSmsConstructData(@Param("pageSize") Integer pageSize,
                                                      @Param("lastId") Long lastId,
                                                     @Param("apiCode") String apiCode);

    List<String> queryConstructedData(@Param("cellSet") Set<String> cellSet,
                                      @Param("pushType") Integer pushType);


    void updateStatusByIds(@Param("ids") List<Long> ids, @Param("status") Integer status);

    void updateStatusByCells(@Param("status") Integer status, @Param("custNums") List<String> custNums);

    List<String> selectPushedCells(@Param("cells") Set<String> cells, @Param("apiCode") String apiCode);

}