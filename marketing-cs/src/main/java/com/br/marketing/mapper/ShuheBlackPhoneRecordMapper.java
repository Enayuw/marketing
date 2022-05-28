package com.br.marketing.mapper;

import org.apache.ibatis.annotations.Param;

public interface ShuheBlackPhoneRecordMapper extends ShuheBlackPhoneRecordMapperBase{

    /**
     * 手机号是否重复
     * @param phone
     * @param pushDate
     */
    int countByPhoneAndDate(@Param("phone")String phone, @Param("pushDate")String pushDate);


}
