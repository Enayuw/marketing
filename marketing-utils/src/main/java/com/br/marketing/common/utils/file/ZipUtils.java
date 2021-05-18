package com.br.marketing.common.utils.file;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import lombok.extern.slf4j.Slf4j;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.FileHeader;
@Slf4j
public class ZipUtils {

    /**
     * @param zipFile 原始文件
     * @param dest 解压路径
     * @param password 解压文件密码(可以为空)
     */
    public static void unZip(File zipFile,String dest,String password){
        try {
            ZipFile zFile = new ZipFile(zipFile);
            zFile.setFileNameCharset("GBK");
            File destDir = new File(dest);
            if (!destDir.exists()) {
                boolean mkdirs = destDir.mkdirs();
                if(!mkdirs){
                    log.error("mkd error {}",dest);
                }
            }
            if (zFile.isEncrypted()) {
                zFile.setPassword(password.toCharArray());
            }
            zFile.extractAll(dest);
            List<FileHeader> headerList = zFile.getFileHeaders();
            List<File> extractedFileList = new ArrayList<File>();
            for (FileHeader fileHeader : headerList) {
                if (!fileHeader.isDirectory()) {
                    extractedFileList.add(new File(destDir, fileHeader.getFileName()));
                }
            }
            File[] extractedFiles = new File[extractedFileList.size()];
            extractedFileList.toArray(extractedFiles);
            for (File f : extractedFileList) {
                log.info(  "{} 文件解压成功!",f.getAbsolutePath());
            }
        } catch (ZipException e) {
            log.error("解压加密压缩文件出错",e);
            throw new RuntimeException("解压加密压缩文件出错", e);
        }
    }
}
