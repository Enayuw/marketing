package com.br.marketing.service.bi.impl;

import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.dto.report.zhongan.ZhongAnControlGroupDTO;
import com.br.marketing.mapper.ZhongAnControlGroupMapper;
import com.br.marketing.service.bi.ZhongAnControlGroupService;
import com.br.marketing.vo.zhongan.ZhongAnCustomInfoVO;
import com.br.marketing.vo.zhongan.param.ZhongAnControlGroupParam;
import com.br.marketing.vo.zhongan.param.ZhongAnCustomInfo;
import groovy.util.logging.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName ZhongAnControlGroupServiceImpl
 * @Description TODO
 * @Author kongbx
 * @Date 2024/9/18 13:43
 */
@Service
@Slf4j
public class ZhongAnControlGroupServiceImpl implements ZhongAnControlGroupService {

    @Autowired
    private ZhongAnControlGroupMapper zhongAnControlGroupMapper;

    @Override
    public Result<List<ZhongAnCustomInfoVO>> getCustomInfoList(String reportDate) {
        List<ZhongAnControlGroupDTO> customInfoList = zhongAnControlGroupMapper.getCustomInfoListbI_(reportDate);
        List<ZhongAnCustomInfoVO> list = new ArrayList<>();
        for(ZhongAnControlGroupDTO zhongAnControlGroupDTO : customInfoList){
            ZhongAnCustomInfoVO zhongAnCustomInfoVO = new ZhongAnCustomInfoVO();
            zhongAnCustomInfoVO.setReportDate(zhongAnControlGroupDTO.getReportDate());
            zhongAnCustomInfoVO.setUserType(zhongAnControlGroupDTO.getUserType());
            zhongAnCustomInfoVO.setConstituencies(zhongAnControlGroupDTO.getConstituencies());
            zhongAnCustomInfoVO.setTotalNum(zhongAnControlGroupDTO.getTotalNum());
            zhongAnCustomInfoVO.setIncomingNum(zhongAnControlGroupDTO.getIncomingNum());
            zhongAnCustomInfoVO.setApproversNum(zhongAnControlGroupDTO.getApproversNum());
            list.add(zhongAnCustomInfoVO);
        }
        return new Result<List<ZhongAnCustomInfoVO>>().setCode(ResultCode.SUCCESS.getValue()).setDate(list);
    }

    @Override
    public Result<Long> saveCustomInfo(ZhongAnControlGroupParam param) {
        if(param == null){
            return new Result().setCode(ResultCode.FAIL.getValue()).setMessage("众安对照组配置入参为空！");
        }
        zhongAnControlGroupMapper.saveCustomInfobI_(buildCustomInfo(param));
        return new Result<Long>().setCode(ResultCode.SUCCESS.getValue()).setDate(param.getReportDate());
    }

    private List<ZhongAnControlGroupDTO> buildCustomInfo(ZhongAnControlGroupParam param) {
        List<ZhongAnControlGroupDTO> list = new ArrayList<>();
        List<ZhongAnCustomInfo> userType1 = param.getUserType1();
        List<ZhongAnCustomInfo> userType7 = param.getUserType7();
        List<ZhongAnCustomInfo> userType8 = param.getUserType8();

        for (ZhongAnCustomInfo zhongAnCustomInfo : userType1) {
            ZhongAnControlGroupDTO zhongAnControlGroupDTO = new ZhongAnControlGroupDTO();
            zhongAnControlGroupDTO.setReportDate(param.getReportDate());
            zhongAnControlGroupDTO.setUserType(1);
            zhongAnControlGroupDTO.setConstituencies(zhongAnCustomInfo.getConstituencies());
            zhongAnControlGroupDTO.setTotalNum(zhongAnCustomInfo.getTotalNum());
            zhongAnControlGroupDTO.setIncomingNum(zhongAnCustomInfo.getIncomingNum());
            zhongAnControlGroupDTO.setApproversNum(zhongAnCustomInfo.getApproversNum());
            list.add(zhongAnControlGroupDTO);
        }

        for (ZhongAnCustomInfo zhongAnCustomInfo : userType7) {
            ZhongAnControlGroupDTO zhongAnControlGroupDTO = new ZhongAnControlGroupDTO();
            zhongAnControlGroupDTO.setReportDate(param.getReportDate());
            zhongAnControlGroupDTO.setUserType(7);
            zhongAnControlGroupDTO.setConstituencies(zhongAnCustomInfo.getConstituencies());
            zhongAnControlGroupDTO.setTotalNum(zhongAnCustomInfo.getTotalNum());
            zhongAnControlGroupDTO.setIncomingNum(zhongAnCustomInfo.getIncomingNum());
            zhongAnControlGroupDTO.setApproversNum(zhongAnCustomInfo.getApproversNum());
            zhongAnControlGroupDTO.setApprovalAvailable(zhongAnCustomInfo.getApprovalAvailable());
            if(zhongAnCustomInfo.getLoginRate() != null){
                zhongAnControlGroupDTO.setLoginRate(new BigDecimal(zhongAnCustomInfo.getLoginRate()));
            }
            if(zhongAnCustomInfo.getPayPassRate() != null){
                zhongAnControlGroupDTO.setPayPassRate(new BigDecimal(zhongAnCustomInfo.getPayPassRate()));
            }
            if(zhongAnCustomInfo.getLendersSucAmount() != null){
                zhongAnControlGroupDTO.setLendersSucAmount(new BigDecimal(zhongAnCustomInfo.getLendersSucAmount()));
            }
            zhongAnControlGroupDTO.setApplyPayNum(zhongAnCustomInfo.getApplyPayNum());
            zhongAnControlGroupDTO.setLendersSucNum(zhongAnCustomInfo.getLendersSucNum());
            list.add(zhongAnControlGroupDTO);
        }

        for (ZhongAnCustomInfo zhongAnCustomInfo : userType8) {
            ZhongAnControlGroupDTO zhongAnControlGroupDTO = new ZhongAnControlGroupDTO();
            zhongAnControlGroupDTO.setReportDate(param.getReportDate());
            zhongAnControlGroupDTO.setUserType(8);
            zhongAnControlGroupDTO.setConstituencies(zhongAnCustomInfo.getConstituencies());
            zhongAnControlGroupDTO.setTotalNum(zhongAnCustomInfo.getTotalNum());
            zhongAnControlGroupDTO.setIncomingNum(zhongAnCustomInfo.getIncomingNum());
            zhongAnControlGroupDTO.setApproversNum(zhongAnCustomInfo.getApproversNum());
            list.add(zhongAnControlGroupDTO);
        }
        return list;
    }


}
