package com.br.marketing.mapper;

import com.br.marketing.dto.RequestPushInfoDTO;
import com.br.marketing.vo.PushInfoDetailVO;

import java.util.List;

public interface CustomerInfoPushMainMapper extends CustomerInfoPushMainMapperBase {

    List<PushInfoDetailVO> getPushInfos(RequestPushInfoDTO dto);
}