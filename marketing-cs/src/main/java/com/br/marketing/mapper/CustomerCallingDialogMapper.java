package com.br.marketing.mapper;

import com.br.marketing.entity.CustomerCallingDialog;
import com.br.marketing.entity.CustomerCallingDialogExample;
import com.br.marketing.vo.HaloCallingDataVo;
import com.br.marketing.vo.HaloCallingLocalFileDataVo;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface CustomerCallingDialogMapper {
    int countByExample(CustomerCallingDialogExample example);

    int deleteByExample(CustomerCallingDialogExample example);

    int deleteByPrimaryKey(Long id);

    int insert(CustomerCallingDialog record);

    int insertSelective(CustomerCallingDialog record);

    List<CustomerCallingDialog> selectByExampleWithBLOBs(CustomerCallingDialogExample example);

    List<CustomerCallingDialog> selectByExample(CustomerCallingDialogExample example);

    CustomerCallingDialog selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") CustomerCallingDialog record, @Param("example") CustomerCallingDialogExample example);

    int updateByExampleWithBLOBs(@Param("record") CustomerCallingDialog record, @Param("example") CustomerCallingDialogExample example);

    int updateByExample(@Param("record") CustomerCallingDialog record, @Param("example") CustomerCallingDialogExample example);

    int updateByPrimaryKeySelective(CustomerCallingDialog record);

    int updateByPrimaryKeyWithBLOBs(CustomerCallingDialog record);

    int updateByPrimaryKey(CustomerCallingDialog record);

    List<HaloCallingDataVo> getInfoByColumns(Map<String, Object> cusMap);

    /**
     * 根据条件查询数据总量
     * @param cusMap
     * @return
     */
    int getHaloCallingCount(Map<String, Object> cusMap);

    List<Map<String, Object>> getRequestId();

    HaloCallingLocalFileDataVo getNewOne();
}