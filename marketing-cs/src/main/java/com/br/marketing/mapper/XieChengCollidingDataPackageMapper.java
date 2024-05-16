package com.br.marketing.mapper;

import com.br.marketing.vo.xiecheng.XiechengPackageVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface XieChengCollidingDataPackageMapper extends XieChengCollidingDataPackageMapperBase {
    List<XiechengPackageVO> getPackageList();
}