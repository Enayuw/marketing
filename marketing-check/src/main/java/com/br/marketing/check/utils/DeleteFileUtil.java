package com.br.marketing.check.utils;

import com.br.marketing.common.utils.Constants;
import com.br.marketing.common.utils.DateHelper;
import com.br.marketing.common.utils.file.FtpUtil2;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.regex.Pattern;

@Slf4j
public class DeleteFileUtil {
    private static final Pattern MYREGEX = Pattern.compile("\\.");
    private static final Pattern REMARK_REGEX = Pattern.compile(Constants.DELETE_MONIZTOR_REMARK);

    /**
     * 校验剔除文件名称是否正确
     * @param fileName 剔除文件名称
     * @param apiCode apiCode
     */
    public static boolean vaildFileName(String fileName, String apiCode,StringBuilder errorMessage) {
        if(errorMessage==null){
            return false;
        }
        if(StringUtils.isNotEmpty(fileName)){
            String[] s = fileName.split("\\.");
            if(s.length<2){
                errorMessage.append("文件名称命名异常");
                return false;
            }
            String name = s[0];
            String[] s1 = name.split("_");
            if(s1.length!=4){
                errorMessage.append("文件名称命名异常");
                return false;
            }else{
                if(!apiCode.equals(s1[0])){
                    errorMessage.append("apicode异常");
                    return false;
                }else if(!REMARK_REGEX.matcher(s1[1]).matches()){
                    errorMessage.append("文件批次命名异常");
                    return false;
                }else if(!"DeleteMonitor".equals(s1[2])){
                    errorMessage.append("文件名称命名异常");
                    return false;
                }else {
                    String s2 = s1[3];
                    try {
                        DateHelper.parseDate(s2);
                    }catch (IllegalArgumentException e){
                        log.error("日期异常",e);
                        errorMessage.append("日期异常");
                        return false;
                    }
                }
            }
        }
        return true;
    }

}
