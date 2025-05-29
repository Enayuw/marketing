package com.br.marketing.client;

import com.br.marketing.common.commondto.Result;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

@Service
@Slf4j
public class ZipFileClient {

    @Resource
    private HttpProxyClient httpProxyClient;
    private final int bufferSize = 8192;
    private final int socketTimeout = 30000;

    public Result downloadZipFile(String url, String targetPath, Boolean isProxy) {
        Result result = new Result().failure();
        HttpClient httpClient = httpProxyClient.getHttpClientInner(isProxy);
        RequestConfig requestConfig = httpProxyClient.getRequestConfig(isProxy, socketTimeout, null);
        HttpGet httpGet = new HttpGet(url);
        httpGet.setConfig(requestConfig);
        
        try {
            // 添加请求头
            httpGet.addHeader("Accept", "application/zip");
            httpGet.addHeader("Accept-Encoding", "gzip, deflate");
            log.warn("Starting download from: {}", url);
            try (CloseableHttpResponse response = (CloseableHttpResponse) httpClient.execute(httpGet)) {
                int statusCode = response.getStatusLine().getStatusCode();
                
                if (statusCode != HttpStatus.SC_OK) {
                    throw new IOException("Invalid response status: " + statusCode);
                }
                
                HttpEntity entity = response.getEntity();
                if (entity == null) {
                    throw new IOException("No response entity found");
                }
                
                // 确保目标目录存在
                File targetFile = new File(targetPath);
                createDirectoryIfNeeded(targetFile.getParentFile());
                
                // 下载文件
                downloadWithProgress(entity, targetFile);
                
                // 验证下载的文件
                validateZipFile(targetFile);
                
                log.warn("Download completed successfully: {}", targetPath);
            }
            
        } catch (Exception e) {
            log.error("Error downloading zip file", e);
            throw new RuntimeException("Failed to download file", e);
        }
        return result.success();
    }
    public Result downloadFile(String url, String targetPath, Boolean isProxy) {
        Result result = new Result().failure();
        HttpClient httpClient = httpProxyClient.getHttpClientInner(isProxy);
        RequestConfig requestConfig = httpProxyClient.getRequestConfig(isProxy, socketTimeout, null);
        HttpGet httpGet = new HttpGet(url);
        httpGet.setConfig(requestConfig);

        try {
            // 添加请求头
            httpGet.addHeader("Accept", "application/zip");
            httpGet.addHeader("Accept-Encoding", "gzip, deflate");
            log.warn("Starting download from: {}", url);
            try (CloseableHttpResponse response = (CloseableHttpResponse) httpClient.execute(httpGet)) {
                int statusCode = response.getStatusLine().getStatusCode();

                if (statusCode != HttpStatus.SC_OK) {
                    throw new IOException("Invalid response status: " + statusCode);
                }

                HttpEntity entity = response.getEntity();
                if (entity == null) {
                    throw new IOException("No response entity found");
                }

                // 确保目标目录存在
                File targetFile = new File(targetPath);
                createDirectoryIfNeeded(targetFile.getParentFile());

                // 下载文件
                downloadWithProgress(entity, targetFile);

                log.warn("Download completed successfully: {}", targetPath);
            }

        } catch (Exception e) {
            log.error("Error downloading zip file", e);
            throw new RuntimeException("Failed to download file", e);
        }
        return result.success();
    }

    private void downloadWithProgress(HttpEntity entity, File targetFile) throws IOException {
        long totalBytes = entity.getContentLength();
        long downloadedBytes = 0;
        long startTime = System.currentTimeMillis();
        
        try (InputStream is = entity.getContent();
             FileOutputStream fos = new FileOutputStream(targetFile)) {
            
            byte[] buffer = new byte[bufferSize];
            int bytes;
            
            while ((bytes = is.read(buffer)) != -1) {
                fos.write(buffer, 0, bytes);
                downloadedBytes += bytes;
                
                // 计算和显示进度
//                if (totalBytes > 0) {
//                    int progress = (int) ((downloadedBytes * 100) / totalBytes);
//                    double speed = calculateSpeed(downloadedBytes, startTime);
//                    log.warn("Progress: {}% - Speed: {} MB/s", progress, String.format("%.2f", speed));
//                }
            }
        }
    }
    
    private void validateZipFile(File file) throws IOException {
        try (ZipFile zipFile = new ZipFile(file)) {
            // 尝试打开ZIP文件来验证其完整性
            if (zipFile.size() == 0) {
                throw new IOException("Downloaded ZIP file is empty");
            }
        } catch (ZipException e) {
            throw new IOException("Invalid ZIP file", e);
        }
    }
    
    private void createDirectoryIfNeeded(File directory) {
        if (directory != null && !directory.exists()) {
            if (!directory.mkdirs()) {
                throw new RuntimeException("Failed to create directory: " + directory);
            }
        }
    }
    
    private double calculateSpeed(long bytes, long startTime) {
        double elapsedSeconds = (System.currentTimeMillis() - startTime) / 1000.0;
        return (bytes / (1024.0 * 1024.0)) / elapsedSeconds;
    }

}