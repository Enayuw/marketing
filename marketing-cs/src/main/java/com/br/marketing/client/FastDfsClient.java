package com.br.marketing.client;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import javax.annotation.Resource;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableMBeanExport;
import org.springframework.context.annotation.Import;
import org.springframework.jmx.support.RegistrationPolicy;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.github.tobato.fastdfs.FdfsClientConfig;
import com.github.tobato.fastdfs.domain.fdfs.StorePath;
import com.github.tobato.fastdfs.service.FastFileStorageClient;

import lombok.extern.slf4j.Slf4j;

/**
 * fastdfs客户端
 *
 * @author senyang.zheng
 * @date 2024/08/15
 */
@Configuration
@Import(FdfsClientConfig.class)
@EnableMBeanExport(registration = RegistrationPolicy.IGNORE_EXISTING)
@Component
@Slf4j
public class FastDfsClient {
    @Resource
    private FastFileStorageClient storageClient;

    /**
     * 上传文件（MultipartFile）
     *
     * @param file 文件
     * @return {@link String }
     * @throws IOException ioexception
     * @author senyang.zheng
     * @date 2024/08/15
     */
    public String uploadFile(MultipartFile file) throws IOException {
        log.warn("FileClient->uploadFile,file={}", file);
        String extension = getExtension(file);
        StorePath storePath = storageClient.uploadFile(file.getInputStream(), file.getSize(), extension, null);
        return getResAccessUrl(storePath);
    }

    /**
     * 获取文件
     *
     * @param file 文件
     * @return {@link String }
     * @author senyang.zheng
     * @date 2024/08/15
     */
    public static String getExtension(MultipartFile file) {
        String trimStr = StringUtils.trim(file.getOriginalFilename());
        return FilenameUtils.getExtension(trimStr);
    }

    /**
     * 上传文件（Stream）
     *
     * @param inputStream 输入流
     * @param fileSize 文件大小
     * @param originalFilename 原始文件名
     * @return {@link String }
     * @author senyang.zheng
     * @date 2024/08/15
     */
    public String uploadFile(InputStream inputStream, Long fileSize, String originalFilename) {
        StorePath storePath = storageClient.uploadFile(inputStream, fileSize, FilenameUtils.getExtension(StringUtils.trim(originalFilename)), null);
        return getResAccessUrl(storePath);
    }

    /**
     * 上传文件（File）
     *
     * @param file 文件
     * @return {@link String }
     * @throws IOException ioexception
     * @author senyang.zheng
     * @date 2024/08/15
     */
    public String uploadFile(File file) throws IOException {
        String extension = FilenameUtils.getExtension(StringUtils.trim(file.getName()));
        // 默认为"txt"
        if (StringUtils.isEmpty(extension)) {
            extension = "txt";
        }
        StorePath storePath = storageClient.uploadFile(Files.newInputStream(file.toPath()), file.length(), extension, null);
        return getResAccessUrl(storePath);
    }

    /**
     * 将一段字符串生成一个文件上传
     *
     * @param content 文件内容
     * @param fileExtension 文件后缀
     * @return {@link String }
     * @author senyang.zheng
     * @date 2024/08/15
     */
    public String uploadFile(String content, String fileExtension) {
        byte[] buff = content.getBytes(StandardCharsets.UTF_8);
        ByteArrayInputStream stream = new ByteArrayInputStream(buff);
        StorePath storePath = storageClient.uploadFile(stream, buff.length, fileExtension, null);
        return getResAccessUrl(storePath);
    }

    /**
     * 下载完整文件
     *
     * @param url url
     * @return {@link byte[] }
     * @throws IOException ioexception
     * @author senyang.zheng
     * @date 2024/08/15
     */
    public byte[] downloadFile(String url) throws IOException {
        StorePath storePath = StorePath.parseFromUrl(url);
        try (InputStream ins = storageClient.downloadFile(storePath.getGroup(), storePath.getPath(), ins1 -> ins1);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
            byte[] buff = new byte[100];
            int rc;
            while ((rc = ins.read(buff, 0, 100)) > 0) {
                byteArrayOutputStream.write(buff, 0, rc);
            }
            return byteArrayOutputStream.toByteArray();
        }
    }

    /**
     * 封装图片完整URL地址
     *
     * @param storePath 存储路径
     * @return {@link String }
     * @author senyang.zheng
     * @date 2024/08/15
     */
    private String getResAccessUrl(StorePath storePath) {
        return "/" + storePath.getFullPath();
    }

    /**
     * 传图片并同时生成一个缩略图 "JPG", "JPEG", "PNG", "GIF", "BMP", "WBMP"
     *
     * @param file 文件对象
     * @return {@link String }
     * @throws IOException ioexception
     * @author senyang.zheng
     * @date 2024/08/15
     */
    public String uploadImageAndCrtThumbImage(MultipartFile file) throws IOException {
        StorePath storePath = storageClient.uploadImageAndCrtThumbImage(file.getInputStream(), file.getSize(),
            FilenameUtils.getExtension(file.getOriginalFilename()), null);
        return getResAccessUrl(storePath);
    }

    /**
     * 删除文件
     *
     * @param fileUrl 文件url
     * @return boolean
     * @author senyang.zheng
     * @date 2024/08/15
     */
    public boolean deleteFile(String fileUrl) {
        if (StringUtils.isEmpty(fileUrl)) {
            log.error("删除文件异常,fileUrl is empty");
            return true;
        }
        try {
            StorePath storePath = StorePath.parseFromUrl(fileUrl);
            storageClient.deleteFile(storePath.getGroup(), storePath.getPath());
        } catch (Exception e) {
            log.error("删除文件异常：", e);
            return false;
        }
        return true;
    }
}
