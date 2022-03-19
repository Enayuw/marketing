package com.br.marketing.rule.shuhe;

import com.alibaba.fastjson.JSONObject;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.client.dassservice.input.userdata.DassSingleImportAdapDTO;
import com.br.marketing.client.dassservice.input.userdata.DassSingleImportDataDTO;
import com.br.marketing.client.dassservice.input.userdata.RealTimeUserDataDTO;
import com.br.marketing.dto.shuhe.strategy.IUserType;
import com.br.marketing.entity.CaseShuheUser;
import com.br.marketing.entity.MarketingTransferSyncUser;
import com.br.marketing.entity.MarketingTransferSyncUserExample;
import com.br.marketing.entity.PhoneSaleExtendShuhe;
import com.br.marketing.mapper.MarketingTransferSyncUserMapper;
import com.br.marketing.origin.ProcessHandlerContext;
import com.br.marketing.origin.ShuHeProcessHandlerContext;
import com.br.marketing.rule.AssembleData;
import com.br.marketing.service.IPushShuheTransferDataService;
import com.br.marketing.strategy.InterfaceHandlerEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;

/**
 * 数禾转化推送人工电销 业务
 *
 * @author Guo Zeqiang
 * @dateTime 2022/3/18 14:45
 */
@Service
@Slf4j
public class ShuHeArtificialRealTimeUserDataFromDelayImpl implements AssembleData<RealTimeUserDataDTO> {

    @Resource
    private MarketingTransferSyncUserMapper marketingTransferSyncUserMapper;
    @Resource
    private RedisChgService redisChgService;
    @Resource
    private IPushShuheTransferDataService iPushShuheTransferDataService;
    private final static DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");


    @Override
    public RealTimeUserDataDTO assemble(Object transmitFact, ProcessHandlerContext context) {
        MarketingTransferSyncUser transfer = (MarketingTransferSyncUser) transmitFact;
        ShuHeProcessHandlerContext shuHeContext = (ShuHeProcessHandlerContext) context;
        RealTimeUserDataDTO realTimeUserDataDTO = new RealTimeUserDataDTO();
        realTimeUserDataDTO.setDassSingleImportAdapDTO(getDassSingleImportAdap(transfer, shuHeContext));
        realTimeUserDataDTO.setPhoneSaleExtendShuhe(getPhoneSaleExtendShuhe(transfer));
        log.warn("@2数禾转化推送人工电销:{}", realTimeUserDataDTO);
        return realTimeUserDataDTO;
    }

    @Override
    public boolean isNeedAssemble(Object transmitFact, ProcessHandlerContext context) {
        boolean bool = Boolean.FALSE;
        if (transmitFact instanceof MarketingTransferSyncUser) {
            MarketingTransferSyncUser transfer = (MarketingTransferSyncUser) transmitFact;
            Integer isDelay = context.getMqFact().getIsDelay();
            if (isDelay != null && isDelay != 1) {
                String tCid = StringUtils.isEmpty(transfer.gettCid()) ? redisChgService.get(
                        String.format(ShuHeArtificialRealTimeUserDataToDelayImpl.KEY
                                , transfer.getApiCode(), transfer.getUserType(), transfer.getCustNum())) : transfer.gettCid();
                MarketingTransferSyncUser dbTransferSyncUser = getDbTransferSyncUser(
                        transfer.getCustNum(), transfer.getApiCode()
                        , transfer.getUserType(), tCid, transfer.getCreateTime());
                if (dbTransferSyncUser != null) {
                    String reserveField1 = dbTransferSyncUser.getReserveField1();
                    JSONObject object = JSONObject.parseObject(reserveField1);
                    String isTurn = object.getString("is_turn");
                    String isBlack = object.getString("is_black");
                    String applyTime = dbTransferSyncUser.getApplyTime();
                    ShuHeProcessHandlerContext shuHeContext = new ShuHeProcessHandlerContext(context);
                    iPushShuheTransferDataService.handlerContext(shuHeContext, transfer);
                    IUserType iUserType = shuHeContext.getiUserType();
                    if (!StringUtils.isEmpty(applyTime) && !iUserType.getY().equals(isTurn)
                            && !iUserType.getY().equals(isBlack)) {
                        LocalDate clcUsrIsoAtoTim = LocalDateTime.parse(applyTime, DateTimeFormatter.ofPattern(""))
                                .toLocalDate();
                        LocalDate createDate = shuHeContext.getCreatTime().toInstant().atZone(
                                ZoneId.systemDefault()).toLocalDate();
                        bool = !(clcUsrIsoAtoTim.isAfter(createDate) || clcUsrIsoAtoTim.isEqual(createDate));
                    }
                }
            }
            log.warn("@1数禾转化推送人工电销剔除规则状态:{}", bool);
        }
        return bool;
    }

    @Override
    public String label() {
        return "ShuHe_TransferData_ArtificialRealTimeUserDataFromDelay";
    }

    @Override
    public Integer dataDirection() {
        return InterfaceHandlerEnum.ARTIFICIAL_REAL_TIME_USERDATA.getCode();
    }

    /**
     * 查询db获取cusNum当天新的数据
     */
    private MarketingTransferSyncUser getDbTransferSyncUser(String custNum, String apiCode, String userType
            , String tCid, Date createTime) {
        MarketingTransferSyncUserExample example = new MarketingTransferSyncUserExample();
        example.createCriteria().andApiCodeEqualTo(apiCode).andUserTypeEqualTo(userType)
                .andCustNumEqualTo(custNum).andCreateTimeGreaterThanOrEqualTo(createTime);
        example.settCid(tCid);
        example.setOrderByClause("create_time desc limit 0,1");
        List<MarketingTransferSyncUser> transferList = marketingTransferSyncUserMapper.selectByExample(example);
        return transferList.size() > 0 ? transferList.get(0) : null;
    }

    /**
     * 封装电销接口数据
     */
    private DassSingleImportAdapDTO getDassSingleImportAdap(MarketingTransferSyncUser transfer
            , ShuHeProcessHandlerContext shuHeContext) {
        CaseShuheUser caseShuheUser = shuHeContext.getCaseShuheUser();
        DassSingleImportAdapDTO adapDTO = new DassSingleImportAdapDTO();
        adapDTO.setDassSingleImportDataDTO(getDassSingleImportData(caseShuheUser, transfer));
        adapDTO.setTransferInfoId(transfer.getId());
        return adapDTO;
    }

    /**
     * 封装电销扩展数据
     */
    private PhoneSaleExtendShuhe getPhoneSaleExtendShuhe(MarketingTransferSyncUser transfer) {
        PhoneSaleExtendShuhe phoneSaleExtendShuhe = new PhoneSaleExtendShuhe();
        phoneSaleExtendShuhe.setCustNum(transfer.getCustNum());
        LocalDateTime localDateTime = LocalDateTime.now().atZone(ZoneId.systemDefault()).toLocalDateTime();
        LocalDate localDate = localDateTime.toLocalDate();
        phoneSaleExtendShuhe.setAppletDate(localDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        phoneSaleExtendShuhe.setAppletTime(localDateTime.format(DATE_TIME_FORMATTER));
        phoneSaleExtendShuhe.setStatus("a");
        return phoneSaleExtendShuhe;
    }

    private DassSingleImportDataDTO getDassSingleImportData(CaseShuheUser caseShuheUser
            , MarketingTransferSyncUser transfer) {
        DassSingleImportDataDTO dataDTO = new DassSingleImportDataDTO();
        dataDTO.setPrioritySymbol("1");
        JSONObject extend = new JSONObject();
        extend.put("face_recognitiion", valuableAndCurrentDay(caseShuheUser.getClcUsrIsoPhoTim()));
        extend.put("is_usr_idt", valuableAndCurrentDay(caseShuheUser.getClcUsrIsoIdtTim()));
        extend.put("is_bindcard", valuableAndCurrentDay(caseShuheUser.getClcUsrIsoCrdTim()));
        extend.put("is_usr_inf", valuableAndCurrentDay(caseShuheUser.getClcUsrIsoInfTim()));
        extend.put("is_usr_lst_app_sta_tim", valuableAndCurrentDay(caseShuheUser.getClcUsrLstAppStaTim()));
        extend.put("typeSign", "1");
        dataDTO.setOrgname("shuheshenwan");
        dataDTO.setPhone(caseShuheUser.getCell());
        dataDTO.setUserType("2");
        dataDTO.setLoginTime(transfer.getLoginTime());
        dataDTO.setSource("16");
        dataDTO.setType("2");
        dataDTO.setExtend(extend.toJSONString());
        return dataDTO;
    }

    /**
     * 判断有值且日期为当天
     */
    private String valuableAndCurrentDay(String dateTimeStr) {
        String value = "0";
        if (StringUtils.isEmpty(dateTimeStr)) {
            return value;
        }
        LocalDate localDate = LocalDateTime.parse(dateTimeStr, DATE_TIME_FORMATTER)
                .atZone(ZoneId.systemDefault()).toLocalDate();
        return LocalDate.now().isEqual(localDate) ? "1" : value;
    }
}
