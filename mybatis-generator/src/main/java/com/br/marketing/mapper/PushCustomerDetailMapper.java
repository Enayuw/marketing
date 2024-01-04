package com.br.marketing.mapper;

import com.br.marketing.entity.PushCustomerDetail;
import com.br.marketing.entity.PushCustomerDetailExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface PushCustomerDetailMapper {
    int countByExample(PushCustomerDetailExample example);

    int deleteByExample(PushCustomerDetailExample example);

    int deleteByPrimaryKey(Long id);

    int insert(PushCustomerDetail record);

    int insertSelective(PushCustomerDetail record);

    List<PushCustomerDetail> selectByExample(PushCustomerDetailExample example);

    PushCustomerDetail selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") PushCustomerDetail record, @Param("example") PushCustomerDetailExample example);

    int updateByExample(@Param("record") PushCustomerDetail record, @Param("example") PushCustomerDetailExample example);

    int updateByPrimaryKeySelective(PushCustomerDetail record);

    int updateByPrimaryKey(PushCustomerDetail record);
}