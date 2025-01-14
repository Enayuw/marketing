package com.br.marketing.service.Impl;


import cn.hutool.core.util.ObjectUtil;
import com.br.common.util.DateUtils;
import com.br.marketing.commonentity.PageResultReturn;
import com.br.marketing.dto.CarClueReportDTO;
import com.br.marketing.entity.*;
import com.br.marketing.mapper.*;
import com.br.marketing.service.*;
import com.br.marketing.vo.CarClueInfoVo;
import com.br.marketing.vo.MarketingSyncReportVO;
import com.github.pagehelper.PageHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

/**
 * 车线索列表
 * @author guangxiu.li
 * @date 2025/1/14
 * @description
 */
@Service
@Slf4j
public class CarClueReportServiceImpl implements CarClueReportService {

    @Resource
    CarClueInfoMapper carClueInfoMapper;

    @Resource
    private MarketingSyncUserMapper marketingSyncUserMapper;

    @Override
    public PageResultReturn getReportList(CarClueReportDTO request) {
        Integer current = request.getCurrent();
        Integer size = request.getSize();
        // 数据入库状态
        Integer status = request.getStatus();

        Map params = new HashMap();
        params.put("createTimeStart", request.getCreateTimeStart());
        params.put("createTimeEnd", request.getCreateTimeEnd());
        params.put("intention", request.getIntention());
        params.put("clueDataStatus", request.getClueDataStatus());
        params.put("updateTimeStart", request.getUpdateTimeStart());
        params.put("updateTimeEnd", request.getUpdateTimeEnd());
        params.put("cluePushChannel", request.getCluePushChannel());
        params.put("cluePushStatus", request.getCluePushStatus());
        params.put("pushTimeStart", request.getPushTimeStart());
        params.put("pushTimeEnd", request.getPushTimeEnd());
        params.put("callBackTimeStart", request.getCallBackTimeStart());
        params.put("callBackTimeEnd", request.getCallBackTimeEnd());

        PageHelper.startPage(current, size);
        List<CarClueInfoVo> list = carClueInfoMapper.selectList(params);
        for (CarClueInfoVo carClueInfo : list) {
            String apiCode = carClueInfo.getApiCode();
            String custNum = carClueInfo.getCustNum();
            String appletDate = carClueInfo.getAppletDate();
            // 匹配入库状态

        }

        return PageResultReturn.setPageResult(list, current, size);
    }
}
