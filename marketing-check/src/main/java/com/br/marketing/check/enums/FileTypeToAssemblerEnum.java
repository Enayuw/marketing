package com.br.marketing.check.enums;

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

    YILIAN_TRANSFER_CSV("yilian_transfer_csv", "csvToDbAssembler"),
    ;

    private String fileType;

    private String assemblerName;

    public static String getByFileType(String fileType){
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
}
