package com.br.marketing.service.Impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.commonentity.PageResultReturn;
import com.br.marketing.dto.*;
import com.br.marketing.dto.account.*;
import com.br.marketing.entity.*;
import com.br.marketing.mapper.*;
import com.br.marketing.service.LineSmsAccountDataNormalService;
import com.br.marketing.service.LineSmsAccountNormalService;
import com.br.marketing.vo.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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


    @Resource
    private SmsBaseInfoNormalMapper smsBaseInfoNormalMapper;

    @Resource
    private SmsAccountLogNormalMapper smsAccountLogNormalMapper;

    @Resource
    private SmsVendorInfoNormalMapper smsVendorInfoNormalMapper;

    @Autowired
    private SmsAccountDetailNormalMapper smsAccountDetailNormalMapper;


    @Override
    public List<LineBaseShowInfoDTO> getLineAccountBasInfo() {
        List<LineBaseFullInfoDTO> lineBaseFullInfoDtoList = lineBaseInfoNormalMapper.selectLineBaseFullInfoList();
        List<LineBaseShowInfoDTO> lineBaseShowInfoDtoList = lineBaseFullInfoDtoList.stream()
                .collect(Collectors.groupingBy(
                        LineBaseFullInfoDTO::getLineSupplier,
                        Collectors.mapping(this::convertToLineBaseInfo, Collectors.toList())
                ))
                .entrySet().stream()
                .map(entry -> {
                    LineBaseShowInfoDTO dto = new LineBaseShowInfoDTO();
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

    @Override
    public Result deleteLineAccount(Long groupId) {
        lineSmsAccountDataNormalService.deleteLineAccount(groupId);
        return new Result<String>().setCode(ResultCode.SUCCESS.getValue());
    }


    @Override
    public List<SmsBaseShowInfoDTO>  getSmsAccountBaseInfo() {
        List<SmsBaseShowInfoDTO> smsBaseShowInfoDTOList = new ArrayList<>();
        List<SmsVendorInfoNormal> smsVendorInfoNormalList = smsVendorInfoNormalMapper.selectList();
        List<SmsBaseInfoNormal> smsBaseInfoNormalList = smsBaseInfoNormalMapper.selectList();
        smsVendorInfoNormalList.forEach(vendorInfo -> {
            SmsBaseShowInfoDTO showInfoItem = new SmsBaseShowInfoDTO();
            showInfoItem.setVendorId(vendorInfo.getVendorId());
            showInfoItem.setVendorName(vendorInfo.getVendorName());
            List<SmsBaseShowInfoDTO.SmsBaseInfo> channelDTOList = new ArrayList<>();
            List<SmsBaseInfoNormal> filterList = smsBaseInfoNormalList.stream().filter(
                    baseInfo -> Objects.equals(baseInfo.getVendorId(), vendorInfo.getVendorId())).collect(Collectors.toList());
            if (!filterList.isEmpty()) {
                filterList.forEach(smsBaseItem -> {
                    SmsBaseShowInfoDTO.SmsBaseInfo smsBaseInfo = new SmsBaseShowInfoDTO.SmsBaseInfo();
                    smsBaseInfo.setChannelId(smsBaseItem.getChannelId());
                    smsBaseInfo.setChannelName(smsBaseItem.getChannelName());
                    channelDTOList.add(smsBaseInfo);
                });
            }
            showInfoItem.setChannelDTOList(channelDTOList);
            smsBaseShowInfoDTOList.add(showInfoItem);
        });
        return smsBaseShowInfoDTOList;
    }

    @Override
    public Result addSmsAccount(SmsAccountDto dto) throws JsonProcessingException{
        //1.校验渠道有无存在的配置
        List<Long> channelIds = dto.getChannels().stream().map(SmsChannelDto::getChannelId).collect(Collectors.toList());
        List<Long> existChannelIds = smsAccountDetailNormalMapper.selectChannelIfExist(channelIds, dto.getGroupId());
        if (!existChannelIds.isEmpty()) {
            List<String> existChannelNames = dto.getChannels().stream()
                    .filter(channel -> existChannelIds.contains(channel.getChannelId()))
                    .map(SmsChannelDto::getChannelName).collect(Collectors.toList());
            return new Result<String>().setCode(ResultCode.FAIL.getValue())
                    .setMessage("渠道：" + String.join(",", existChannelNames) + "已存在配置，无法新增，请在列表页面变更对应渠道配置！");
        }
        //2.判断日期没有重复
        List<PriceDateDTO> priceDates = dto.getPriceDates();
        long esDateSize = priceDates.stream().map(PriceDateDTO::getEffectStartDate).distinct().count();
        if (esDateSize != priceDates.size()) {
            return new Result<String>().setCode(ResultCode.FAIL.getValue()).setMessage("价格有效期不能重复！");
        }
        //3.校验短信单价
        if (checkPrice(priceDates)) {
            return new Result<String>().setCode(ResultCode.FAIL.getValue()).setMessage("短信单价最大值为1元！");
        }
        //4.日期排序，从低到高
        priceDates.sort(Comparator.comparing(PriceDateDTO::getEffectStartDate));
        for (int i = 0; i < priceDates.size(); i++) {
            if (i != priceDates.size() - 1) {
                priceDates.get(i).setEffectEndDate(priceDates.get(i + 1).getEffectStartDate().minusDays(1));
            }
        }
        //5.事务保存
        lineSmsAccountDataNormalService.addSmsAccount(dto);
        return new Result<String>().setCode(ResultCode.SUCCESS.getValue());
    }

    @Override
    public Result updSmsAccount(SmsAccountDto dto) throws JsonProcessingException{
        //1.校验渠道有无存在的配置
        List<Long> channelIds = dto.getChannels().stream().map(SmsChannelDto::getChannelId).collect(Collectors.toList());
        List<Long> existChannelIds = smsAccountDetailNormalMapper.selectChannelIfExist(channelIds, dto.getGroupId());
        if (!existChannelIds.isEmpty()) {
            List<String> existChannelNames = dto.getChannels().stream()
                    .filter(channel -> existChannelIds.contains(channel.getChannelId()))
                    .map(SmsChannelDto::getChannelName).collect(Collectors.toList());
            return new Result<String>().setCode(ResultCode.FAIL.getValue())
                    .setMessage("渠道：" + String.join(",", existChannelNames) + "已存在配置，无法变更，请在列表页面变更对应渠道配置！");
        }
        //2.判断日期没有重复
        List<PriceDateDTO> priceDates = dto.getPriceDates();
        long esDateSize = priceDates.stream().map(PriceDateDTO::getEffectStartDate).distinct().count();
        if (esDateSize != priceDates.size()) {
            return new Result<String>().setCode(ResultCode.FAIL.getValue()).setMessage("价格有效期不能重复！");
        }
        //3.校验短信单价
        if (checkPrice(priceDates)) {
            return new Result<String>().setCode(ResultCode.FAIL.getValue()).setMessage("短信单价最大值为1元！");
        }
        //4.日期排序，从低到高
        priceDates.sort(Comparator.comparing(PriceDateDTO::getEffectStartDate));
        for (int i = 0; i < priceDates.size(); i++) {
            if (i != priceDates.size() - 1) {
                priceDates.get(i).setEffectEndDate(priceDates.get(i + 1).getEffectStartDate().minusDays(1));
            }
        }
        //5.校验供应商是否变更，数据是否需要更新
        SmsAccountLogNormalExample smsLogExample = new SmsAccountLogNormalExample();
        smsLogExample.createCriteria().andGroupIdEqualTo(dto.getGroupId()).andIsDeleteEqualTo(0);
        smsLogExample.setOrderByClause("create_time desc limit 1");
        SmsAccountLogNormal oldAccountLog = smsAccountLogNormalMapper.selectByExample(smsLogExample).get(0);
        JSONObject oldAccountLogDetail = JSONObject.parseObject(oldAccountLog.getDetail());
        List<Long> oldChannelIds = JSON.parseArray(oldAccountLogDetail.getString("channelIds"), Long.class);
        boolean channelEqualFlag = new HashSet<>(oldChannelIds).equals(new HashSet<>(channelIds));
        List<PriceDateDTO> oldPriceDates = JSON.parseArray(oldAccountLogDetail.getString("priceDates"), PriceDateDTO.class);
        boolean priceDateEqualFlag = new HashSet<>(oldPriceDates).equals(new HashSet<>(priceDates));
        if(channelEqualFlag && priceDateEqualFlag){
            return new Result<String>().setCode(ResultCode.FAIL.getValue()).setMessage("配置无修改，无需变更");
        }
        //6.事务保存
        lineSmsAccountDataNormalService.updSmsAccount(dto);
        return new Result<String>().setCode(ResultCode.SUCCESS.getValue());
    }

    @Override
    public PageResultReturn getSmsAccounts(Integer current, Integer size, Long vendorId, Long channelId, Double price) {
        Date nowDate = new Date(System.currentTimeMillis());
        Long totalCount = smsAccountDetailNormalMapper.selectTotalCount(vendorId, channelId,price,nowDate);
        List<SmsAccountDetailDTO> detailDbDtoList = smsAccountDetailNormalMapper.selectList(vendorId,
                channelId,price,nowDate,size,Math.max((current - 1) * size, 0));
        return PageResultReturn.setPageResult(converToSmsShowVOList(detailDbDtoList), current, size, totalCount);
    }

    @Override
    public List<SmsAccountDetailVO> getSmsAccountsByGroupId(Long groupId) {
        List<SmsAccountDetailDTO> detailDbDtoList = smsAccountDetailNormalMapper.selectListByGroupId(groupId);
        return converToSmsShowVOList(detailDbDtoList);
    }

    @Override
    public Result forbSmsAccount(Long groupId) {
        lineSmsAccountDataNormalService.forbSmsAccount(groupId);
        return new Result<String>().setCode(ResultCode.SUCCESS.getValue());
    }

    @Override
    public Result allowSmsAccount(Long groupId) {
        lineSmsAccountDataNormalService.allowSmsAccount(groupId);
        return new Result<String>().setCode(ResultCode.SUCCESS.getValue());
    }

    @Override
    public Result deleteSmsAccount(Long groupId) {
        lineSmsAccountDataNormalService.deleteSmsAccount(groupId);
        return new Result<String>().setCode(ResultCode.SUCCESS.getValue());
    }

    @Override
    public PageResultReturn getSmsAccountLogs(Integer current, Integer size, Long groupId) {
        PageHelper.startPage(current, size);
        List<SmsAccountLogNormal> smsDbLogList = smsAccountLogNormalMapper.getLineAccountLogs(groupId);
        Page<SmsAccountLogNormal> page = (Page<SmsAccountLogNormal>) smsDbLogList;
        List<SmsAccountLogNormalVO> voList = convertToSmsAccountLogVoList(smsDbLogList);
        return PageResultReturn.setPageResult(voList, page.getPageNum(), page.getPageSize(), page.getTotal());
    }


    private List<SmsAccountDetailVO> converToSmsShowVOList(List<SmsAccountDetailDTO> detailDbDtoList) {
        List<SmsAccountDetailVO> detailShowDTOList = new ArrayList<>();

        detailDbDtoList.forEach(dto -> {
            SmsAccountDetailVO showDTOItem = new SmsAccountDetailVO();
            showDTOItem.setGroupId(String.valueOf(dto.getGroupId()));
            showDTOItem.setVendorId(dto.getVendorId());
            showDTOItem.setPrice(dto.getPrice());
            showDTOItem.setEffectStartDate(dto.getEffectStartDate());
            showDTOItem.setEffectEndDate(dto.getEffectEndDate());
            showDTOItem.setEnabled(dto.getEnabled());
            showDTOItem.setCreateTime(dto.getCreateTime());
            showDTOItem.setUpdateTime(dto.getUpdateTime());
            showDTOItem.setIsDelete(dto.getIsDelete());

            //vendorName
            SmsVendorInfoNormalExample smsVendorInfoNormalExample = new SmsVendorInfoNormalExample();
            smsVendorInfoNormalExample.createCriteria().andVendorIdEqualTo(dto.getVendorId()).andIsDeleteEqualTo(0);
            smsVendorInfoNormalExample.setOrderByClause("create_time desc limit 1");
            SmsVendorInfoNormal smsVendorInfoNormal = smsVendorInfoNormalMapper.selectByExample(smsVendorInfoNormalExample).get(0);
            showDTOItem.setVendorName(smsVendorInfoNormal.getVendorName());

            //channelsInfo
            JSONArray channelsInfo = new JSONArray();
            List<Long> channelIdList = Arrays.stream(dto.getChannelIds().split(","))
                    .map(String::trim)
                    .map(Long::valueOf)
                    .collect(Collectors.toList());
            List<SmsBaseInfoNormal> baseInfoNormalList = smsBaseInfoNormalMapper.selectByChannelIdListtikv_(channelIdList);

            baseInfoNormalList.forEach(baseItem -> {
                JSONObject lineObj = new JSONObject();
                lineObj.put("channelId", baseItem.getChannelId());
                lineObj.put("channelName", baseItem.getChannelName());
                channelsInfo.add(lineObj);
            });
            showDTOItem.setChannelsInfo(channelsInfo.toJSONString());
            detailShowDTOList.add(showDTOItem);
        });
        return detailShowDTOList;
    }

    /**
     *
     * @param smsDbLogList
     * @return
     * SmsAccountLogNormalVO
     * private Long id;
     * private String groupId;
     * private Long vendorId;
     * private Long vendorName;
     * private String detail;
     * private String userId;
     * private String userName;
     * private String realName;
     * private Integer opeType;
     * private Date createTime;
     * private Date updateTime;
     * private Integer isDelete;
     */
    private List<SmsAccountLogNormalVO> convertToSmsAccountLogVoList(List<SmsAccountLogNormal> smsDbLogList) {
        List<SmsAccountLogNormalVO> smsAccountLogNormalVOList = new ArrayList<>();
        smsDbLogList.forEach(smsDbLog -> {
            SmsAccountLogNormalVO smsAccountLogNormalVO = new SmsAccountLogNormalVO();
            smsAccountLogNormalVO.setId(smsDbLog.getId());
            smsAccountLogNormalVO.setGroupId(smsDbLog.getGroupId().toString());
            smsAccountLogNormalVO.setVendorId(smsDbLog.getVendorId());
            //vendorName
            SmsVendorInfoNormalExample smsVendorInfoNormalExample = new SmsVendorInfoNormalExample();
            smsVendorInfoNormalExample.createCriteria().andVendorIdEqualTo(smsDbLog.getVendorId()).andIsDeleteEqualTo(0);
            smsVendorInfoNormalExample.setOrderByClause("create_time desc limit 1");
            SmsVendorInfoNormal smsVendorInfoNormal = smsVendorInfoNormalMapper.selectByExample(smsVendorInfoNormalExample).get(0);
            smsAccountLogNormalVO.setVendorName(smsVendorInfoNormal.getVendorName());

            //detail
            JSONObject dbLogDetailObj = JSONObject.parseObject(smsDbLog.getDetail());
            String channelIdsStr = dbLogDetailObj.getString("channelIds");
            List<Long> channelIdList = JSON.parseArray(channelIdsStr, Long.class);
            List<SmsBaseInfoNormal> baseInfoNormalList = smsBaseInfoNormalMapper.selectByChannelIdListtikv_(channelIdList);
            List<String> channelNames = baseInfoNormalList.stream()
                    .map(SmsBaseInfoNormal::getChannelName)
                    .collect(Collectors.toList());
            try {
                dbLogDetailObj.put("channelNames", objectMapper.writeValueAsString(channelNames));
            } catch (JsonProcessingException e) {
                log.error("JSON序列化失败", e);
                throw new RuntimeException(e);
            }
            smsAccountLogNormalVO.setDetail(dbLogDetailObj.toJSONString());
            smsAccountLogNormalVO.setUserId(smsDbLog.getUserId());
            smsAccountLogNormalVO.setUserName(smsDbLog.getUserName());
            smsAccountLogNormalVO.setRealName(smsDbLog.getRealName());
            smsAccountLogNormalVO.setOpeType(smsDbLog.getOpeType());
            smsAccountLogNormalVO.setCreateTime(smsDbLog.getCreateTime());
            smsAccountLogNormalVO.setUpdateTime(smsDbLog.getUpdateTime());
            smsAccountLogNormalVO.setIsDelete(smsDbLog.getIsDelete());
            smsAccountLogNormalVOList.add(smsAccountLogNormalVO);
        });
        return smsAccountLogNormalVOList;
    }


    /**
     * //detailDbDtoList -> showDtoList
     * @param detailDbDtoList
     *
     * LineAccountDetailVO
     *    private String groupId;
     *    private String lineSupplier;
     *    private String linesInfo;
     *    private BigDecimal price;
     *    private Date effectStartDate;
     *    private Date effectEndDate;
     *    private Integer enabled;
     *    private Date createTime;
     *    private Date updateTime;
     *    private Integer isDelete;
     * @return
     */
    private List<LineAccountDetailVO> converToShowVOList(List<LineAccountDetailDTO> detailDbDtoList) {
        List<LineAccountDetailVO> detailShowDTOList = new ArrayList<>();
        detailDbDtoList.forEach(dto -> {
            LineAccountDetailVO showDTOItem = new LineAccountDetailVO();
            showDTOItem.setGroupId(String.valueOf(dto.getGroupId()));
            showDTOItem.setPrice(dto.getPrice());
            showDTOItem.setEffectStartDate(dto.getEffectStartDate());
            showDTOItem.setEffectEndDate(dto.getEffectEndDate());
            showDTOItem.setEnabled(dto.getEnabled());
            showDTOItem.setCreateTime(dto.getCreateTime());
            showDTOItem.setUpdateTime(dto.getUpdateTime());
            showDTOItem.setIsDelete(dto.getIsDelete());
            LineSupplierInfoNormal lineSupplierItem = lineSupplierInfoNormalMapper.selectByPrimaryKey(dto.getLineSupplierId());
            showDTOItem.setLineSupplier(lineSupplierItem.getLineSupplier());

            JSONArray linesInfo = new JSONArray();
            List<Long> gatewayIdList = Arrays.stream(dto.getGatewayIds().split(","))
                    .map(String::trim)
                    .map(Long::valueOf)
                    .collect(Collectors.toList());
            List<LineBaseInfoNormal> baseInfoNormalList = lineBaseInfoNormalMapper.selectByGatewayIdListtikv_(gatewayIdList);
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
            List<LineBaseInfoNormal> baseInfoNormalList = lineBaseInfoNormalMapper.selectByGatewayIdListtikv_(gatewayIdList);
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
    private LineBaseShowInfoDTO.LineBaseInfo convertToLineBaseInfo(LineBaseFullInfoDTO fullInfo) {
        LineBaseShowInfoDTO.LineBaseInfo baseInfo = new LineBaseShowInfoDTO.LineBaseInfo();
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
