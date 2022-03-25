package com.br.marketing.mapper;


import com.br.marketing.vo.CustomerPushLogVO;
import com.br.marketing.vo.RulePushLogOfStatusVO;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface CustomerInfoPushLogMapper extends CustomerInfoPushLogMapperBase{
    List<CustomerPushLogVO> getPushLog(@Param("mId") Long mId,@Param("realStatusList") List<String> realStatusList);

    List<RulePushLogOfStatusVO> selectRealStatusByMid(@Param("mIds") List<Long> mIds);
}