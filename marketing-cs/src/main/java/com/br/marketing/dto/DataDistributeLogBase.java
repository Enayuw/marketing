package com.br.marketing.dto;


import lombok.Data;

import java.util.List;

@Data
public class DataDistributeLogBase<T> {

    private List<T> data;

    private List<DataJoinLogDTO> detailLogList;

    /**
     * 是否去重
     * 0-不去重；1-去重
     * @return
     */
    Boolean isSole = false;

    /**
     * 1-apiCode,custNum
     * 2-apiCode,cell
     * 3-apiCode,cell,status
     * 4-apiCode,custNum,status
     *
     * @return
     */
    Integer soleField;

    /**
     * 去重日期 0-全范围；1-当天
     * @return
     */
    Integer soleDay = 1;
}
