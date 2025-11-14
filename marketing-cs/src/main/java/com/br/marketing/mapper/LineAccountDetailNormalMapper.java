package com.br.marketing.mapper;


import com.br.marketing.dto.LineAccountDetailDbDTO;
import org.apache.ibatis.annotations.Param;

import java.sql.Date;
import java.util.List;

public interface LineAccountDetailNormalMapper extends  LineAccountDetailNormalMapperBase{

    List<Long> selectLineIfExist(@Param("gatewayIds")List<Long> gatewayIds,@Param("groupId")  Long groupId);

    Long selectTotalCount(@Param("nowDate") Date nowDate);

    List<LineAccountDetailDbDTO> selectList(
            @Param("lineSupplierId") Long lineSupplierId,
            @Param("gatewayId") Long gatewayId,
            @Param("price") Double price,
            @Param("nowDate") Date nowDate,
            @Param("limitSize") Integer limitSize,
            @Param("offset") Integer offset
    );

    List<LineAccountDetailDbDTO> selectListByGroupId(@Param("groupId") Long groupId);
}