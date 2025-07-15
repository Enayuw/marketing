package com.br.marketing.mapper;

import com.br.marketing.entity.BFileBiConfig;
import com.br.marketing.entity.BFileBiConfigExample;
import com.br.marketing.vo.TransFileToBiConfigRecordVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface BFileBiConfigMapper {
    int countByExample(BFileBiConfigExample example);

    int deleteByExample(BFileBiConfigExample example);

    int deleteByPrimaryKey(Long id);

    int insert(BFileBiConfig record);

    int insertSelective(BFileBiConfig record);

    List<BFileBiConfig> selectByExampleWithBLOBs(BFileBiConfigExample example);

    List<BFileBiConfig> selectByExample(BFileBiConfigExample example);

    BFileBiConfig selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") BFileBiConfig record, @Param("example") BFileBiConfigExample example);

    int updateByExampleWithBLOBs(@Param("record") BFileBiConfig record, @Param("example") BFileBiConfigExample example);

    int updateByExample(@Param("record") BFileBiConfig record, @Param("example") BFileBiConfigExample example);

    int updateByPrimaryKeySelective(BFileBiConfig record);

    int updateByPrimaryKeyWithBLOBs(BFileBiConfig record);

    int updateByPrimaryKey(BFileBiConfig record);

    /**
     * 联合查询b_file_bi_config和b_transfer_file_task，过滤b_nfsfile_bi_record无taskId记录
     */
    TransFileToBiConfigRecordVO selectConfigAndTaskForBiShard(
        @Param("dataDate") String dataDate,
        @Param("priorityStatus") Integer priorityStatus
    );
}