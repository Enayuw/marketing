package com.br.marketing.enums.clean;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 数据类型-表名枚举
 */

@Getter
@AllArgsConstructor
public enum DataProcessEnum {

    UPLOAD_DATA_GENERAL(DataTypeEnum.UPLOAD.getCode(), AcceptTypeEnum.GENERAL.getCode(), "b_marketing_sync_info"),
    UPLOAD_DATA_CUSTOM(DataTypeEnum.UPLOAD.getCode(), AcceptTypeEnum.CUSTOM.getCode(), "b_marketing_customer_original_data"),
    TRANSFORM_DATA_GENERAL(DataTypeEnum.TRANSFORM.getCode(), AcceptTypeEnum.GENERAL.getCode(), "b_marketing_transfer_info"),
    TRANSFORM_DATA_CUSTOM(DataTypeEnum.TRANSFORM.getCode(), AcceptTypeEnum.CUSTOM.getCode(), "b_marketing_customer_original_data");

    /**
     * Data type
     */
    private  Integer dataType;


    /**
     * Accept type
     */
    private final Integer acceptType;

    /**
     * Table name
     */
    private final String tableName;

    /**
     * Get enum by dataType and acceptType
     */
    public static DataProcessEnum getByTypes(Integer dataType, Integer acceptType) {
        for (DataProcessEnum value : values()) {
            if (value.getDataType().equals(dataType) && value.getAcceptType().equals(acceptType)) {
                return value;
            }
        }
        return null;
    }

    /**
     * Data type enum: UPLOAD(0), TRANSFORM(1)
     */
    @Getter
    @AllArgsConstructor
    public enum DataTypeEnum {
        UPLOAD(0, "上传"),
        TRANSFORM(1, "转化");

        private  Integer code;
        private  String desc;
    }

    /**
     * Accept type enum: GENERAL(0), CUSTOM(1)
     */
    @Getter
    @AllArgsConstructor
    public enum AcceptTypeEnum {
        GENERAL(0, "通用"),
        CUSTOM(1, "定制"),
        FTP(2, "FTP");

        private  Integer code;
        private  String desc;
    }

    /**
     * accountType type enum: GENERAL(0), CUSTOM(1)
     */
    @Getter
    @AllArgsConstructor
    public enum AccountTypeEnum {
        GENERAL(0, "测试"),
        CUSTOM(1, "正式");

        private  Integer code;
        private  String desc;
    }
}
