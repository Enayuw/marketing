package com.br.marketing.mapper;

import com.br.marketing.entity.ZhongYuanTransfer;
import com.br.marketing.entity.ZhongYuanTransferExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ZhongYuanTransferMapperBase {
    int countByExample(ZhongYuanTransferExample example);

    int deleteByExample(ZhongYuanTransferExample example);

    int deleteByPrimaryKey(Long id);

    int insert(ZhongYuanTransfer record);

    int insertSelective(ZhongYuanTransfer record);

    List<ZhongYuanTransfer> selectByExample(ZhongYuanTransferExample example);

    ZhongYuanTransfer selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") ZhongYuanTransfer record, @Param("example") ZhongYuanTransferExample example);

    int updateByExample(@Param("record") ZhongYuanTransfer record, @Param("example") ZhongYuanTransferExample example);

    int updateByPrimaryKeySelective(ZhongYuanTransfer record);

    int updateByPrimaryKey(ZhongYuanTransfer record);
}