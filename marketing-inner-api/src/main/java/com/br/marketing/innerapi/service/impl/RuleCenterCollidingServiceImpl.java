package com.br.marketing.innerapi.service.impl;

import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.entity.XieChengCollidingDataRobExample;
import com.br.marketing.innerapi.service.RuleCenterCollidingService;
import com.br.marketing.mapper.XieChengCollidingDataLoopCycleMapper;
import com.br.marketing.mapper.XieChengCollidingDataRobMapper;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.vo.xiecheng.XiechengCollidingDataVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class RuleCenterCollidingServiceImpl implements RuleCenterCollidingService {

    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Resource
    private XieChengCollidingDataLoopCycleMapper xieChengCollidingDataLoopCycleMapper;

    @Resource
    XieChengCollidingDataRobMapper robMapper;

    /**
     * 获取撞库结果数据
     *
     * @param apiCode
     * @return List<XiechengCollidingDataVO>
     */
    @Override
    public Result<List<XiechengCollidingDataVO>> getCollidingResultData(String apiCode) {
        List<XiechengCollidingDataVO> xiechengCollidingDataVOList = new ArrayList<>();

        if (!marketingCommonConfig.getXieChengCollidingDataProcessApiCodes().contains(apiCode)) {
            return new Result<>().setCode(ResultCode.PARAM_ERROR.getValue()).setMessage("非撞库的apiCode，请检查");
        }
        //周期数据包
        Map<String, String> xiechengCycleMap = xieChengCollidingDataLoopCycleMapper.selectCycleNumData();
        XiechengCollidingDataVO cycleData = new XiechengCollidingDataVO();
        cycleData.setApiCode(apiCode);
        cycleData.setResultData("True的数据包");
        cycleData.setResultNum(xiechengCycleMap.get("CellNum"));
        cycleData.setUpdateTime(xiechengCycleMap.get("requestBeginTime") + "-" + xiechengCycleMap.get("requestEndTime"));
        xiechengCollidingDataVOList.add(cycleData);
        //周期数据包
        XieChengCollidingDataRobExample robExample = new XieChengCollidingDataRobExample();
        robExample.createCriteria().andIsDeleteEqualTo(0);
        int robCount = robMapper.countByExample(robExample);
        XiechengCollidingDataVO falseData = new XiechengCollidingDataVO();
        cycleData.setApiCode(apiCode);
        cycleData.setResultData("False的数据包");
        cycleData.setResultNum(Integer.toString(robCount));
        cycleData.setUpdateTime("-");
        xiechengCollidingDataVOList.add(falseData);
        return new Result<>().setCode(ResultCode.SUCCESS.getValue()).setDate(xiechengCollidingDataVOList);
    }


}
