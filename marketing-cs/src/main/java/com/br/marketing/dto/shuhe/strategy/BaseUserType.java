package com.br.marketing.dto.shuhe.strategy;

import com.br.common.util.BrCipherMaker;
import com.br.marketing.adapter.transfer.adaptee.CaseShuheUserAdaptee;
import com.br.marketing.client.dassservice.input.userdata.DassSingleImportDataDTO;
import com.br.marketing.dto.shuhe.ShuheTransferJsonDTO;
import com.br.marketing.entity.CaseShuheUser;
import com.br.marketing.service.IMarketingSyncUserService;
import com.br.marketing.service.Impl.CaseUserServiceImpl;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.*;

/**
 * 场景策略
 *
 * @author Guo Zeqiang
 * @dateTime 2022/2/10 16:54
 */
public abstract class BaseUserType {
    protected String userType;
    protected final String Y = "Y";
    protected final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss[:SSS]");
    protected final DateTimeFormatter dateTime2Formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    protected List<String> apiCodes = new ArrayList<>(Collections.singletonList("7410785"));

    public BaseUserType setUserType(String userType) {
        this.userType = userType;
        return this;
    }

    BaseUserType(String... api2Codes) {
        Collections.addAll(apiCodes, api2Codes);
    }

    public void setApiCodes(List<String> apiCodes) {
        this.apiCodes.addAll(apiCodes);
    }

    /**
     * 将推送的数据转换为本地数据
     *
     * @param dataItem 业务数据
     * @author Guo Zeqiang
     * @dateTime 2022/2/10 17:30
     */
    abstract void getCaseUser(Map<String, String> dataItem, CaseShuheUser caseUser);

    /**
     * 2022/2/11 14:03
     * 初始pojo
     */
    final CaseShuheUser initCaseUser(ShuheTransferJsonDTO jsonDTO, String apiCode, String jsonData) {
        CaseShuheUser caseUser = new CaseShuheUserAdaptee();
        caseUser.setApiCode(apiCode);
        final Map<String, String> dataItem = jsonDTO.getDataItem();
        caseUser.setIsTurn(dataItem.getOrDefault("is_turn", ""));
        caseUser.setIsBlack(dataItem.getOrDefault("is_black", ""));
        caseUser.setCustNum(jsonDTO.getOrderId());
        caseUser.setCreateTime(Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant()));
        caseUser.setUploadDate(LocalDateTime.now().format(DateTimeFormatter.BASIC_ISO_DATE));
        caseUser.setBiztype(jsonDTO.getBizType());
        caseUser.setUserType(this.userType);
        caseUser.setMobile(jsonDTO.getMobile());
        caseUser.setCell(BrCipherMaker.getInstance().encode(jsonDTO.getMobile()));
        caseUser.setJsonData(jsonData);
        return caseUser;
    }

    /**
     * 赋值 其他字段
     */
    final void setTotalField(Map<String, String> dataItem, CaseShuheUser caseUser) {
        String defaultValue = "";
        caseUser.setClcUsrLstAppStaTim(dataItem.getOrDefault("clc_usr_lst_app_sta_tim", defaultValue));
        caseUser.setClcUsrIsoPhoTim(dataItem.getOrDefault("clc_usr_iso_pho_tim", defaultValue));
        caseUser.setClcUsrIsoIdtTim(dataItem.getOrDefault("clc_usr_iso_idt_tim", defaultValue));
        caseUser.setClcUsrIsoCrdTim(dataItem.getOrDefault("clc_usr_iso_crd_tim", defaultValue));
        caseUser.setClcUsrIsoInfTim(dataItem.getOrDefault("clc_usr_iso_inf_tim", defaultValue));
        caseUser.setClcUsrIsoAtoTim(dataItem.getOrDefault("clc_usr_iso_ato_tim", defaultValue));
        caseUser.setClcUsrAdtTimRcnLon(dataItem.getOrDefault("clc_usr_adt_tim_rcn_lon", defaultValue));
        caseUser.setClcUsrFstLogTimAll(dataItem.getOrDefault("clc_usr_fst_log_tim_all", defaultValue));
        caseUser.setClcUsrAdtLmtItr(dataItem.getOrDefault("clc_usr_adt_lmt_itr", defaultValue));
        caseUser.setClcUsrFrtFqOrdTim(dataItem.getOrDefault("clc_usr_frt_fq_ord_tim", defaultValue));
        caseUser.setClcUsrFstLndTimCshBtHl(dataItem.getOrDefault("clc_usr_fst_lnd_tim_csh_bt_hl", defaultValue));
        caseUser.setClcUsrMaxDxRrtEnd(dataItem.getOrDefault("clc_usr_max_dx_rrt_end", defaultValue));
        caseUser.setUsrForbidCallEndTim(dataItem.getOrDefault("usr_forbid_call_end_tim", defaultValue));
    }

    /**
     * 不同场景判断转化
     * 4.判断逻辑（D2022018修改）
     * <p>
     * 断点判断规则
     * 值不为空且
     * <p>
     * clc_usr_iso_ato_tim>creattime(上传接口上传该数据时间)   促申完
     * <p>
     * clc_usr_fst_log_tim_all>creattime(上传接口上传该数据时间)  促首登
     * <p>
     * clc_usr_frt_fq_ord_tim>creattime(上传接口上传该数据时间)  	促首借
     */
    public abstract boolean ifTransfer(CaseShuheUser caseShuheUser
            , Date creatTime);

    /**
     * 全部场景空判断
     */
    public final boolean isEmpty(CaseShuheUser caseShuheUser) {
        return (StringUtils.isEmpty(caseShuheUser.getIsBlack())
                && StringUtils.isEmpty(caseShuheUser.getIsTurn())
                && StringUtils.isEmpty(caseShuheUser.getUserType())
                && StringUtils.isEmpty(caseShuheUser.getClcUsrIsoAtoTim())
                && StringUtils.isEmpty(caseShuheUser.getClcUsrFstLogTimAll())
                && StringUtils.isEmpty(caseShuheUser.getClcUsrFrtFqOrdTim()));
    }

    /**
     * 黑名单判断
     */
    public boolean isBlack(CaseShuheUser caseShuheUser) {
        return Y.equals(caseShuheUser.getIsBlack());
    }

    /**
     * 转化判断
     */
    public boolean isTurn(CaseShuheUser caseShuheUser) {
        return Y.equals(caseShuheUser.getIsTurn());
    }

    /**
     * 数据有效期
     *
     * @param caseShuheUser             pojo
     * @param iMarketingSyncUserService javaBean
     * @param creatTime                 上传表创建时间
     * @return true or false 在有效期内为true 否则为false
     * @author Guo Zeqiang
     * @dateTime 2022/2/22 15:48
     */
    @Deprecated
    public abstract boolean dataPeriodOfValidity(CaseShuheUser caseShuheUser
            , IMarketingSyncUserService iMarketingSyncUserService, Date creatTime);

    /**
     * 数据有效期
     *
     * @param iMarketingSyncUserService javaBean
     * @param creatTime                 上传表创建时间
     * @return true or false 在有效期内为true 否则为false
     * @author Guo Zeqiang
     * @dateTime 2022/2/22 15:48
     */
    public abstract boolean dataPeriodOfValidity(IMarketingSyncUserService iMarketingSyncUserService, Date creatTime);

    /**
     * 数据有效期
     *
     * @param iMarketingSyncUserService javaBean
     * @param tCreatTime                转化数据创建时间
     * @param creatTime                 上传表创建时间
     * @return true or false 在有效期内为true 否则为false
     * @author Guo Zeqiang
     * @dateTime 2022/2/22 15:48
     */
    public abstract boolean dataPeriodOfValidity(IMarketingSyncUserService iMarketingSyncUserService
            , Date tCreatTime, Date creatTime);

    /**
     * 数据有效期
     *
     * @param iMarketingSyncUserService javaBean
     * @param tCreatTime                转化数据创建时间
     * @param creatTime                 上传表创建时间
     * @param day                       有效期
     * @return true or false 在有效期内为true 否则为false
     * @author Guo Zeqiang
     * @dateTime 2022/3/22 15:48
     * @deprecated 已迁移使用新版有效期
     */
    @Deprecated
    public boolean dataPeriodOfValidity(IMarketingSyncUserService iMarketingSyncUserService
            , Date tCreatTime, Integer day, Date creatTime) {
        return iMarketingSyncUserService.isPeriodOfValidity(tCreatTime, day, creatTime);
    }


    /**
     * 黑名单失效日期
     *
     * @param creatTime 有效期时间
     * @return yyyy-MM-dd hh:mm:ss  日期精确到日期，时分秒补充23：59：59即可
     * @author Guo Zeqiang
     * @dateTime 2022/2/22 15:48
     */
    public abstract String getBlackExpireDate(Date creatTime);

    /**
     * 计算失效日期
     */
    final String calculateExpireDate(Date date, int day) {
        if (date == null) {
            return "";
        }
        final LocalDateTime localDateTime = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        final LocalDateTime localDateTimeNew;
        if (day == 0) {
            localDateTimeNew = localDateTime.with(TemporalAdjusters.lastDayOfMonth());
        } else {
            localDateTimeNew = localDateTime.plusDays(day);
        }
        return localDateTimeNew.withHour(23).withMinute(59).withSecond(59).format(dateTime2Formatter);
    }

    public final boolean isY(String str) {
        return Y.equals(str);
    }

    /**
     * 前置剔除条件
     */
    public abstract boolean ifGiveUp(CaseShuheUser caseShuheUser, Date creatTime);

    /**
     * 前置剔除条件
     */
    public abstract boolean ifGiveUp(CaseShuheUser caseShuheUser, Date creatTime, CaseUserServiceImpl caseUserService);

    /**
     * 获取私有信息
     */
    public abstract void getPrivateInfo(DassSingleImportDataDTO dataDTO);

    /**
     * 转电销规则
     * true 满足推电销
     */
    public abstract boolean isSatisfyPhoneSale(CaseShuheUser caseShuheUser, Date creatTime);

    public List<String> getApiCodes() {
        return apiCodes;
    }

    public boolean isDxRrrEndAndY(CaseShuheUser caseShuheUser,CaseUserServiceImpl caseUserService){
        return caseUserService.isY(caseShuheUser.getCell())||caseUserService.isRrtEnd(caseShuheUser.getCell());
    }
}
