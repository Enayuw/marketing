package com.br.marketing.service.bi.impl;

import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.commonentity.PageResultReturn;
import com.br.marketing.dto.report.zhongan.ZhongAnControlGroupDTO;
import com.br.marketing.mapper.ZhongAnControlGroupMapper;
import com.br.marketing.service.bi.ZhongAnControlGroupService;
import com.br.marketing.vo.zhongan.ZhongAnCustomInfoVO;
import com.br.marketing.vo.zhongan.param.ControlGroupDTO;
import com.br.marketing.vo.zhongan.param.ZhongAnControlGroupParam;
import com.br.marketing.vo.zhongan.param.ZhongAnCustomInfo;
import com.github.pagehelper.PageHelper;
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
    public Result<List<ZhongAnCustomInfoVO>> getCustomInfoList(ControlGroupDTO dto) {
        if (dto.getSize() == null) {
            dto.setSize(10);
        }
        PageHelper.startPage(dto.getCurrent(), dto.getSize());
        List<ZhongAnCustomInfoVO> customInfoList = zhongAnControlGroupMapper.getCustomInfoListbI_(dto);
        for (ZhongAnCustomInfoVO zhongAnCustomInfoVO : customInfoList){
            if(zhongAnCustomInfoVO.getLoginRate() != null){
                zhongAnCustomInfoVO.setLoginRate(zhongAnCustomInfoVO.getLoginRate().movePointRight(2));
            }
            if(zhongAnCustomInfoVO.getPayPassRate() != null){
                zhongAnCustomInfoVO.setPayPassRate(zhongAnCustomInfoVO.getPayPassRate().movePointRight(2));
            }
        }
        PageResultReturn pageResultReturn = PageResultReturn.setPageResult(customInfoList, dto.getCurrent(), dto.getSize());
        return new Result<>().setCode(ResultCode.SUCCESS.getValue()).setDate(pageResultReturn);
    }

    @Override
    public Result<Long> saveCustomInfo(ZhongAnControlGroupParam param) {
        if(param == null){
            return new Result().setCode(ResultCode.FAIL.getValue()).setMessage("众安对照组配置入参为空！");
        }
        zhongAnControlGroupMapper.saveCustomInfobI_(buildCustomInfo(param));
        return new Result<Long>().setCode(ResultCode.SUCCESS.getValue()).setDate(param.getReportDate());
    }

    @Override
    public Result<List<String>> getConfigStatus(String startDate, String endDate) {
        if(StringUtils.isEmpty(startDate) || StringUtils.isEmpty(endDate)){
            return new Result().setCode(ResultCode.FAIL.getValue()).setMessage("查询日期为空！");
        }
        List<String> strings = zhongAnControlGroupMapper.selectConfigStatusbI_(startDate, endDate);
        return new Result<Long>().setCode(ResultCode.SUCCESS.getValue()).setDate(strings);
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
                zhongAnControlGroupDTO.setLoginRate(new BigDecimal(zhongAnCustomInfo.getLoginRate()).movePointLeft(2));
            }
            if(zhongAnCustomInfo.getPayPassRate() != null){
                zhongAnControlGroupDTO.setPayPassRate(new BigDecimal(zhongAnCustomInfo.getPayPassRate()).movePointLeft(2));
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
