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
            List<File> extractedFileList = new ArrayList<>();
            for (FileHeader fileHeader : headerList) {
                if (!fileHeader.isDirectory()) {
                    extractedFileList.add(new File(destDir, fileHeader.getFileName()));
                }
            }
            for (File f : extractedFileList) {
                log.info(  "{} 文件解压成功!",f.getAbsolutePath());
            }
        } catch (ZipException e) {
            log.error("解压加密压缩文件出错",e);
            throw new RuntimeException("解压加密压缩文件出错", e);
        }
    }

    /**
     * 解压 zip 到指定目录，并返回解压出的文件相对路径列表（不含目录）
     *
     * @param zipFile  压缩包文件
     * @param dest     解压目标目录（与压缩包同级目录时，解压文件在该目录下）
     * @param password 解压密码，可为空
     * @param charset  文件名编码，如 "GBK"、"UTF-8"；为空时使用 GBK
     * @return 解压出的文件相对 dest 的路径列表，如 ["a.txt", "sub/b.txt"]
     */
    public static List<String> unZipAndReturnExtractedPaths(File zipFile, String dest, String password, String charset) {
        try {
            ZipFile zFile = new ZipFile(zipFile);
            zFile.setFileNameCharset(charset != null && !charset.isEmpty() ? charset : "GBK");
            List<FileHeader> headerList = zFile.getFileHeaders();
            List<String> extractedPaths = new ArrayList<>();
            for (FileHeader fileHeader : headerList) {
                if (!fileHeader.isDirectory()) {
                    extractedPaths.add(fileHeader.getFileName());
                }
            }
            File destDir = new File(dest);
            if (!destDir.exists() && !destDir.mkdirs()) {
                log.error("mkdir error {}", dest);
            }
            if (zFile.isEncrypted() && password != null && !password.isEmpty()) {
                zFile.setPassword(password.toCharArray());
            }
            zFile.extractAll(dest);
            for (String path : extractedPaths) {
                log.info("文件解压成功: {}", new File(destDir, path).getAbsolutePath());
            }
            return extractedPaths;
        } catch (ZipException e) {
            log.error("解压文件出错", e);
            throw new RuntimeException("解压文件出错", e);
        }
    }
}
