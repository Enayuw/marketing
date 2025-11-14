package com.br.marketing.mapper;


import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface LineAccountDetailNormalMapper extends  LineAccountDetailNormalMapperBase{

    List<Long> selectLineIfExist(@Param("gatewayIds")List<Long> gatewayIds,@Param("groupId")  Long groupId);

}