package com.br.marketing.dto.shuhe;

import java.io.Serializable;
import java.util.Map;

/**
 * 数禾数据
 *
 * @author Guo Zeqiang
 * @dateTime 2022/2/10 15:19
 */
public class ShuheTransferJsonDTO implements Serializable {
    private static final long serialVersionUID = -7402540325274471035L;
    /**
     * 2022/2/10 15:22 批量上传案件编号
     */
    private String orderId;
    /**
     * 2022/2/10 15:22 手机号
     */
    private String mobile;
    /**
     * 2022/2/10 15:22 业务类型
     */
    private String bizType;
    /**
     * 2022/2/10 15:22 扩展字段,业务对应关系 见【业务类型关系映射】
     * <p>
     * is_turn	String	是否已经转人工		是-'Y'，否-'N'
     * is_black	String	是否黑名单		是-'Y'，否-'N'
     * clc_usr_fst_log_tim_all	String	首登时间		yyyy-MM-dd HH:mm:ss （促首登）
     * clc_usr_lst_app_sta_tim	String	最近一次登录时间		yyyy-MM-dd HH:mm:ss
     * clc_usr_iso_pho_tim	String	最近一次人脸识别完成（开始）时间		yyyy-MM-dd HH:mm:ss
     * clc_usr_iso_idt_tim	String	最近一次身份验证完成时间		yyyy-MM-dd HH:mm:ss
     * clc_usr_iso_crd_tim	String	最近一次绑卡完成时间		yyyy-MM-dd HH:mm:ss
     * clc_usr_iso_inf_tim	String	最新一次个人信息验证完成时间		yyyy-MM-dd HH:mm:ss
     * clc_usr_iso_ato_tim	String	最近一次申完时间		yyyy-MM-dd HH:mm:ss （促申完）
     * clc_usr_adt_tim_rcn_lon	String	最近一次授信时间		yyyy-MM-dd HH:mm:ss
     * clc_usr_adt_lmt_itr	String	用户授信额度区间		格式：[0,3000] 返回3k-
     * (3000,5000] 返回3-5k
     * (5000,10000] 返回5-10k
     * (10000,20000]返回10-20k
     * (20000,30000]返回20-30k
     * (30000,+∞]返回30k+
     * clc_usr_frt_fq_ord_tim	String	用户首次发起借款时间		yyyy-MM-dd HH:mm:ss（促首借）
     * clc_usr_fst_lnd_tim_csh_bt_hl	String	用户首次借款成功时间		yyyy-MM-dd HH:mm:ss
     */
    private Map<String, String> dataItem;

    /* 2022/2/10 15:33 业务类型关系映射
     业务类型	字段	                       类型	    中文释义	                        返回值类型	                是否对应业务类型中的结案状态	业务负责人
     共性参数	is_turn	                       String	是否已经转人工	                是-'Y'，否-'N'		                                    严晨/宫铭萱
	            is_black	                   String	是否黑名单	                    是-'Y'，否-'N'

     促首登	    clc_usr_fst_log_tim_all	       String	首登时间	                        yyyy-MM-dd HH:mm:ss （促首登）	是

     促申完	    clc_usr_lst_app_sta_tim	       String	最近一次登录时间	                yyyy-MM-dd HH:mm:ss
	            clc_usr_iso_pho_tim	           String	最近一次人脸识别完成（开始）时间	yyyy-MM-dd HH:mm:ss
	            clc_usr_iso_idt_tim	           String	最近一次身份验证完成时间	        yyyy-MM-dd HH:mm:ss
	            clc_usr_iso_crd_tim	           String	最近一次绑卡完成时间	            yyyy-MM-dd HH:mm:ss
	            clc_usr_iso_inf_tim	           String	最新一次个人信息验证完成时间	    yyyy-MM-dd HH:mm:ss
	            clc_usr_iso_ato_tim	           String	最近一次申完时间	                yyyy-MM-dd HH:mm:ss （促申完）	是
	            clc_usr_adt_tim_rcn_lon	       String	最近一次授信时间	                yyyy-MM-dd HH:mm:ss

     促首借	    clc_usr_adt_lmt_itr	           String	用户授信额度区间	                格式：[0,3000] 返回 3k-		                            姜度
				                                                                             (3000,5000] 返回 3-5k
				                                                                             (5000,10000] 返回 5-10k
				                                                                             (10000,20000]返回 10-20k
				                                                                             (20000,30000]返回 20-30k
				                                                                             (30000,+∞]返回 30k+
	            clc_usr_frt_fq_ord_tim	       String	用户首次发起借款时间	             yyyy-MM-dd HH:mm:ss（促首借）	是
	            clc_usr_fst_lnd_tim_csh_bt_hl  String	用户首次借款成功时间	             yyyy-MM-dd HH:mm:ss


     * 业务场景枚举值：促审完、促首登、促动支、促首借、促复借、轻资产、重申
     */

    public ShuheTransferJsonDTO() {
    }

    public ShuheTransferJsonDTO(String orderId, String mobile, String bizType, Map<String, String> dataItem) {
        this.orderId = orderId;
        this.mobile = mobile;
        this.bizType = bizType;
        this.dataItem = dataItem;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getBizType() {
        return bizType;
    }

    public void setBizType(String bizType) {
        this.bizType = bizType;
    }

    public Map<String, String> getDataItem() {
        return dataItem;
    }

    public void setDataItem(Map<String, String> dataItem) {
        this.dataItem = dataItem;
    }

    @Override
    public String toString() {
        return "ShuheTransferJsonDTO{" +
                "orderId='" + orderId + '\'' +
                ", mobile='" + mobile + '\'' +
                ", bizType='" + bizType + '\'' +
                ", dataItem=" + dataItem +
                '}';
    }
}
