package com.br.marketing.bridge.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * FileTypeToAssemblerEnum
 *
 * @author xiang.li
 * @date 2024/01/22
 */
@Getter
@AllArgsConstructor
public enum FileTypeToAssemblerEnum {

    TRANSFER_CSV_COMMON("transfer_csv_common", "csvToDbAssembler", "1"),
    SYJ_ORIGINAL("syj_original", "", "1"),
    ;

    private String fileType;

    private String assemblerName;

    private String pushStatusMark;

    public static String getAssemblerByFileType(String fileType){
        if(StringUtils.isEmpty(fileType)){
            return "";
        }
        for (FileTypeToAssemblerEnum e: FileTypeToAssemblerEnum.values()) {
            if (e.getFileType().equals(fileType)) {
                return e.getAssemblerName();
            }
        }
        return "";
    }

    public static String getPushStatusMarkByFileType(String fileType){
        if(StringUtils.isEmpty(fileType)){
            return "0";
        }
        for (FileTypeToAssemblerEnum e: FileTypeToAssemblerEnum.values()) {
            if (e.getFileType().equals(fileType)) {
                return e.getPushStatusMark();
            }
        }
        return "0";
    }
}
