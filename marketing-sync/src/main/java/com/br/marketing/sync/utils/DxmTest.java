package com.br.marketing.sync.utils;

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

public class DxmTest {
    private static final int AES_KEY_SIZE = 32; // AES-256密钥长度
    private static final int BLOCK_SIZE = 16;   // AES块大小

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
     * 处理CSV文件（解密第一列）
     * @param inputPath 加密的CSV文件路径
     * @param outputPath 解密后的输出路径
     * @param keyHex 十六进制格式的密钥（32字节）
     */
    public static void decryptCSV(String inputPath, String outputPath, String keyHex) throws Exception {
        // 转换十六进制密钥为字节
        byte[] keyBytes = hexToBytes(keyHex);

        // 读取CSV所有行，尝试多种编码
        List<String> lines = null;
        Charset[] charsets = {StandardCharsets.UTF_8, Charset.forName("GBK"), Charset.forName("GB2312"), StandardCharsets.ISO_8859_1};
        
        for (Charset charset : charsets) {
            try {
                lines = Files.readAllLines(Paths.get(inputPath), charset);
                break; // 成功读取，跳出循环
            } catch (Exception e) {
                // 继续尝试下一个编码
                continue;
            }
        }
        
        if (lines == null) {
            throw new IOException("无法读取CSV文件，尝试了多种编码格式");
        }
        if (lines.isEmpty()) {
            throw new IOException("CSV文件为空");
        }

        // 处理每行数据
        List<String> decryptedLines = new ArrayList<>();
        decryptedLines.add(lines.get(0)); // 保留标题行

        for (int i = 1; i < lines.size(); i++) {
            String[] columns = lines.get(i).split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
            if (columns.length > 0) {
                try {
                    columns[0] = decrypt(columns[0], keyBytes); // 解密第一列
                } catch (Exception e) {
                    columns[0] = "[DECRYPT_FAILED]";
                    System.err.println("解密失败 (行 " + i + "): " + e.getMessage());
                }
            }
            decryptedLines.add(String.join(",", columns));
        }

        // 写入解密后的文件，使用UTF-8编码
        Files.write(Paths.get(outputPath), decryptedLines, StandardCharsets.UTF_8);
        System.out.println("解密完成！结果保存在: " + outputPath);
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
            System.err.println("处理失败: " + e.getMessage());
            e.printStackTrace();
        }
        byte[] keyBytes = hexToBytes(secretKeyHex);
        try {
          String a =  decrypt("D9GoyhNjdo+tb4tK9xsuR+ZGekVy8a58k6XehTOmiMs=", keyBytes);
          System.out.println(a);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
