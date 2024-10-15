package com.br.marketing.service.bi;

import com.br.marketing.common.commondto.Result;
import com.br.marketing.vo.zhongan.ZhongAnCustomInfoVO;
import com.br.marketing.vo.zhongan.param.ControlGroupDTO;
import com.br.marketing.vo.zhongan.param.ZhongAnControlGroupParam;

import java.util.List;

/**
 * @ClassName ZhongAnControlGroupService
 * @Description TODO
 * @Author kongbx
 * @Date 2024/9/18 13:37
 */
public interface ZhongAnControlGroupService {

    Result<List<ZhongAnCustomInfoVO>> getCustomInfoList(ControlGroupDTO controlGroupDTO);

    Result<Long> saveCustomInfo(ZhongAnControlGroupParam param);

    Result<List<String>> getConfigStatus(String startDate, String endDate);
}
