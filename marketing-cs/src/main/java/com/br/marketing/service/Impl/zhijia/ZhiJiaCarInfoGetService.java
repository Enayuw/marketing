package com.br.marketing.service.Impl.zhijia;

import com.br.marketing.common.commondto.Result;
import com.br.marketing.entity.ZhiJiaCarSeriesInfo;
import com.br.marketing.entity.ZhiJiaClueBackData;

/**
 * 获取之家车辆信息接口
 * @author guangxiu.li
 * @date 2024/7/9 11:00
 */
public interface ZhiJiaCarInfoGetService {


    /**
     * 根据sftp信息获取车辆品牌以及车系信息
     * @author guangxiu.li
     * @date 2024/7/9 14:15
     * @param zhiJiaClueBackInfo
     * @return java.util.List<ZhiJiaClueBackInfo>
     */
    Result<ZhiJiaCarSeriesInfo> getZhiJiaCarInfo(ZhiJiaClueBackData zhiJiaClueBackInfo);

}
