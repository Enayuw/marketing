package com.br.marketing.dto;

import com.br.marketing.common.annoation.DistributeLog;
import com.br.marketing.entity.DataDistributeDetailLog;
import lombok.Data;

import java.util.List;

@Data
public class DataDistributeLogBase<T extends DataDistributeBase> {

    private List<T> data;

    private List<DataDistributeDetailLog> detailLogList;

    /**
     * 是否去重
     * 0-不去重；1-去重
     * @return
     */
    Boolean isSole;

    /**
     * 1-apiCode,custNum
     * 2-apiCode,cell
     * @return
     */
    Integer soleField;

    /**
     * 去重日期 0-全范围；1-当天
     * @return
     */
    Integer soleDay;
}
