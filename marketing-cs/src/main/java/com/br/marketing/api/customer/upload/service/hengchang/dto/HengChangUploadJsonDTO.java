package com.br.marketing.api.customer.upload.service.hengchang.dto;

import com.alibaba.fastjson.JSONArray;
import com.br.marketing.api.customer.upload.adapter.BaseUploadDataAdaptee;
import com.br.marketing.dto.MarketingPreUserDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @ClassName HengChangUploadJsonDTO
 * @Description 恒昌定制上传数据json
 * @Author kongbx
 * @Date 2025/1/3 15:31
 */
@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
public class HengChangUploadJsonDTO extends BaseUploadDataAdaptee<MarketingPreUserDTO> {

    /**
     * 任务ID
     */
    private String taskCode;
    /**
     * 批次ID
     */
    private String batchId;

    /**
     * 营销客群
     */
    private Integer userType;

    /**
     * 经营周期
     */
    private String marketingTime;

    private String requestNo;

    /**
     * 客户名单列表
     * uniqueId	String	是	用户唯一标识
     * phone	String	是	手机号
     * name	String	否	姓名
     * registerTime	String	是	注册时间
     * creditGrantingTime	String	否	授信时间
     * settlementTime	String	否	最近一次结清时间
     * creditBalance	Long	否	可用额度
     * lastLoginTime	String	否	最近一次登录时间
     * extra	String	否	预留字段
     */
    private JSONArray userInfoList;

    /**
     * @param apiCode
     * @param jsonData
     * @return
     */
    @Override
    protected MarketingPreUserDTO adapteeRequest(String apiCode, String jsonData) {
        return new MarketingPreUserDTO();
    }

}
