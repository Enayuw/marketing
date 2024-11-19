package com.br.marketing.mapper;

import com.br.marketing.entity.XiechengCollidingTaskBatch;
import com.br.marketing.entity.XiechengCollidingTaskBatchExample;
import com.br.marketing.vo.XiechengCollidingTaskBatchVo;
import org.apache.ibatis.annotations.Param;
import java.util.Date;
import java.util.List;

public interface XiechengCollidingTaskBatchMapper {
    int countByExample(XiechengCollidingTaskBatchExample example);

    int deleteByExample(XiechengCollidingTaskBatchExample example);

    int deleteByPrimaryKey(Long id);

    int insert(XiechengCollidingTaskBatch record);

    int insertSelective(XiechengCollidingTaskBatch record);

    List<XiechengCollidingTaskBatch> selectByExample(XiechengCollidingTaskBatchExample example);

    XiechengCollidingTaskBatch selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") XiechengCollidingTaskBatch record, @Param("example") XiechengCollidingTaskBatchExample example);

    int updateByExample(@Param("record") XiechengCollidingTaskBatch record, @Param("example") XiechengCollidingTaskBatchExample example);

    int updateByPrimaryKeySelective(XiechengCollidingTaskBatch record);

    int updateByPrimaryKey(XiechengCollidingTaskBatch record);

    XiechengCollidingTaskBatchVo selectEarliestBatch(@Param("apiCode") String apiCode, @Param("nowDate") Date nowDate, @Param("type") Integer type);
}