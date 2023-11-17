package com.br.marketing.mapper;

import com.br.marketing.entity.ZhongbangCaifuData;
import com.br.marketing.entity.ZhongbangCaifuDataExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface ZhongbangCaifuDataBaseMapper {
    long countByExample(ZhongbangCaifuDataExample example);

    int deleteByExample(ZhongbangCaifuDataExample example);

    int deleteByPrimaryKey(Long id);

    int insert(ZhongbangCaifuData record);

    int insertSelective(ZhongbangCaifuData record);

    List<ZhongbangCaifuData> selectByExample(ZhongbangCaifuDataExample example);

    ZhongbangCaifuData selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") ZhongbangCaifuData record, @Param("example") ZhongbangCaifuDataExample example);

    int updateByExample(@Param("record") ZhongbangCaifuData record, @Param("example") ZhongbangCaifuDataExample example);

    int updateByPrimaryKeySelective(ZhongbangCaifuData record);

    int updateByPrimaryKey(ZhongbangCaifuData record);
}