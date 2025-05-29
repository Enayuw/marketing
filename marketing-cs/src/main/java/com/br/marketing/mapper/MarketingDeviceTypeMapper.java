package com.br.marketing.mapper;

import com.br.marketing.client.yunke.output.ChildDataZDto;
import com.br.marketing.dto.LogEncryptionCellsDto;
import com.br.marketing.entity.MarketingDeviceType;
import com.br.marketing.entity.MarketingDeviceTypeExample;
import com.br.marketing.vo.yunke.DeviceTypeVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface MarketingDeviceTypeMapper {
    int countByExample(MarketingDeviceTypeExample example);

    int deleteByExample(MarketingDeviceTypeExample example);

    int deleteByPrimaryKey(Long id);

    int insert(MarketingDeviceType record);

    int insertSelective(MarketingDeviceType record);

    List<MarketingDeviceType> selectByExample(MarketingDeviceTypeExample example);

    MarketingDeviceType selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") MarketingDeviceType record, @Param("example") MarketingDeviceTypeExample example);

    int updateByExample(@Param("record") MarketingDeviceType record, @Param("example") MarketingDeviceTypeExample example);

    int updateByPrimaryKeySelective(MarketingDeviceType record);

    int updateByPrimaryKey(MarketingDeviceType record);

    void saveBatch(@Param("list") List<MarketingDeviceType> list);

    List<MarketingDeviceType> selectPageByDeviceType(@Param("pageSize") Integer pageSize);

    List<MarketingDeviceType> selectPageByCodeMaxId(@Param("pageSize") Integer pageSize, @Param("apiCode") String apiCode,
                                       @Param("maxId") Long maxId);

    void updateDeviceTypeBySha1(@Param("checkElement") String checkElement, @Param("parseState") Integer parseState);

    List<DeviceTypeVO> getDeviceTypesByLogCells(@Param("list") List<LogEncryptionCellsDto> list);

}