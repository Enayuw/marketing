package com.br.marketing.service;

import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.dto.customer.CallRecordBO;
import com.br.marketing.dto.customer.CallRecordDTO;
import com.br.marketing.dto.customer.SmsRecordDTO;

public interface ZnkfPushService {
    /**
     * 客服推送营销拨打记录 回调接口
     * @param dto
     * @return
     */
    String znkfPushCallBack(CallRecordDTO dto);

    /**
     * 判断是否符合情况b：userType=促申完 && intentionGrade="A级(有明确意向）" && cusNun && 有效期内
     * @param dto
     * @return
     */
    Boolean isSatisfyPushDX(CallRecordBO dto) throws IllegalAccessException;

    /**
     * 判断案件编号是否为当天首次传输
     * @param key
     * @return
     */
    Boolean cusNumIsFirstToday(String key);

    /**
     *智能客服推送宜信黑名单结束标识
     * @param apiCode，pushDate
     * @return
     */
    ApiResult znkfPushBlackPhoneMark(String apiCode, String pushDate);


    /**
     *智能客服推送宜信黑名单结束标识
     * @param apiCode
     * @param pushDate(yyyy-MM-dd)
     * @return
     */
    Boolean isPushBlackPhoneEnd(String apiCode, String pushDate);

    String smsCallBack(SmsRecordDTO dto);
}
