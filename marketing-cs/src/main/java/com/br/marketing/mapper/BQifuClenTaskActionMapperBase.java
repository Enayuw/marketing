package com.br.marketing.mapper;

import com.br.marketing.entity.BQifuClenTaskAction;
import com.br.marketing.entity.BQifuClenTaskActionExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface BQifuClenTaskActionMapperBase {
    int countByExample(BQifuClenTaskActionExample example);

    int deleteByExample(BQifuClenTaskActionExample example);

    int deleteByPrimaryKey(Long id);

    int insert(BQifuClenTaskAction record);

    int insertSelective(BQifuClenTaskAction record);

    List<BQifuClenTaskAction> selectByExample(BQifuClenTaskActionExample example);

    BQifuClenTaskAction selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") BQifuClenTaskAction record, @Param("example") BQifuClenTaskActionExample example);

    int updateByExample(@Param("record") BQifuClenTaskAction record, @Param("example") BQifuClenTaskActionExample example);

    int updateByPrimaryKeySelective(BQifuClenTaskAction record);

    int updateByPrimaryKey(BQifuClenTaskAction record);
}