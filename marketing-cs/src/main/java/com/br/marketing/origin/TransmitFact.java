package com.br.marketing.origin;

import com.br.marketing.entity.MarketingTransferSyncUser;
import lombok.Data;

/**
 * code is far away from bug with the animal protecting
 * ┏┓　　　┏┓
 * ┏┛┻━━━┛┻┓
 * ┃　　　　　　　┃
 * ┃　　　━　　　┃
 * ┃　┳┛　┗┳　┃
 * ┃　　　　　　　┃
 * ┃　　　┻　　　┃
 * ┃　　　　　　　┃
 * ┗━┓　　　┏━┛
 * 　　┃　　　┃神兽保佑
 * 　　┃　　　┃代码无BUG！
 * 　　┃　　　┗━━━┓
 * 　　┃　　　　　　　┣┓
 * 　　┃　　　　　　　┏┛
 * 　　┗┓┓┏━┳┓┏┛
 * 　　　┃┫┫　┃┫┫
 * 　　　┗┻┛　┗┻┛
 *
 * @Description : 不同数据来源对象封装到 统一对象上，便于参数传输
 * ---------------------------------
 * @Author : jilong.xu
 * @Date : Create in 2022/3/12 15:28
 */

@Data
public class TransmitFact {

    private MarketingTransferSyncUser marketingTransferSyncUser;

    public TransmitFact(){}

    public TransmitFact(MarketingTransferSyncUser transferSyncUser){
        this.marketingTransferSyncUser = transferSyncUser;
    }
}
