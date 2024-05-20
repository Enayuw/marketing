package com.br.marketing.innerapi.service.impl;

import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.entity.XieChengCollidingDataPackage;
import com.br.marketing.entity.XieChengCollidingDataPackageExample;
import com.br.marketing.entity.XieChengCollidingDataRobExample;
import com.br.marketing.innerapi.service.RuleCenterCollidingService;
import com.br.marketing.mapper.XieChengCollidingDataLoopCycleMapper;
import com.br.marketing.mapper.XieChengCollidingDataPackageMapper;
import com.br.marketing.mapper.XieChengCollidingDataRobMapper;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.vo.xiecheng.XiechengCollidingDataVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class RuleCenterCollidingServiceImpl implements RuleCenterCollidingService {

    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Resource
    private XieChengCollidingDataLoopCycleMapper xieChengCollidingDataLoopCycleMapper;

    @Resource
    XieChengCollidingDataRobMapper robMapper;

    @Resource
    private XieChengCollidingDataPackageMapper packageMapper;

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
            return new Result<>().setCode(ResultCode.SUCCESS.getValue());
        }
        //周期数据包
        Map<String, Object> xiechengCycleMap = xieChengCollidingDataLoopCycleMapper.selectCycleNumData();
        XiechengCollidingDataVO cycleData = new XiechengCollidingDataVO();
        cycleData.setApiCode(apiCode);
        cycleData.setResultData("True的数据包");
        cycleData.setResultNum(xiechengCycleMap.get("CellNum").toString());
        cycleData.setUpdateTime(xiechengCycleMap.get("requestBeginTime") + "-" + xiechengCycleMap.get("requestEndTime"));
        xiechengCollidingDataVOList.add(cycleData);
        //周期数据包
        XieChengCollidingDataPackageExample packageExample = new XieChengCollidingDataPackageExample();
        packageExample.createCriteria().andIsDeleteEqualTo(0);
        List<XieChengCollidingDataPackage> packages = packageMapper.selectByExample(packageExample);
        List<Long> ids = packages.stream().map(XieChengCollidingDataPackage::getId).collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(ids)) {
            XieChengCollidingDataRobExample robExample = new XieChengCollidingDataRobExample();
            robExample.createCriteria().andIsDeleteEqualTo(0).andPackageIdIn(ids);
            int robCount = robMapper.countByExample(robExample);
            XiechengCollidingDataVO falseData = new XiechengCollidingDataVO();
            falseData.setApiCode(apiCode);
            falseData.setResultData("False的数据包");
            falseData.setResultNum(Integer.toString(robCount));
            falseData.setUpdateTime("-");
            xiechengCollidingDataVOList.add(falseData);
        }
        return new Result<>().setCode(ResultCode.SUCCESS.getValue()).setDate(xiechengCollidingDataVOList);
    }


}
