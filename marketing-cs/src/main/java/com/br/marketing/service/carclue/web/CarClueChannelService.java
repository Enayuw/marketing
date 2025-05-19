package com.br.marketing.service.carclue.web;

import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.commonentity.PageResultReturn;
import com.br.marketing.dto.CarClueChannelConfigDTO;
import com.br.marketing.dto.CarClueChannelDTO;
import com.br.marketing.entity.auth.MarketingUserDetail;
import com.br.marketing.vo.CarClueChannelConfigVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * @ClassName CarClueChannelService
 * @Description 车线索外采渠道管理
 * @Author kongbx
 * @Date 2025/5/6 10:59
 */
public interface CarClueChannelService {

    PageResultReturn getCarClueChannelList(CarClueChannelDTO request);

    ApiResult<Boolean> checkCleanFile();

    ApiResult<Boolean> updateInitMapping(List<String> scope, MultipartFile multipartFile);

    ApiResult<CarClueChannelConfigVO> getChannelConfig();

    ApiResult<Boolean> updateChannelConfig(CarClueChannelConfigDTO dto, MarketingUserDetail user);


}
