package com.br.marketing.mapper;

import com.br.marketing.entity.TaikangDingDingTransferDetail;
import com.br.marketing.entity.TaikangDingDingTransferDetailExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface TaikangDingDingTransferDetailMapperBase {
    int countByExample(TaikangDingDingTransferDetailExample example);

    int deleteByExample(TaikangDingDingTransferDetailExample example);

    int deleteByPrimaryKey(Long id);

    int insert(TaikangDingDingTransferDetail record);

    int insertSelective(TaikangDingDingTransferDetail record);

    List<TaikangDingDingTransferDetail> selectByExample(TaikangDingDingTransferDetailExample example);

    TaikangDingDingTransferDetail selectByPrimaryKey(Long id);

    int updateByExampleSelective(
            @Param("record") TaikangDingDingTransferDetail record, @Param("example") TaikangDingDingTransferDetailExample example);

    int updateByExample(
            @Param("record") TaikangDingDingTransferDetail record, @Param("example") TaikangDingDingTransferDetailExample example);

    int updateByPrimaryKeySelective(TaikangDingDingTransferDetail record);

    int updateByPrimaryKey(TaikangDingDingTransferDetail record);
}