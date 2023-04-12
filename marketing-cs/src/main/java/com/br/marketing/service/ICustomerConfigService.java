package com.br.marketing.service;

import com.br.marketing.common.commondto.Result;
import com.br.marketing.enums.ThreeKeyEncryptEnum;
import com.br.marketing.enums.ThreeKeyTypeEnum;

public interface ICustomerConfigService {
    /**
     * 获取加密类型 1-MD5;2-SHA256;
     * @param apiCode
     * @return
     */
    Result<Integer> getEncryptyType(String apiCode);

    /**
     * 修改加密方式
     * @param apiCode
     * @param type
     * @return
     */
    Result updateEncryptyType(String apiCode,Integer type);

    /**
     * 3k的摘要数据转换为log加密数据
     * 调用该方法 要求客户的配置表中 有配置客户的加密方式
     * @param apiCode
     * @param content （摘要加密内容）
     * @param threeKeyTypeEnum （加密）
     * @return
     */
    Result<String> getThreeKeyDigToLog(String apiCode, String content, ThreeKeyTypeEnum threeKeyTypeEnum);


    /**
     * 3k的log加密数据转换为摘要数据
     * 调用该方法 要求客户的配置表中 有配置客户的加密方式
     * @param apiCode
     * @param content （log加密内容）
     * @return
     */
    Result<String> getThreeKeyLogToDig(String apiCode, String content);
}
