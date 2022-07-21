package com.br.marketing.adapter.transfer.adaptee;

import com.alibaba.fastjson.JSONObject;
import com.br.marketing.adapter.transfer.IToTransferSyncAdaptee;
import com.br.marketing.dto.shuhe.ShuheTransferJsonDTO;
import com.br.marketing.entity.CaseShuheUser;
import com.br.marketing.entity.MarketingTransferSyncUser;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * 数禾适配者
 *
 * @author Guo Zeqiang
 * @dateTime 2022/3/16 16:50
 */
public class CaseShuheUserAdaptee extends CaseShuheUser implements IToTransferSyncAdaptee {

    private final static String REGEX_DATE_TIME = "^(?:(?!0000)[0-9]{4}-(?:(?:0[1-9]|1[0-2])-(?:0[1-9]|1[0-9]|2[0-8])|(?:0[13-9]|1[0-2])-(?:29|30)|(?:0[13578]|1[02])-31)|(?:[0-9]{2}(?:0[48]|[2468][048]|[13579][26])|(?:0[48]|[2468][048]|[13579][26])00)-02-29)\\s+([0-1]?[0-9]|2[0-3]):([0-5][0-9]):([0-5][0-9])$";
    private final static Pattern COMPILE = Pattern.compile(REGEX_DATE_TIME);

    @Override
    public void adapteeRequest(MarketingTransferSyncUser transferSyncUser, String taskId) {
        transferSyncUser.setApiCode(this.getApiCode());
        transferSyncUser.setCustNum(this.getCustNum());
        transferSyncUser.setUserType(this.getUserType());
        transferSyncUser.setLoginTime(addMillisecond(this.getClcUsrFstLogTimAll()));
        String jsonStr = "{" + "\"is_turn\":\"" + this.getIsTurn() + "\"," +
                "\"is_black\":\"" + this.getIsBlack() + "\"," +
                "\"clc_usr_lst_app_sta_tim\":\"" + this.getClcUsrLstAppStaTim() + "\"," +
                "\"clc_usr_iso_pho_tim\":\"" + this.getClcUsrIsoPhoTim() + "\"," +
                "\"clc_usr_iso_idt_tim\":\"" + this.getClcUsrIsoIdtTim() + "\"," +
                "\"clc_usr_iso_crd_tim\":\"" + this.getClcUsrIsoCrdTim() + "\"," +
                "\"clc_usr_iso_inf_tim\":\"" + this.getClcUsrIsoInfTim() + "\"," +
                "\"taskId\":\"" + taskId + "\"," +
                "\"applyLoanTime\":\"" + this.getClcUsrFrtFqOrdTim() + "\"," +
                "\"cell\":\"" + this.getCell() + "\"" +
                "}";
        transferSyncUser.setReserveField1(jsonStr);
        transferSyncUser.setApplyTime(addMillisecond(this.getClcUsrIsoAtoTim()));
        transferSyncUser.setAuditTime(addMillisecond(this.getClcUsrAdtTimRcnLon()));
        transferSyncUser.setAuditAmount(this.getClcUsrAdtLmtItr());
        transferSyncUser.setLentTime(addMillisecond(this.getClcUsrFstLndTimCshBtHl()));
        transferSyncUser.setCreateTime(new Date());
    }

    @Override
    public void adapteeRequest(MarketingTransferSyncUser transferSyncUser, String taskId, ShuheTransferJsonDTO jsonDTO) {
        transferSyncUser.setApiCode(this.getApiCode());
        transferSyncUser.setCustNum(this.getCustNum());
        transferSyncUser.setUserType(this.getUserType());
        transferSyncUser.setLoginTime(addMillisecond(this.getClcUsrFstLogTimAll()));
        final Map<String, String> dataItem = jsonDTO.getDataItem();
        dataItem.put("taskId", taskId);
        dataItem.put("applyLoanTime", this.getClcUsrFrtFqOrdTim());
        dataItem.put("cell", this.getCell());
        transferSyncUser.setReserveField1(JSONObject.toJSONString(dataItem));
        transferSyncUser.setApplyTime(addMillisecond(this.getClcUsrIsoAtoTim()));
        // 2022年7月21日14:11:56 AuditTime 优先使用clc_usr_grp_zjy_csx_sjs_yzz_cqc_jxd_c2字段的值
        String auditTime = dataItem.getOrDefault("clc_usr_grp_zjy_csx_sjs_yzz_cqc_jxd_c2", null);
        transferSyncUser.setAuditTime(addMillisecond(StringUtils.isEmpty(auditTime) || "".equals(auditTime)
                ? dataItem.getOrDefault("clc_usr_lst_adt_apy_tim_hvy", "") : auditTime));
        transferSyncUser.setAuditAmount(this.getClcUsrAdtLmtItr());
        transferSyncUser.setLentTime(addMillisecond(this.getClcUsrFstLndTimCshBtHl()));
        transferSyncUser.setCreateTime(new Date());
    }

    private String addMillisecond(String dateStr) {
        if (StringUtils.isEmpty(dateStr)) {
            return dateStr;
        }
        try {
            if (COMPILE.matcher(dateStr).matches()) {
                return dateStr.concat(":000");
            }
        } catch (Exception ignored) {
        }
        return dateStr;
    }
}
