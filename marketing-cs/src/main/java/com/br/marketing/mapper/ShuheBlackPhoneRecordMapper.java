package com.br.marketing.mapper;

import com.br.marketing.entity.ShuheBlackPhoneRecord;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ShuheBlackPhoneRecordMapper extends ShuheBlackPhoneRecordMapperBase{

    /**
     * 手机号是否重复
     * @param phone
     * @param pushDate
     */
    int countByPhoneAndDate(@Param("phone")String phone, @Param("pushDate")String pushDate);


    void saveBatch(@Param("list") List<ShuheBlackPhoneRecord> list);
}
