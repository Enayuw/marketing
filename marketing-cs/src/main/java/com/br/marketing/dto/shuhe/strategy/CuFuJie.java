package com.br.marketing.dto.shuhe.strategy;

import com.alibaba.fastjson.JSONObject;
import com.br.marketing.client.dassservice.input.userdata.DassSingleImportDataDTO;
import com.br.marketing.entity.CaseShuheUser;
import com.br.marketing.service.IMarketingSyncUserService;
import com.br.marketing.service.Impl.CaseUserServiceImpl;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 促复借 场景
 *
 * @author Guo Zeqiang
 * @dateTime 2022/4/12 15:14
 */
@Slf4j
public class CuFuJie extends BaseUserType {

    public CuFuJie(String... api2Codes) {
        super(api2Codes);
        super.apiCodes.add("3710043");
    }

    private final static Pattern NONNEGATIVE_FLOATING_NUMBER_REGEX = Pattern.compile("(\\d+)(\\.\\d+)?");

    @Override
    void getCaseUser(Map<String, String> dataItem, CaseShuheUser caseUser) {

    }

    @Override
    public boolean ifTransfer(CaseShuheUser caseShuheUser, Date creatTime) {
        return false;
    }

    public boolean ifTransfer(CaseShuheUser caseShuheUser, Date creatTime, MarketingCommonConfig marketingCommonConfig) {
        boolean ifTransfer1 = Boolean.FALSE;
        boolean ifTransfer2 = Boolean.FALSE;
        if (creatTime == null) {
            return false;
        }
        JSONObject jsonObject = caseShuheUser.getJsonObject();
        String clcUsrLstNonDcpTrsTim = jsonObject.getString("clc_usr_lst_non_dcp_trs_tim");
        String offUsrLstOrdTimAll = jsonObject.getString("off_usr_lst_ord_tim_all");
        LocalDate appletDate = creatTime.toInstant().atZone(
                ZoneId.systemDefault()).toLocalDateTime().toLocalDate();
        if (!StringUtils.isEmpty(clcUsrLstNonDcpTrsTim) && !StringUtils.isEmpty(offUsrLstOrdTimAll)) {
            LocalDate dcpTrsTim = LocalDateTime.parse(clcUsrLstNonDcpTrsTim, dateTimeFormatter).toLocalDate();
            LocalDate ordTimAll = LocalDateTime.parse(offUsrLstOrdTimAll, dateTimeFormatter).toLocalDate();
            ifTransfer1 = (dcpTrsTim.isAfter(appletDate) || dcpTrsTim.isEqual(appletDate)) && (ordTimAll.isBefore(appletDate));
        }
        if (ifTransfer1) {
            return ifTransfer1;
        }
        Double clcUsrAvlLmtLv0;
        try {
            clcUsrAvlLmtLv0 = jsonObject.getDouble("clc_usr_avl_lmt_lv0");
        } catch (Exception e) {
            log.error("数禾促复借转化数据推送,clc_usr_avl_lmt_lv0字段客户传入数据格式非double类型！apicode={},传入的clc_usr_avl_lmt_lv0值={}"
                    , caseShuheUser.getApiCode(), jsonObject.getString("clc_usr_avl_lmt_lv0"));
            return false;
        }
        if (!StringUtils.isEmpty(offUsrLstOrdTimAll) && !StringUtils.isEmpty(clcUsrAvlLmtLv0)) {
            Double max = marketingCommonConfig.getClcUsrAvlLmtLv0();
            if (max == null) {
                max = 100.0;
            }
            LocalDate ordTimAll = LocalDateTime.parse(offUsrLstOrdTimAll, dateTimeFormatter).toLocalDate();
            ifTransfer2 = (ordTimAll.isAfter(appletDate) || ordTimAll.isEqual(appletDate)) && (clcUsrAvlLmtLv0 < max);
        }
        return ifTransfer1 || ifTransfer2;
    }

    @Override
    public boolean dataPeriodOfValidity(CaseShuheUser caseShuheUser, IMarketingSyncUserService iMarketingSyncUserService, Date creatTime) {
        return false;
    }

    @Override
    public boolean dataPeriodOfValidity(IMarketingSyncUserService iMarketingSyncUserService, Date creatTime) {
        return false;
    }

    @Override
    public boolean dataPeriodOfValidity(IMarketingSyncUserService iMarketingSyncUserService, Date tCreatTime, Date creatTime) {
        return false;
    }

    @Override
    public String getBlackExpireDate(Date creatTime) {
        return null;
    }

    @Override
    public boolean ifGiveUp(CaseShuheUser caseShuheUser, Date creatTime) {
//        return isY(caseShuheUser.getIsTurn()) || isY(caseShuheUser.getIsBlack());
        return isY(caseShuheUser.getIsTurn());
    }

    @Override
    public boolean ifGiveUp(CaseShuheUser caseShuheUser, Date creatTime, CaseUserServiceImpl caseUserService) {
        return ifGiveUp(caseShuheUser,creatTime)
                ||isDxRrrEndAndY(caseShuheUser,caseUserService);
    }

    @Override
    public void getPrivateInfo(DassSingleImportDataDTO dataDTO) {
        dataDTO.setOrgname("shuhefujie");
        dataDTO.setSource("16");
        dataDTO.setUserType("1");
        dataDTO.setType("4");

    }

    @Override
    public boolean isSatisfyPhoneSale(CaseShuheUser caseShuheUser, Date creatTime) {
        return false;
    }

    public boolean isSatisfyPhoneSale(CaseShuheUser caseShuheUser, Date creatTime
            , MarketingCommonConfig marketingCommonConfig) {
        HashMap<String, List<String>> statusMap = marketingCommonConfig.getShuHePushDXStatusMap();
        List<String> status;
        if (statusMap == null
                || (status = statusMap.getOrDefault(caseShuheUser.getUserType(), null)) == null) {
            status = Arrays.asList("a", "b");
        }
        JSONObject jsonObject = caseShuheUser.getJsonObject();
        if (status.contains("a")) {
            // 情况a
            String a = statusA(caseShuheUser, creatTime, marketingCommonConfig);
            if (a != null) {
                caseShuheUser.setReserveField2(a);
                jsonObject.put("prioritySymbol", "1");
                jsonObject.put("typeSign", "1");
                return true;
            }
        }
        if (status.contains("b")) {
            // 情况b
            String b = statusB(caseShuheUser, creatTime, marketingCommonConfig);
            if (b != null) {
                caseShuheUser.setReserveField2(b);
                jsonObject.put("prioritySymbol", "2");
                jsonObject.put("typeSign", "2");
                return true;
            }
        }
        caseShuheUser.setJsonObject(jsonObject);
        return false;
    }

    /**
     * 规则
     * clc_usr_lst_app_sta_tim>=上传接口该案件编号创建时间
     * &
     * clc_usr_lst_non_dcp_trs_tim（非空）<=上传接口该案件编号创建时间（创建时间非空）
     * &
     * off_usr_lst_ord_tim_all（非空）<=上传接口该案件编号创建时间（创建时间非空）
     * &
     * userType=促复借
     * &
     * cusNun
     * &
     * 有效期内
     * &
     * 剔除已转化数据
     */
    private String statusA(CaseShuheUser caseShuheUser, Date creatTime, MarketingCommonConfig marketingCommonConfig) {
        JSONObject jsonObject = caseShuheUser.getJsonObject();
        String defaultValue = "";
        String clcUsrLstAppStaTim = (String) jsonObject.getOrDefault("clc_usr_lst_app_sta_tim", defaultValue);
        if (creatTime == null || org.apache.commons.lang3.StringUtils.isBlank(clcUsrLstAppStaTim)) {
            return null;
        }
        LocalDateTime appStaTim = LocalDateTime.parse(clcUsrLstAppStaTim, dateTimeFormatter);
        LocalDateTime localCreatTime = creatTime.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        // 2022-5-9 14:51:08 需求变更，去掉等于的条件
//        if (appStaTim.isAfter(localCreatTime) || appStaTim.isEqual(localCreatTime)) {
        if (appStaTim.isAfter(localCreatTime)) {
            String clcUsrLstNonDcpTrsTim = (String) jsonObject.getOrDefault("clc_usr_lst_non_dcp_trs_tim"
                    , defaultValue);
            if (org.apache.commons.lang3.StringUtils.isBlank(clcUsrLstNonDcpTrsTim)) {
                return null;
            }
            LocalDateTime nonDcpTrsTim = LocalDateTime.parse(clcUsrLstNonDcpTrsTim, dateTimeFormatter);
            // 2022-5-9 14:51:08 需求变更，去掉等于的条件
//            if (nonDcpTrsTim.isBefore(localCreatTime) || nonDcpTrsTim.isEqual(localCreatTime)) {
            if (nonDcpTrsTim.isBefore(localCreatTime)) {
                String offUsrLstOrdTimAll = (String) jsonObject.getOrDefault("off_usr_lst_ord_tim_all"
                        , defaultValue);
                if (org.apache.commons.lang3.StringUtils.isBlank(offUsrLstOrdTimAll)) {
                    return null;
                }
                LocalDateTime ordTimAll = LocalDateTime.parse(offUsrLstOrdTimAll, dateTimeFormatter);
                if (ordTimAll.isBefore(localCreatTime) || ordTimAll.isEqual(localCreatTime)) {
                    if (this.ifTransfer(caseShuheUser, creatTime, marketingCommonConfig)) {
                        return null;
                    }
                    return "a";
                }
            }

        }
        return null;
    }

    /**
     * 规则
     * clc_usr_lst_app_sta_tim>=上传接口该案件编号创建时间
     * &
     * clc_usr_lst_non_dcp_trs_tim>=上传接口该案件编号创建时间（创建时间非空）
     * &
     * off_usr_lst_ord_tim_all>=上传接口该案件编号创建时间（创建时间非空）
     * &
     * clc_usr_avl_lmt_lv0>=100(该字段考虑做成配置，后期会调整为区间值)
     * &
     * userType=促复借
     * &
     * cusNun
     * &
     * 有效期内
     * &
     * 剔除D20220424数禾促复借转化数据推送-3710043-337（营销→客服）已转化数据
     * &T日拨打情况
     * 需追加判断T日至推送时间止，是否ai拨打过，已拨打过7天内该案件编号停止推送，若未拨打过T日正常推送
     */
    private String statusB(CaseShuheUser caseShuheUser, Date creatTime
            , MarketingCommonConfig marketingCommonConfig) {
        JSONObject jsonObject = caseShuheUser.getJsonObject();
        String defaultValue = "";
        String clcUsrLstAppStaTim = (String) jsonObject.getOrDefault("clc_usr_lst_app_sta_tim", defaultValue);
        if (creatTime == null || org.apache.commons.lang3.StringUtils.isBlank(clcUsrLstAppStaTim)) {
            return null;
        }
        LocalDateTime appStaTim = LocalDateTime.parse(clcUsrLstAppStaTim, dateTimeFormatter);
        LocalDateTime localCreatTime = creatTime.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        if (appStaTim.isAfter(localCreatTime) || appStaTim.isEqual(localCreatTime)) {
            String clcUsrLstNonDcpTrsTim = (String) jsonObject.getOrDefault("clc_usr_lst_non_dcp_trs_tim"
                    , defaultValue);
            if (org.apache.commons.lang3.StringUtils.isBlank(clcUsrLstNonDcpTrsTim)) {
                return null;
            }
            LocalDateTime nonDcpTrsTim = LocalDateTime.parse(clcUsrLstNonDcpTrsTim, dateTimeFormatter);
            if (nonDcpTrsTim.isAfter(localCreatTime) || nonDcpTrsTim.isEqual(localCreatTime)) {
                String offUsrLstOrdTimAll = (String) jsonObject.getOrDefault("off_usr_lst_ord_tim_all"
                        , defaultValue);
                if (org.apache.commons.lang3.StringUtils.isBlank(offUsrLstOrdTimAll)) {
                    return null;
                }
                LocalDateTime ordTimAll = LocalDateTime.parse(offUsrLstOrdTimAll, dateTimeFormatter);
                if (ordTimAll.isAfter(localCreatTime) || ordTimAll.isEqual(localCreatTime)) {
                    String clcUsrAvlLmtLv0 = (String) jsonObject.getOrDefault("clc_usr_avl_lmt_lv0", defaultValue);
                    if (org.apache.commons.lang3.StringUtils.isBlank(clcUsrAvlLmtLv0)) {
                        return null;
                    }
                    Matcher matcher = NONNEGATIVE_FLOATING_NUMBER_REGEX.matcher(clcUsrAvlLmtLv0);
                    List<String> list = new ArrayList<>();
                    while (matcher.find()) {
                        list.add(matcher.group());
                    }
                    boolean compareTo;
                    List<String> range = marketingCommonConfig.getShuHeUserAvailableQuotaRange();
                    int size = list.size();
                    if (size > 1) {
                        compareTo = compareTo(list.get(0), list.get(1), range);
                    } else if (size == 1) {
                        compareTo = compareTo(list.get(0), null, range);
                    } else {
                        compareTo = Boolean.FALSE;
                    }
                    if (!compareTo || this.ifTransfer(caseShuheUser, creatTime, marketingCommonConfig)) {
                        return null;
                    }
                    return "b";
                }
            }

        }
        return null;
    }


    /**
     * 相比于
     *
     * @param v11   比较数值1, 此值为必填参数，为空时默认返回{@code false}
     * @param v12   比较数值2
     * @param range {@link List}自定义范围 eg: new ArrayList<>(Arrays.asList(">=", "10", "<=", "50"))
     */
    public boolean compareTo(@NotNull String v11, String v12, List<String> range) {
        if (range == null) {
            range = Arrays.asList("&ge;", "100");
        }
        if (org.apache.commons.lang3.StringUtils.isBlank(v11) || range.isEmpty()) {
            return false;
        }
        int size = range.size();
        int l = 2;
        if (size % l != 0) {
            return false;
        }
        int len = size / l;
        Boolean bool1 = null;
        Boolean bool2 = null;
        switch (len) {
            case 2:
                if (org.apache.commons.lang3.StringUtils.isNotBlank(v12)) {
                    String v22 = range.get(3);
                    if (org.apache.commons.lang3.StringUtils.isNotBlank(v22)) {
                        String sign2 = range.get(2);
                        bool2 = compare(v12, v22, sign2);
                    }
                } else {
                    String v22 = range.get(3);
                    if (org.apache.commons.lang3.StringUtils.isNotBlank(v22)) {
                        String sign2 = range.get(2);
                        bool2 = compare(v11, v22, sign2);
                    }
                }
            case 1:
                if (org.apache.commons.lang3.StringUtils.isNotBlank(v11)) {
                    String v21 = range.get(1);
                    if (org.apache.commons.lang3.StringUtils.isNotBlank(v21)) {
                        String sign1 = range.get(0);
                        bool1 = compare(v11, v21, sign1);
                    }
                }
                break;
            default:

        }
        return (bool1 != null && bool2 != null) ? bool1 && bool2 : bool1 != null
                ? bool1 : bool2 != null ? bool2 : false;
    }


    /**
     * 比较数值
     */
    private boolean compare(String v1, String v2, String sign) {
        final int i = new BigDecimal(v1).compareTo(new BigDecimal(v2));
        switch (sign) {
            // 大于
            case "&gt;":
                return i > 0;
            // 小于
            case "&lt;":
                return i < 0;
            // 等于
            case "&eq;":
                return i == 0;
            // 不等于
            case "&nq;":
                return i != 0;
            // 大于等于
            case "&ge;":
                return i > -1;
            // 小于等于
            case "&le;":
                return i < 1;
            default:
                return false;
        }
    }
}
