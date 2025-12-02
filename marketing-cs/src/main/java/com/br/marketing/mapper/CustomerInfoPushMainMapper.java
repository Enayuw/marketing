package com.br.marketing.mapper;

import com.br.marketing.dto.PushInfoFilterDTO;
import com.br.marketing.dto.RequestPushInfoDTO;
import com.br.marketing.mysqlInterceptor.AddDataAuth;
import com.br.marketing.vo.PushInfoDetailVO;
import com.br.marketing.vo.PushInfoListVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface CustomerInfoPushMainMapper extends CustomerInfoPushMainMapperBase {

    List<PushInfoDetailVO> getPushInfos(RequestPushInfoDTO dto);

    List<PushInfoListVO> getPushInfoList(@Param("dto") PushInfoFilterDTO dto, @Param("pushTargets") List<Integer> pushTargets);
}