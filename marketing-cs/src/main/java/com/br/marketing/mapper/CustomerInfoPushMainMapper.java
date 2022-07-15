package com.br.marketing.mapper;

import com.br.marketing.dto.PushInfoFilterDTO;
import com.br.marketing.dto.RequestPushInfoDTO;
import com.br.marketing.mysqlInterceptor.AddDataAuth;
import com.br.marketing.vo.PushInfoDetailVO;
import com.br.marketing.vo.PushInfoListVO;

import java.util.List;

public interface CustomerInfoPushMainMapper extends CustomerInfoPushMainMapperBase {

    List<PushInfoDetailVO> getPushInfos(RequestPushInfoDTO dto);

    @AddDataAuth
    //TODO sql语句是mApicode，需要修改
    List<PushInfoListVO> getPushInfoList(PushInfoFilterDTO dto);
}