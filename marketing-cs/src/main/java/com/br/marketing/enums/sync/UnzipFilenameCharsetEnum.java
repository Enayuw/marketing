package com.br.marketing.enums.sync;

import com.br.marketing.common.utils.StringUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * b_sync_config.unzip_filename_charset 解压时 ZIP 内文件名的字符集（与 {@link com.br.marketing.common.utils.file.ZipUtils} 一致）
 */
@Getter
@AllArgsConstructor
public enum UnzipFilenameCharsetEnum {

    GBK("GBK"),
    UTF_8("UTF-8");

    /** 写入库、传给 ZipUtils 的编码名 */
    private final String charsetName;

    public static UnzipFilenameCharsetEnum fromName(String name) {
        if (StringUtils.isBlank(name)) {
            return null;
        }
        String t = name.trim();
        for (UnzipFilenameCharsetEnum e : values()) {
            if (e.charsetName.equalsIgnoreCase(t)) {
                return e;
            }
        }
        return null;
    }

    /** 与库表 DEFAULT 'GBK' 一致 */
    public static String defaultIfBlank(String charset) {
        if (StringUtils.isBlank(charset)) {
            return GBK.getCharsetName();
        }
        return charset.trim();
    }
}
