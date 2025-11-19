package com.br.marketing.service.Impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.commonentity.PageResultReturn;
import com.br.marketing.dto.LineAccountDetailDTO;
import com.br.marketing.dto.LineBaseFullInfoDTO;
import com.br.marketing.dto.LineBaseShowInfoDto;
import com.br.marketing.dto.account.LineAccountDto;
import com.br.marketing.dto.account.LineCallerDto;
import com.br.marketing.dto.account.PriceDateDTO;
import com.br.marketing.entity.*;
import com.br.marketing.mapper.*;
import com.br.marketing.service.LineSmsAccountDataNormalService;
import com.br.marketing.service.LineSmsAccountNormalService;
import com.br.marketing.vo.LineAccountDetailVO;
import com.br.marketing.vo.LineAccountLogNormalVO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.sql.Date;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class LineSmsAccountNormalServiceImpl implements LineSmsAccountNormalService {

    private static final Logger log = LoggerFactory.getLogger(LineSmsAccountNormalServiceImpl.class);

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Resource
    private LineSmsAccountDataNormalService lineSmsAccountDataNormalService;

    @Resource
    private LineBaseInfoNormalMapper lineBaseInfoNormalMapper;

    @Resource
    private LineAccountDetailNormalMapper lineAccountDetailNormalMapper;

    @Resource
    private LineSupplierInfoNormalMapper    lineSupplierInfoNormalMapper;

    @Resource
    private LineAccountLogNormalMapper lineAccountLogNormalMapper;

    @Override
    public List<LineBaseShowInfoDto> getLineAccountBasInfo() {
        List<LineBaseFullInfoDTO> lineBaseFullInfoDtoList = lineBaseInfoNormalMapper.selectLineBaeFullInfoList();
        List<LineBaseShowInfoDto> lineBaseShowInfoDtoList = lineBaseFullInfoDtoList.stream()
                .collect(Collectors.groupingBy(
                        LineBaseFullInfoDTO::getLineSupplier,
                        Collectors.mapping(this::convertToLineBaseInfo, Collectors.toList())
                ))
                .entrySet().stream()
                .map(entry -> {
                    LineBaseShowInfoDto dto = new LineBaseShowInfoDto();
                    dto.setLineSupplier(entry.getKey());
                    dto.setChannelDTOList(entry.getValue());
                    return dto;
                }).collect(Collectors.toList());

        return lineBaseShowInfoDtoList;
    }



    @Override
    public Result addLineAccount(LineAccountDto dto) throws JsonProcessingException {
        //1.校验线路有无存在的配置
        List<Long> gatewayIds = dto.getLines().stream().map(LineCallerDto::getGatewayId).collect(Collectors.toList());
        List<Long> existGatewayIds = lineAccountDetailNormalMapper.selectLineIfExist(gatewayIds, dto.getGroupId());
        if (existGatewayIds.size() > 0) {
            List<String> callerFullnames = dto.getLines().stream()
                    .filter(line -> existGatewayIds.contains(line.getGatewayId()))
                    .map(LineCallerDto::getCallerFullname).collect(Collectors.toList());
            return new Result<String>().setCode(ResultCode.FAIL.getValue())
                    .setMessage("主叫项目名称：" + String.join(",", callerFullnames) + "已存在配置，无法新增，请在列表页面变更对应主叫项目名称配置！");
        }
        //2.判断日期没有重复
        List<PriceDateDTO> priceDates = dto.getPriceDates();
        long esDateSize = priceDates.stream().map(PriceDateDTO::getEffectStartDate).distinct().count();
        if (esDateSize != priceDates.size()) {
            return new Result<String>().setCode(ResultCode.FAIL.getValue()).setMessage("价格有效期不能重复！");
        }
        //3.校验短信单价
        if (checkPrice(priceDates)) {
            return new Result<String>().setCode(ResultCode.FAIL.getValue()).setMessage("通话单价最大值为1元/分钟！");
        }
        //4.日期排序，从低到高
        priceDates.sort(Comparator.comparing(PriceDateDTO::getEffectStartDate));
        for (int i = 0; i < priceDates.size(); i++) {
            if (i != priceDates.size() - 1) {
                priceDates.get(i).setEffectEndDate(priceDates.get(i + 1).getEffectStartDate().minusDays(1));
            }
        }
        //5.事务保存->要拆分 直接存储程 多个单条的明细
        lineSmsAccountDataNormalService.addLineAccount(dto);
        return new Result<String>().setCode(ResultCode.SUCCESS.getValue());
    }


    @Override
    public Result updLineAccount(LineAccountDto dto) throws JsonProcessingException{
        //1.校验线路有无存在的配置
        List<Long> gatewayIds = dto.getLines().stream().map(LineCallerDto::getGatewayId).collect(Collectors.toList());
        List<Long> existGatewayIds = lineAccountDetailNormalMapper.selectLineIfExist(gatewayIds, dto.getGroupId());
        if (existGatewayIds.size() > 0) {
            List<String> callerFullnames = dto.getLines().stream()
                    .filter(line -> existGatewayIds.contains(line.getGatewayId()))
                    .map(LineCallerDto::getCallerFullname).collect(Collectors.toList());
            return new Result<String>().setCode(ResultCode.FAIL.getValue())
                    .setMessage("主叫项目名称：" + String.join(",", callerFullnames) + "已存在配置，无法变更，请在列表页面变更对应主叫项目名称配置！");
        }
        //2.判断日期没有重复
        List<PriceDateDTO> priceDates = dto.getPriceDates();
        long esDateSize = priceDates.stream().map(PriceDateDTO::getEffectStartDate).distinct().count();
        if (esDateSize != priceDates.size()) {
            return new Result<String>().setCode(ResultCode.FAIL.getValue()).setMessage("价格有效期不能重复！");
        }
        //3.校验短信单价
        if (checkPrice(priceDates)) {
            return new Result<String>().setCode(ResultCode.FAIL.getValue()).setMessage("通话单价最大值为1元/分钟！");
        }
        //4.日期排序，从低到高
        priceDates.sort(Comparator.comparing(PriceDateDTO::getEffectStartDate));
        for (int i = 0; i < priceDates.size(); i++) {
            if (i != priceDates.size() - 1) {
                priceDates.get(i).setEffectEndDate(priceDates.get(i + 1).getEffectStartDate().minusDays(1));
            }
        }

        //5.校验供应商是否变更，数据是否需要更新
        LineAccountLogNormalExample lineLogExample = new LineAccountLogNormalExample();
        lineLogExample.createCriteria().andGroupIdEqualTo(dto.getGroupId()).andIsDeleteEqualTo(0);
        lineLogExample.setOrderByClause("create_time desc limit 1");
        LineAccountLogNormal oldLineLogNormal = lineAccountLogNormalMapper.selectByExample(lineLogExample).get(0);
        JSONObject oldAccountLogDetail = JSONObject.parseObject(oldLineLogNormal.getDetail());
        List<Long> oldGatewayIds = JSON.parseArray(oldAccountLogDetail.getString("gatewayIds"), Long.class);
        boolean lineEqualFlag = new HashSet<>(oldGatewayIds).equals(new HashSet<>(gatewayIds));
        List<PriceDateDTO> oldPriceDates = JSON.parseArray(oldAccountLogDetail.getString("priceDates"), PriceDateDTO.class);
        boolean priceDateEqualFlag = new HashSet<>(oldPriceDates).equals(new HashSet<>(priceDates));
        if(lineEqualFlag && priceDateEqualFlag){
            return new Result<String>().setCode(ResultCode.FAIL.getValue()).setMessage("配置无修改，无需变更");
        }

        //6.事务保存
        lineSmsAccountDataNormalService.updLineAccount(dto);
        return new Result<String>().setCode(ResultCode.SUCCESS.getValue());
    }


    @Override
    public PageResultReturn getLineAccounts(Integer current, Integer size, String lineSupplier, String callerFullName, Double price) {
        Date nowDate = new Date(System.currentTimeMillis());
        Long lineSupplierId = lineSupplierInfoNormalMapper.selectIdByLineSupplierNoOpeStatus(lineSupplier);
        //TODO 相同的lineSupplier,是否存在projectName + caller 相同的多条gatewayId记录
        // (场景不会，但理论绝对值会,此处查询idList做兼容 若没有配置 后续过滤及分组会过滤调)
        List<Long> gatewayIdList = new ArrayList<>();
        if (StringUtils.isNotEmpty(callerFullName)) {
            int lastDash = callerFullName.lastIndexOf('-');
            gatewayIdList = lineBaseInfoNormalMapper.selectGatewayIdByFiled(
                    lineSupplierId,
                    callerFullName.substring(0, lastDash),
                    callerFullName.substring(lastDash + 1)
            );
        }
        Long totalCount = lineAccountDetailNormalMapper.selectTotalCount(lineSupplierId,gatewayIdList,price,nowDate);
        List<LineAccountDetailDTO> detailDbDtoList = lineAccountDetailNormalMapper.selectList(lineSupplierId,
                gatewayIdList,price,nowDate,size,Math.max((current - 1) * size, 0));
        return PageResultReturn.setPageResult(converToShowVOList(detailDbDtoList), current, size, totalCount);
    }

    @Override
    public List<LineAccountDetailVO> getLineAccountsByGroupId(Long groupId) {
        List<LineAccountDetailDTO> detailDbDtoList = lineAccountDetailNormalMapper.selectListByGroupId(groupId);
        return converToShowVOList(detailDbDtoList);
    }

    @Override
    public PageResultReturn getLineAccountLogs(Integer current, Integer size, Long groupId){
        PageHelper.startPage(current, size);
        List<LineAccountLogNormal> lineDbLogList = lineAccountLogNormalMapper.getLineAccountLogs(groupId);
        Page<LineAccountLogNormal> page = (Page<LineAccountLogNormal>) lineDbLogList;
        List<LineAccountLogNormalVO> voList = convertToLineAccountLogVoList(lineDbLogList);
        return PageResultReturn.setPageResult(voList, page.getPageNum(), page.getPageSize(), page.getTotal());
    }

    @Override
    public Result forbLineAccount(Long groupId) {
        lineSmsAccountDataNormalService.forbLineAccount(groupId);
        return new Result<String>().setCode(ResultCode.SUCCESS.getValue());
    }

    @Override
    public Result allowLineAccount(Long groupId) {
        lineSmsAccountDataNormalService.allowLineAccount(groupId);
        return new Result<String>().setCode(ResultCode.SUCCESS.getValue());
    }




    /**
     * //detailDbDtoList -> showDtoList
     * @param detailDbDtoList
     * @return
     */
    private List<LineAccountDetailVO> converToShowVOList(List<LineAccountDetailDTO> detailDbDtoList) {
        List<LineAccountDetailVO> detailShowDTOList = new ArrayList<>();
        detailDbDtoList.forEach(dto -> {
            LineAccountDetailVO showDTOItem = new LineAccountDetailVO();
            BeanUtils.copyProperties(dto, showDTOItem);
            LineSupplierInfoNormal lineSupplierItem = lineSupplierInfoNormalMapper.selectByPrimaryKey(dto.getLineSupplierId());
            showDTOItem.setLineSupplier(lineSupplierItem.getLineSupplier());

            JSONArray linesInfo = new JSONArray();
            List<Long> gatewayIdList = Arrays.stream(dto.getGatewayIds().split(","))
                    .map(String::trim)
                    .map(Long::valueOf)
                    .collect(Collectors.toList());
            List<LineBaseInfoNormal> baseInfoNormalList = lineBaseInfoNormalMapper.selectByIdList(gatewayIdList);
            baseInfoNormalList.forEach(baseItem -> {
                JSONObject lineObj = new JSONObject();
                lineObj.put("gatewayId", baseItem.getGatewayId());
                lineObj.put("callerFullname", baseItem.getProjectName()+"-"+baseItem.getCaller());
                linesInfo.add(lineObj);
            });
            showDTOItem.setLinesInfo(linesInfo.toJSONString());
            detailShowDTOList.add(showDTOItem);
        });
        return detailShowDTOList;
    }

    /**
     * lineDbLogList -> logVoList
     * @param lineDbLogList
     * @return
     */
    private List<LineAccountLogNormalVO> convertToLineAccountLogVoList(List<LineAccountLogNormal> lineDbLogList) {
        List<LineAccountLogNormalVO> voList = new ArrayList<>();
        lineDbLogList.forEach(dbDto -> {
            LineAccountLogNormalVO vo = new LineAccountLogNormalVO();
            vo.setId(dbDto.getId());
            vo.setGroupId(dbDto.getGroupId().toString());

            LineSupplierInfoNormal lineSupplierInfoNormal = lineSupplierInfoNormalMapper.selectByPrimaryKey(dbDto.getLineSupplierId());
            vo.setLineSupplier(lineSupplierInfoNormal.getLineSupplier());

            JSONObject dbLogDetailObj = JSONObject.parseObject(dbDto.getDetail());
            String gatewayIdsStr = dbLogDetailObj.getString("gatewayIds");
            List<Long> gatewayIdList = JSON.parseArray(gatewayIdsStr, Long.class);
            List<LineBaseInfoNormal> baseInfoNormalList = lineBaseInfoNormalMapper.selectByIdList(gatewayIdList);
            List<String> callerFullnames = baseInfoNormalList.stream()
                    .map(baseInfo -> baseInfo.getProjectName() + "-" + baseInfo.getCaller())
                    .collect(Collectors.toList());
            try {
                dbLogDetailObj.put("callerFullnames", objectMapper.writeValueAsString(callerFullnames));
            } catch (JsonProcessingException e) {
                log.error("JSON序列化失败", e);
                throw new RuntimeException(e);
            }
            vo.setDetail(dbLogDetailObj.toJSONString());
            vo.setUserId(dbDto.getUserId());
            vo.setUserName(dbDto.getUserName());
            vo.setRealName(dbDto.getRealName());
            vo.setOpeType(dbDto.getOpeType());
            vo.setCreateTime(dbDto.getCreateTime());
            vo.setUpdateTime(dbDto.getUpdateTime());
            vo.setIsDelete(dbDto.getIsDelete());
            voList.add(vo);
        });
        return voList;
    }

    /**
     * 将 LineBaseFullInfoDto 转换为 LineBaseInfo
     */
    private LineBaseShowInfoDto.LineBaseInfo convertToLineBaseInfo(LineBaseFullInfoDTO fullInfo) {
        LineBaseShowInfoDto.LineBaseInfo baseInfo = new LineBaseShowInfoDto.LineBaseInfo();
        baseInfo.setGatewayId(fullInfo.getGatewayId());
        baseInfo.setCaller(fullInfo.getCaller());
        baseInfo.setProjectName(fullInfo.getProjectName());
        baseInfo.setOutboundNumber(fullInfo.getOutboundNumber());
        baseInfo.setLineSupplier(fullInfo.getLineSupplier());
        baseInfo.setCallerFullName(fullInfo.getProjectName() + "-" + fullInfo.getCaller());
        return baseInfo;
    }

    private Boolean checkPrice(List<PriceDateDTO> priceDates) {
        return priceDates.stream()
                .anyMatch(priceDate -> priceDate.getPrice() != null && priceDate.getPrice().compareTo(BigDecimal.valueOf(1.0)) > 0);
    }

}
