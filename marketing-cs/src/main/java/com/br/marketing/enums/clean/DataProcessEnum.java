package com.br.marketing.enums.clean;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 数据类型-表名枚举
 */

@Getter
@AllArgsConstructor
public enum DataProcessEnum {

    UPLOAD_DATA_GENERAL(SystemTypeEnum.MARKETING.getCode(),
            DataTypeEnum.UPLOAD.getCode(),
            AcceptTypeEnum.GENERAL.getCode(),
            "b_marketing_sync_info"),
    UPLOAD_DATA_CUSTOM(SystemTypeEnum.MARKETING.getCode(),
            DataTypeEnum.UPLOAD.getCode(),
            AcceptTypeEnum.CUSTOM.getCode(),
            "b_marketing_customer_original_data"),
    TRANSFORM_DATA_GENERAL(SystemTypeEnum.MARKETING.getCode(),
            DataTypeEnum.TRANSFORM.getCode(),
            AcceptTypeEnum.GENERAL.getCode(),
            "b_marketing_transfer_info"),
    TRANSFORM_DATA_CUSTOM(SystemTypeEnum.MARKETING.getCode(),
            DataTypeEnum.TRANSFORM.getCode(),
            AcceptTypeEnum.CUSTOM.getCode(),
            "b_marketing_customer_original_data");

    /**
     * System type
     */
    private Integer systemType;

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
    public static DataProcessEnum getByTypes(Integer systemType, Integer dataType, Integer acceptType) {
        for (DataProcessEnum value : values()) {
            if (value.getSystemType().equals(systemType) && value.getDataType().equals(dataType) && value.getAcceptType().equals(acceptType)) {
                return value;
            }
        }
        return null;
    }

    @Getter
    @AllArgsConstructor
    public enum SystemTypeEnum{
        MARKETING(0,"营销中台"),
        CALL(1,"外呼系统");

        private  Integer code;
        private  String desc;
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

    /**
     * 规则状态
     */
    @Getter
    @AllArgsConstructor
    public enum RuleStatusEnum {
        READY(0, "待生效"),
        PRE_SUCCESS(1, "试跑成功");

        private  Integer code;
        private  String desc;
    }

    /**
     * 清洗文件状态
     */
    @Getter
    @AllArgsConstructor
    public enum FileStatusEnum {
        READY(0, "待开始"),
        RUNNING(1, "清洗中"),
        SUCCESS(2, "清洗完成"),
        FAIL(3, "失败");


        private  Integer code;
        private  String desc;
    }



}
