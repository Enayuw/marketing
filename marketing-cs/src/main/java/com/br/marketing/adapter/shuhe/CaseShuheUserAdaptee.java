package com.br.marketing.adapter.shuhe;

import com.br.marketing.entity.CaseShuheUserWithBLOBs;
import com.br.marketing.entity.MarketingTransferSyncUser;

import java.util.Date;

/**
 * 数禾适配者
 *
 * @author Guo Zeqiang
 * @dateTime 2022/3/16 16:50
 */
public class CaseShuheUserAdaptee extends CaseShuheUserWithBLOBs implements IToTransferSyncAdaptee {

    private static final long serialVersionUID = -6001852261613858955L;

    @Override
    public void adapteeRequest(MarketingTransferSyncUser transferSyncUser, String taskId) {
        transferSyncUser.setApiCode(this.getApiCode());
        transferSyncUser.setCustNum(this.getCustNum());
        transferSyncUser.setUserType(this.getUserType());
        transferSyncUser.setLoginTime(this.getClcUsrFstLogTimAll());
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
        transferSyncUser.setApplyTime(this.getClcUsrIsoAtoTim());
        transferSyncUser.setAuditTime(this.getClcUsrAdtTimRcnLon());
        transferSyncUser.setAuditAmount(this.getClcUsrAdtLmtItr());
        transferSyncUser.setLentTime(this.getClcUsrFstLndTimCshBtHl());
        transferSyncUser.setCreateTime(new Date());
    }
}
