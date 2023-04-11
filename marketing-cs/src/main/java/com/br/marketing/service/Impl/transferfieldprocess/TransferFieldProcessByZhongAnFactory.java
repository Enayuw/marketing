package com.br.marketing.service.Impl.transferfieldprocess;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.entity.MarketingSyncUser;
import com.br.marketing.entity.MarketingTransferSyncUser;
import com.br.marketing.mapper.MarketingSyncUserMapper;
import com.br.marketing.service.TransferFieldProcessFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;

@Service
public class TransferFieldProcessByZhongAnFactory implements TransferFieldProcessFactory {

    @Resource
    MarketingSyncUserMapper syncUserMapper;

    final static String cKey = "initCustNum";

    final static String uKey = "initUserType";

    final static String tKey = "uploadCreateTime";

    @Override
    public String customerName() {
        return "zhongan";
    }

    @Override
    public void fieldProcess(MarketingTransferSyncUser transferSyncUser) {
        String transferSyncUserCustNum = transferSyncUser.getCustNum();
        String transferSyncUserUserType = transferSyncUser.getUserType();
        MarketingSyncUser syncUser = syncUserMapper.selectSynsUserByCustNumLast(transferSyncUser.getApiCode(), transferSyncUserCustNum);
        if (syncUser != null) {
            transferSyncUser.setCustNum(syncUser.getCustNum());
            transferSyncUser.setUserType(syncUser.getUserType());

            JSONObject jb = new JSONObject();
            if (StringUtils.isNotBlank(transferSyncUser.getReserveField1())) {
                try {
                    jb = JSON.parseObject(transferSyncUser.getReserveField1());
                } catch (Exception ex) {
                    jb = null;
                }
            }
            if (jb == null) {
                String content = String.format("\"%s\":\"%s\",\"%s\":\"%s\",\"%s\":\"%s\""
                        , cKey, transferSyncUserCustNum
                        , uKey, transferSyncUserUserType
                        , tKey, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(syncUser.getCreateTime()));
                String initContent = StringUtils.isNotBlank(transferSyncUser.getReserveField1()) ? transferSyncUser.getReserveField1().concat(",") : "";
                transferSyncUser.setReserveField1(initContent.concat(content));
            } else {
                jb.put(cKey, transferSyncUserCustNum);
                jb.put(uKey, transferSyncUserUserType);
                jb.put(tKey, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(syncUser.getCreateTime()));
                transferSyncUser.setReserveField1(jb.toJSONString());
            }
        }
    }
}
