package com.br.marketing.enums;

import lombok.Getter;

/**
 * 定制文件拉取类型
 *
 * @author senyang.zheng
 * @date 2024/10/18
 */
@Getter
public enum SyncConfigCustomizedTypeEnum {
    /**
     * 默认
     */
    DEFAULT(0),
    /**
     * <a href="https://c.100credit.cn/pages/viewpage.action?pageId=178649474">D20241015数禾促复借每日自动化匹配数据-3710043</a>
     */
    SHUHE_AUTO_MATCH_DATA(1),

    //同程cpa推送文件
    TC_CPA_PUSH_FILE(2);

    private final Integer code;

    SyncConfigCustomizedTypeEnum(Integer code) {
        this.code = code;
    }

    public static SyncConfigCustomizedTypeEnum getEnumByCode(Integer code) {
        for (SyncConfigCustomizedTypeEnum enumValue : SyncConfigCustomizedTypeEnum.values()) {
            if (enumValue.getCode().equals(code)) {
                return enumValue;
            }
        }
        // 根据Code未找到对应枚举返回默认值
        return SyncConfigCustomizedTypeEnum.DEFAULT;
    }
}
