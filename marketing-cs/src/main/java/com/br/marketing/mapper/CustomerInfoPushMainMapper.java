package com.br.marketing.mapper;

import com.br.marketing.dto.RequestPushInfoDTO;
import com.br.marketing.vo.PushInfoDetailVO;
import com.br.marketing.vo.PushInfoListVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface CustomerInfoPushMainMapper extends CustomerInfoPushMainMapperBase {

    List<PushInfoDetailVO> getPushInfos(RequestPushInfoDTO dto);

    List<PushInfoListVO> getPushInfoList(@Param("mApiCode") String mApiCode, @Param("pushBeginTime") String pushBeginTime,
                                         @Param("pushEndTime") String pushEndTime, @Param("pushInfoId") String pushInfoId,
                                         @Param("mStatus") Integer mStatus);
}