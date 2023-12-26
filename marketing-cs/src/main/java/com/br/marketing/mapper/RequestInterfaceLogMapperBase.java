package com.br.marketing.mapper;

import com.br.marketing.entity.RequestInterfaceLog;
import com.br.marketing.entity.RequestInterfaceLogExample;
import com.br.marketing.entity.RequestInterfaceLogWithBlobs;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface RequestInterfaceLogMapperBase{
    int countByExample(RequestInterfaceLogExample example);

    int deleteByExample(RequestInterfaceLogExample example);

    int deleteByPrimaryKey(Long id);

    int insert(RequestInterfaceLogWithBlobs record);

    int insertSelective(RequestInterfaceLogWithBlobs record);

    List<RequestInterfaceLogWithBlobs> selectByExampleWithBLOBs(RequestInterfaceLogExample example);

    List<RequestInterfaceLog> selectByExample(RequestInterfaceLogExample example);

    RequestInterfaceLogWithBlobs selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") RequestInterfaceLogWithBlobs record, @Param("example") RequestInterfaceLogExample example);

    int updateByExampleWithBLOBs(@Param("record") RequestInterfaceLogWithBlobs record, @Param("example") RequestInterfaceLogExample example);

    int updateByExample(@Param("record") RequestInterfaceLog record, @Param("example") RequestInterfaceLogExample example);

    int updateByPrimaryKeySelective(RequestInterfaceLogWithBlobs record);

    int updateByPrimaryKeyWithBLOBs(RequestInterfaceLogWithBlobs record);

    int updateByPrimaryKey(RequestInterfaceLog record);
}