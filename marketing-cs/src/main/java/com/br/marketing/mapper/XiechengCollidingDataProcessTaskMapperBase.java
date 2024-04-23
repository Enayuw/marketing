package com.br.marketing.mapper;

import com.br.marketing.entity.XiechengCollidingDataProcessTask;
import com.br.marketing.entity.XiechengCollidingDataProcessTaskExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface XiechengCollidingDataProcessTaskMapperBase {
    int countByExample(XiechengCollidingDataProcessTaskExample example);

    int deleteByExample(XiechengCollidingDataProcessTaskExample example);

    int deleteByPrimaryKey(Long id);

    int insert(XiechengCollidingDataProcessTask record);

    int insertSelective(XiechengCollidingDataProcessTask record);

    List<XiechengCollidingDataProcessTask> selectByExample(XiechengCollidingDataProcessTaskExample example);

    XiechengCollidingDataProcessTask selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") XiechengCollidingDataProcessTask record, @Param("example") XiechengCollidingDataProcessTaskExample example);

    int updateByExample(@Param("record") XiechengCollidingDataProcessTask record, @Param("example") XiechengCollidingDataProcessTaskExample example);

    int updateByPrimaryKeySelective(XiechengCollidingDataProcessTask record);

    int updateByPrimaryKey(XiechengCollidingDataProcessTask record);
}