package com.br.marketing.sync.service.impl;

import io.minio.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.InputStream;

@Slf4j
@Service
public class MinioFileService {

    private static final String LOG_PREFIX = "[MinIO] ";

    private MinioClient minioClient;

    @Value("${minio.endpoint}")
    private String endpoint;

    @Value("${minio.accessKey}")
    private String accessKey;

    @Value("${minio.secretKey}")
    private String secretKey;

    @Value("${minio.bucketName}")
    private String bucketName;

    private boolean initialized = false;

    /**
     * 延迟初始化 MinioClient，只在实际使用时才连接
     */
    private boolean ensureInitialized() {
        if (!initialized) {
            synchronized (this) {
                if (!initialized) {
                    try {
                        log.warn(LOG_PREFIX + "Initializing MinIO client for endpoint: {}", endpoint);
                        this.minioClient = MinioClient.builder()
                                .endpoint(endpoint)
                                .credentials(accessKey, secretKey)
                                .build();
                        createBucketIfNotExists();
                        initialized = true;
                        log.warn(LOG_PREFIX + "MinIO client initialized successfully. accessKey={}, secretKey={}", accessKey, secretKey);
                    } catch (Exception e) {
                        log.error(LOG_PREFIX + "Failed to initialize MinIO client: {}", e.getMessage(), e);
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private void createBucketIfNotExists() {
        try {
            boolean exists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
            if (!exists) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
                log.warn(LOG_PREFIX + "Bucket '{}' created successfully.", bucketName);
            } else {
                log.warn(LOG_PREFIX + "Bucket '{}' already exists.", bucketName);
            }
        } catch (Exception e) {
            log.error(LOG_PREFIX + "创建bucket失败", e);
            if (e.getMessage() != null && e.getMessage().contains("InvalidAccessKeyId")) {
                log.warn(LOG_PREFIX + "No permission to check bucket existence, assuming bucket '{}' exists", bucketName);
            }
        }
    }
    /**
     * 通过 localFilePath 上传文件到 MinIO
     * @param localFilePath 本地路径
     * @param objectName  对象名称（MinIO 中的路径）
     * @return 是否上传成功
     */
    public boolean uploadFile(String localFilePath, String objectName) {
        if (!ensureInitialized()) {
            log.error(LOG_PREFIX + "Client not initialized, cannot upload file");
            return false;
        }

        try {
            File file = new File(localFilePath);
            if (!file.exists()) {
                log.error(LOG_PREFIX + "File not found: {}", localFilePath);
                return false;
            }
            minioClient.uploadObject(
                    UploadObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .filename(localFilePath)
                            .build());
            log.warn(LOG_PREFIX + "File uploaded successfully: {}", objectName);
            return true;
        } catch (Exception e) {
            log.error(LOG_PREFIX + "Failed to upload file {}: {}", objectName, e.getMessage(), e);
            return false;
        }
    }

    /**
     * 获取对象信息，用于验证文件是否存在及获取元数据
     * @param objectName 对象名称
     * @return StatObjectResponse，包含大小、ETag等信息；如果不存在返回 null
     */
    public StatObjectResponse getObjectInfo(String objectName) {
        if (!ensureInitialized()) {
            log.error(LOG_PREFIX + "Client not initialized, cannot get object info");
            return null;
        }
        try {
            return minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .build());
        } catch (Exception e) {
            log.warn(LOG_PREFIX + "Object not found or error getting info: {}, error: {}", objectName, e.getMessage());
            return null;
        }
    }

    public boolean downloadFile(String objectName, String localFilePath) {
        if (!ensureInitialized()) {
            log.error(LOG_PREFIX + "Client not initialized, cannot download file");
            return false;
        }

        try {
            // 下载前校验文件是否存在
            StatObjectResponse objectInfo = getObjectInfo(objectName);
            if (objectInfo == null) {
                log.error(LOG_PREFIX + "Object does not exist, cannot download: {}", objectName);
                return false;
            }

            // 创建本地目录（如果不存在）
            File localFile = new File(localFilePath);
            File parentDir = localFile.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                if (!parentDir.mkdirs()) {
                    log.error(LOG_PREFIX + "Failed to create directory: {}", parentDir.getAbsolutePath());
                    return false;
                }
            }
            minioClient.downloadObject(
                    DownloadObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .filename(localFilePath)
                            .overwrite(true)
                            .build());
            
            // 下载后校验本地文件大小是否与远程一致
            if (localFile.exists() && localFile.length() != objectInfo.size()) {
                log.error(LOG_PREFIX + "Downloaded file size mismatch. Expected: {}, Actual: {}", 
                        objectInfo.size(), localFile.length());
                return false;
            }
            
            log.warn(LOG_PREFIX + "File downloaded successfully: {}, size: {}", localFilePath, objectInfo.size());
            return true;
        } catch (Exception e) {
            log.error(LOG_PREFIX + "Failed to download file {}: {}", objectName, e.getMessage(), e);
            return false;
        }
    }

    /**
     * 通过 InputStream 上传文件到 MinIO
     * @param inputStream 输入流
     * @param objectName  对象名称（MinIO 中的路径）
     * @return 是否上传成功
     */
    public boolean uploadFile(InputStream inputStream, String objectName) {
        return uploadFile(inputStream, objectName, null);

    }

    /**
     * 通过 InputStream 上传文件到 MinIO
     *
     * @param inputStream 输入流
     * @param objectName  对象名称（MinIO 中的路径）
     * @param contentType 文件类型，可为 null
     * @return 是否上传成功
     */
    public boolean uploadFile(InputStream inputStream, String objectName, String contentType) {
        if (!ensureInitialized()) {
            log.error(LOG_PREFIX + "Client not initialized, cannot upload file");
            return false;
        }

        try {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .stream(inputStream, -1, 10485760) // -1 未知大小，10MB 分块
                            .contentType(contentType != null ? contentType : "application/octet-stream")
                            .build());
            log.warn(LOG_PREFIX + "File uploaded successfully via stream: {}", objectName);
            return true;
        } catch (Exception e) {
            log.error(LOG_PREFIX + "Failed to upload file {}: {}", objectName, e.getMessage(), e);
            return false;
        }
    }

    /**
     * 通过 InputStream 上传文件到 MinIO（指定文件大小）
     *
     * @param inputStream 输入流
     * @param objectName  对象名称（MinIO 中的路径）
     * @param objectSize  文件大小（字节），如果未知传入 -1
     * @param contentType 文件类型，可为 null
     * @return 是否上传成功
     */
    public boolean uploadFile(InputStream inputStream, String objectName, long objectSize, String contentType) {
        if (!ensureInitialized()) {
            log.error(LOG_PREFIX + "Client not initialized, cannot upload file");
            return false;
        }

        try {
            PutObjectArgs.Builder builder = PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .contentType(contentType != null ? contentType : "application/octet-stream");

            if (objectSize > 0) {
                // 已知文件大小，partSize 设为 -1 自动计算
                builder.stream(inputStream, objectSize, -1);
            } else {
                // 未知文件大小，使用 10MB 分块上传
                builder.stream(inputStream, -1, 10485760);
            }

            minioClient.putObject(builder.build());
            log.warn(LOG_PREFIX + "File uploaded successfully via stream: {}, size: {}", objectName, objectSize);
            return true;
        } catch (Exception e) {
            log.error(LOG_PREFIX + "Failed to upload file {}: {}", objectName, e.getMessage(), e);
            return false;
        }
    }


    public void minioTest() {
        // 示例：上传文件
        String localUploadPath = "E:\\minio\\1208.txt";
        uploadFile(localUploadPath, "marketing/minio/1208/1208.txt");

        // 示例：下载文件
        /*String localDownloadPath = "E:\\minio\\1.txt";
        this.downloadFile("1.txt", localDownloadPath);*/
    }

    /**
     * 手动初始化连接，用于测试连接是否正常
     */
    public boolean testConnection() {
        if (ensureInitialized()) {
            log.warn(LOG_PREFIX + "Connection test successful");
            return true;
        }
        return false;
    }
}
