package com.br.marketing.service;

import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.commonentity.PageResultReturn;
import com.br.marketing.dto.CarClueReportDTO;
import com.br.marketing.vo.CarClueInfoVo;
import com.br.marketing.vo.MarketingCustomerVO;
import com.br.marketing.vo.SyncConfigEditVO;

import java.util.List;

/**
 * 车线索列表
 * return null
 * @author guangxiu.li
 * @date 2025/1/14
 * @description
 */
public interface CarClueReportService {

    /**
     * @param request:
     * return PageResultReturn
     * @author guangxiu.li
     * @date 2025/1/14
     * {@link PageResultReturn}
     * @description
     */
    PageResultReturn getReportList(CarClueReportDTO request);

    /**
     * 编辑车线索信息
     * @param vo
     * @return
     */
    ApiResult<Boolean> editCarClues(List<CarClueInfoVo> voList);

    /**
     * 品牌、车系、城市
     *
     * @param search
     * @return
     */
    List<CarClueInfoVo> getCarInfoLike(String search);

    /**
     * 根据车渠道获取相应apiCode
     * @param key
     * @return
     */
    ApiResult<String> getValueByKey(String key);

}
