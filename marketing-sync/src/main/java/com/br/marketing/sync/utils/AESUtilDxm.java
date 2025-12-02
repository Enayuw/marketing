package com.br.marketing.sync.utils;

import com.br.marketing.common.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.regex.Pattern;

public class AESUtilDxm {
    private static final int AES_KEY_SIZE = 32; // AES-256密钥长度
    private static final int BLOCK_SIZE = 16;   // AES块大小
    
    // 预编译的正则表达式，避免重复编译
    private static final Pattern CSV_SPLIT_PATTERN = Pattern.compile(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
    private static final Pattern LINE_BREAK_PATTERN = Pattern.compile("\\r?\\n");
    private static final Pattern QUOTE_PATTERN = Pattern.compile("^\"|\"$");

    protected final static Logger log = LoggerFactory.getLogger(AESUtilDxm.class);

    /**
     * AES-256解密（兼容Python加密输出格式）
     * @param encryptedData Base64编码的加密数据（IV + ciphertext）
     * @param keyBytes 32字节密钥
     */
    public static String decrypt(String encryptedData, byte[] keyBytes) throws Exception {
        // 检查密钥长度
        if (keyBytes.length != AES_KEY_SIZE) {
            throw new IllegalArgumentException("Invalid key length. Must be 32 bytes for AES-256");
        }

        // Base64解码
        byte[] encryptedBytes = Base64.getDecoder().decode(encryptedData);

        // 提取IV（前16字节）
        if (encryptedBytes.length < BLOCK_SIZE) {
            throw new IllegalArgumentException("Invalid encrypted data - missing IV");
        }
        byte[] iv = new byte[BLOCK_SIZE];
        System.arraycopy(encryptedBytes, 0, iv, 0, BLOCK_SIZE);

        // 提取密文
        byte[] ciphertext = new byte[encryptedBytes.length - BLOCK_SIZE];
        System.arraycopy(encryptedBytes, BLOCK_SIZE, ciphertext, 0, ciphertext.length);

        // 初始化解密器
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE,
                new SecretKeySpec(keyBytes, "AES"),
                new IvParameterSpec(iv));

        // 解密并去除填充
        byte[] decrypted = cipher.doFinal(ciphertext);
        return new String(decrypted, StandardCharsets.UTF_8);
    }

    /**
     * 处理CSV文件（解密"手机号"列）
     * @param inputPath 加密的CSV文件路径
     * @param outputPath 解密后的输出路径
     * @param keyHex 十六进制格式的密钥（32字节）
     */
    public static void decryptCSV(String inputPath, String outputPath, String keyHex) throws Exception {
        // 转换十六进制密钥为字节
        byte[] keyBytes = hexToBytes(keyHex);

        // 读取CSV文件，自动处理UTF-8 BOM
        List<String> lines = readCsvWithBomSupport(inputPath);
        
        // 处理每行数据
        List<String> decryptedLines = new ArrayList<>();
        
        // 解析标题行，查找"手机号"列的索引
        String headerLine = lines.get(0);
        String[] headers = CSV_SPLIT_PATTERN.split(headerLine, -1);
        int phoneColumnIndex = -1;
        
        for (int i = 0; i < headers.length; i++) {
            String header = QUOTE_PATTERN.matcher(headers[i].trim()).replaceAll(""); // 去除可能的引号
            if ("手机号".equals(header)) {
                phoneColumnIndex = i;
                break;
            }
        }
        
        decryptedLines.add(headerLine); // 保留标题行
        
        // 如果没有找到"手机号"列，直接复制所有行，不进行解密
        if (phoneColumnIndex == -1) {
            for (int i = 1; i < lines.size(); i++) {
                decryptedLines.add(lines.get(i));
            }
        } else {
            // 处理数据行
            for (int i = 1; i < lines.size(); i++) {
                String[] columns = CSV_SPLIT_PATTERN.split(lines.get(i), -1);
                
                // 确保列数匹配
                if (columns.length > phoneColumnIndex) {
                    String phoneColumn = columns[phoneColumnIndex];
                    
                    // 检查手机号列是否为空
                    if (!StringUtils.isEmpty(phoneColumn)) {
                        try {
                            String decrypted = decrypt(phoneColumn, keyBytes);
                            columns[phoneColumnIndex] = decrypted; // 解密手机号列
                        } catch (Exception e) {
                            // 解密失败时保留原始值，而不是替换为 [DECRYPT_FAILED]
                            // columns[phoneColumnIndex] = phoneColumn; // 保留原值
                            log.warn("【度小满解密】解密失败 (行 " + (i + 1) + ")，保留原值: " + e.getMessage());
                        }
                    } else {
                        log.warn("【度小满解密】跳过空手机号 (行 " + (i + 1) + ")");
                    }
                }
                
                decryptedLines.add(String.join(",", columns));
            }
        }

        // 写入解密后的文件，统一使用UTF-8编码（国际标准，避免下游系统乱码）
        Files.write(Paths.get(outputPath), decryptedLines, StandardCharsets.UTF_8);
    }

    /**
     * 读取CSV文件，支持UTF-8 BOM和多种编码
     * 
     * @param inputPath 输入文件路径
     * @return 文件内容行列表
     * @throws IOException 读取失败
     */
    private static List<String> readCsvWithBomSupport(String inputPath) throws IOException {
        // 先读取文件字节，检测BOM
        byte[] fileBytes = Files.readAllBytes(Paths.get(inputPath));
        
        if (fileBytes.length == 0) {
            throw new IOException("CSV文件为空");
        }
        
        // 检测UTF-8 BOM (EF BB BF)
        boolean hasUtf8Bom = false;
        
        if (fileBytes.length >= 3 && 
            (fileBytes[0] & 0xFF) == 0xEF && 
            (fileBytes[1] & 0xFF) == 0xBB && 
            (fileBytes[2] & 0xFF) == 0xBF) {
            hasUtf8Bom = true;
            log.warn("【度小满解密】检测到 UTF-8 BOM 标记，将自动处理");
        }
        
        // 如果有BOM，跳过BOM后读取
        if (hasUtf8Bom) {
            byte[] contentBytes = new byte[fileBytes.length - 3];
            System.arraycopy(fileBytes, 3, contentBytes, 0, contentBytes.length);
            String content = new String(contentBytes, StandardCharsets.UTF_8);
            String[] lineArray = LINE_BREAK_PATTERN.split(content);
            return new ArrayList<>(java.util.Arrays.asList(lineArray));
        }
        
        // 没有BOM，尝试多种编码
        List<String> lines = null;
        String detectedEncoding = "UTF-8";
        Charset[] charsets = {
            Charset.forName("GBK"),       // 优先GBK（Windows中文默认）
            Charset.forName("GB2312"),    // GB2312
            StandardCharsets.UTF_8,       // UTF-8
            StandardCharsets.ISO_8859_1   // ISO-8859-1（最后尝试）
        };
        
        for (Charset charset : charsets) {
            try {
                lines = Files.readAllLines(Paths.get(inputPath), charset);
                detectedEncoding = charset.name();
                log.warn("【度小满解密】使用 " + detectedEncoding + " 编码读取文件");
                break; // 成功读取，跳出循环
            } catch (Exception e) {
                // 继续尝试下一个编码
                continue;
            }
        }
        
        if (lines == null) {
            throw new IOException("无法读取CSV文件，尝试了多种编码格式均失败");
        }
        
        return lines;
    }

    /** 十六进制字符串转字节数组 */
    private static byte[] hexToBytes(String hex) {
        if (hex.length() % 2 != 0) {
            throw new IllegalArgumentException("Hex string must have even length");
        }
        byte[] bytes = new byte[hex.length() / 2];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (byte) Integer.parseInt(hex.substring(i * 2, i * 2 + 2), 16);
        }
        return bytes;
    }

    public static void main(String[] args) {
        // 配置参数（与Python示例对应）
        String inputCsv = "/Users/dxm/Documents/task.csv";
        String outputCsv = "/Users/dxm/Documents/task_des.csv";
        String secretKeyHex = "40999bbc7cdc1a14a1c61a3fb9a74485f196f4a8d205e76966ef44178f0827b5";

        try {
            decryptCSV(inputCsv, outputCsv, secretKeyHex);
        } catch (Exception e) {
            log.warn("处理失败: " + e.getMessage());
            e.printStackTrace();
        }
        byte[] keyBytes = hexToBytes(secretKeyHex);
        try {
            decrypt("D9GoyhNjdo+tb4tK9xsuR+ZGekVy8a58k6XehTOmiMs=", keyBytes);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
