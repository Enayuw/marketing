package com.br.marketing.dto.shuhe.strategy;

import com.br.common.util.BrCipherMaker;
import com.br.marketing.dto.shuhe.ShuheTransferJsonDTO;
import com.br.marketing.entity.CaseShuheUser;
import com.br.marketing.entity.CaseShuheUserWithBLOBs;
import com.br.marketing.service.IMarketingSyncUserService;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Map;

/**
 * 场景策略
 *
 * @author Guo Zeqiang
 * @dateTime 2022/2/10 16:54
 */
public abstract class IUserType {
    private String userType;
    private final String Y = "Y";
    public final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");


    public IUserType setUserType(String userType) {
        this.userType = userType;
        return this;
    }

    /**
     * 将推送的数据转换为本地数据
     *
     * @param dataItem 业务数据
     * @author Guo Zeqiang
     * @dateTime 2022/2/10 17:30
     */
    protected abstract void getCaseUser(Map<String, String> dataItem, CaseShuheUser caseUser);

    /**
     * 2022/2/11 14:03
     * 初始pojo
     */
    protected final CaseShuheUserWithBLOBs initCaseUser(ShuheTransferJsonDTO jsonDTO, String apiCode, String jsonData) {
        CaseShuheUserWithBLOBs caseUser = new CaseShuheUserWithBLOBs();
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
    protected final void setTotalField(Map<String, String> dataItem, CaseShuheUser caseUser) {
        if (this instanceof CuShouDeng) {
            new CuShenWan().getCaseUser(dataItem, caseUser);
            new CuShouJie().getCaseUser(dataItem, caseUser);
        } else if (this instanceof CuShenWan) {
            new CuShouDeng().getCaseUser(dataItem, caseUser);
            new CuShouJie().getCaseUser(dataItem, caseUser);
        } else if (this instanceof CuShouJie) {
            new CuShenWan().getCaseUser(dataItem, caseUser);
            new CuShouDeng().getCaseUser(dataItem, caseUser);
        } else {
            new CuShenWan().getCaseUser(dataItem, caseUser);
            new CuShouDeng().getCaseUser(dataItem, caseUser);
            new CuShouJie().getCaseUser(dataItem, caseUser);
        }
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
    public abstract boolean ifTransfer(CaseShuheUser caseShuheUser, IMarketingSyncUserService iMarketingSyncUserService);

    /**
     * 全部场景空判断
     */
    public boolean isEmpty(CaseShuheUser caseShuheUser) {
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

}
