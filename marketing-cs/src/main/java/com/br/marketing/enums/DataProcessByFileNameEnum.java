package com.br.marketing.enums;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.EnumSet;

/**
 * @Description EnumLogErrorCodeType
 * @Author hong.chen
 * @CreateTime 2023/11/17
 */
public enum DataProcessByFileNameEnum {
    ZHONGBANG_SYNC_CAIFU("original_caifu_", 1, "众邦上传数据文件-财富"),
    ZHONGBANG_SYNC_DAIKUAN("original_daikuan_", 2, "众邦上传数据文件-信贷"),
    ZHONGBANG_TRANS("transform_", 3, "众邦转化数据文件");

    private final String fileNamePrefix;
    private final Integer order;
    private final String desc;
    private String url;


//    static {
//        // 根据 fileNamePrefix 初始化 URL
//        for (DataProcessByFileNameEnum enumValue : values()) {
//            if ("original_caifu_".equals(enumValue.fileNamePrefix)) {
//                enumValue.url = uploadUrl;
//            } else if ("original_daikuan_".equals(enumValue.fileNamePrefix)) {
//                enumValue.url = uploadUrl;
//            } else if ("transform_".equals(enumValue.fileNamePrefix)) {
//                enumValue.url = transferUrl;
//            }
//        }
//    }

    @Component
    public static class DataProcessByFileNameEnumInjector {
        @Value("${api.marketing.transferUrl:00}")
        private String transferUrl;

        @Value("${api.marketing.uploadUrl:00}")
        private String uploadUrl;

        @PostConstruct
        private void init() {
            for (DataProcessByFileNameEnum enumValue : EnumSet.allOf(DataProcessByFileNameEnum.class)) {
//            for (DataProcessByFileNameEnum enumValue : DataProcessByFileNameEnum.values()) {
                if (ZHONGBANG_SYNC_CAIFU.getFileNamePrefix().equals(enumValue.getFileNamePrefix())) {
                    enumValue.setUrl(uploadUrl);
                } else if (ZHONGBANG_SYNC_DAIKUAN.getFileNamePrefix().equals(enumValue.getFileNamePrefix())) {
                    enumValue.setUrl(uploadUrl);
                } else if (ZHONGBANG_TRANS.getFileNamePrefix().equals(enumValue.getFileNamePrefix())) {
                    enumValue.setUrl(transferUrl);
                }
            }
        }
    }


    DataProcessByFileNameEnum(String fileNamePrefix, Integer order, String desc) {
        this.fileNamePrefix = fileNamePrefix;
        this.order = order;
        this.desc = desc;
    }

    public String getFileNamePrefix() {
        return fileNamePrefix;
    }

    public Integer getOrder() {
        return order;
    }

    public String getDesc() {
        return desc;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    //    public static DataProcessByFileNameEnum getEnumByFileName(String fileName) {
//        for (DataProcessByFileNameEnum dataProcessByFileNameEnum : DataProcessByFileNameEnum.values()) {
//            if (fileName.startsWith(dataProcessByFileNameEnum.getFileNamePrefix())) {
//                return dataProcessByFileNameEnum;
//            }
//        }
//    }

}
