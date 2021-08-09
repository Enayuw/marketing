package com.br.marketing.common.utils;

import com.br.fastdfs.FastdfsClient;
import com.br.fastdfs.FastdfsClientFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class FastdfsUtils {
    private static final Logger logger = LoggerFactory.getLogger(FastdfsUtils.class);

    /**
     * 上传到文件管理系统
     *
     * @param convFile 上传的文件
     * @return 服务器保存的地址
     * @throws Exception
     */
    public static String uploadDFSFile(File convFile) throws Exception {
        FastdfsClient fastdfsClient = FastdfsClientFactory.getFastdfsClient();
        Map<String, String> meta = new HashMap<String, String>();
        meta.put("fileName", convFile.getName());
        String fileId = "";
        return fastdfsClient.upload(convFile, convFile.getName(), meta);
    }

    public static String uploadDFSFileByte(byte[] file, String name) throws Exception {
        FastdfsClient fastdfsClient = FastdfsClientFactory.getFastdfsClient();
        Map<String, String> meta = new HashMap<String, String>();
        meta.put("fileName", name);
        String result;
        try {
            result = fastdfsClient.uploadByte(file, name, meta);
        } catch (Exception e) {
            logger.error("文件上传失败--重试一次", e);
            result = fastdfsClient.uploadByte(file, name, meta);
        }
        return result;
    }
}
