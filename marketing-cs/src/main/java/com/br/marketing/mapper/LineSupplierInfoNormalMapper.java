package com.br.marketing.mapper;


import org.apache.ibatis.annotations.Param;


public interface LineSupplierInfoNormalMapper extends LineSupplierInfoNormalMapperBase{

    Long selectIdByLineSupplier(@Param("lineSupplier") String lineSupplier);

    void updateOpeStatusById(@Param("lineSupplierId") Long lineSupplierId, @Param("opeStatus") Integer opeStatus);

    Long selectIdByLineSupplierNoOpeStatus(@Param("lineSupplier") String lineSupplier);

}