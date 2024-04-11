package com.br.marketing.mapper;

import com.br.marketing.entity.ShuheBlackPhoneRecord;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Set;

public interface ShuheBlackPhoneRecordMapper extends ShuheBlackPhoneRecordMapperBase{

    /**
     * 手机号是否重复
     *
     * @param phone
     * @param pushDate
     */
    int countByPhoneAndDate(@Param("phone") String phone, @Param("pushDate") String pushDate);


    void saveBatch(@Param("list") List<ShuheBlackPhoneRecord> list);

    Set<String> fendCellByCell(@Param("cellSet") Set<String> cellSet);

    int countTmpNonBlackListByCell(@Param("cell") String cell);
}
