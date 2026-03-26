package com.br.marketing.common.utils.file;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;

import java.io.File;
import java.nio.file.Files;
import java.util.List;

/**
 * ZipUtils 单测：用本地 zip 文件 + 指定编码验证解压是否成功。
 * 请将你的 zip 放到 src/test/resources/test.zip，并修改下面 CHARSET 为实际编码（如 GBK、UTF-8）。
 */
public class ZipUtilsTest {

    /** 本地 zip 相对 resources 的路径，可改为你的文件名 */
    private static final String LOCAL_ZIP_RESOURCE = "20260127-M1-LOW_LOW_RISK-WW-公司简称-01 .zip";

    /** 解压时使用的编码，按你的压缩包实际编码修改（如 GBK、UTF-8） */
    private static final String CHARSET = "GBK";

    /** 解压输出在 resources 下的子目录名 */
    private static final String EXTRACTED_DIR = "extracted";

    /**
     * 使用本地 zip（src/test/resources/）和指定编码解压到 resources/extracted 下，校验能正常解压并返回路径列表。
     */
    @Test
    public void unZipAndReturnExtractedPaths_withLocalZipAndCharset() throws Exception {
        File zipFile = getLocalZipFile();
        Assume.assumeTrue("请将待测 zip 放到 src/test/resources/" + LOCAL_ZIP_RESOURCE, zipFile != null && zipFile.exists());

        File resourcesDir = zipFile.getParentFile();
        File destDir = new File(resourcesDir, EXTRACTED_DIR);
        if (destDir.exists()) {
            deleteRecursively(destDir);
        }
        destDir.mkdirs();
        try {
            List<String> paths = ZipUtils.unZipAndReturnExtractedPaths(
                    zipFile, destDir.getAbsolutePath(), "", CHARSET);

            Assert.assertNotNull(paths);
            Assert.assertFalse("解压后应至少有一个文件", paths.isEmpty());
            for (String path : paths) {
                Assert.assertTrue("解压文件应存在: " + path, new File(destDir, path).exists());
            }
        } finally {
//            deleteRecursively(destDir);
        }
    }

    private static File getLocalZipFile() {
        try {
            return new File(ZipUtilsTest.class.getClassLoader().getResource(LOCAL_ZIP_RESOURCE).toURI());
        } catch (Exception e) {
            return null;
        }
    }

    private static void deleteRecursively(File f) {
        if (f == null || !f.exists()) return;
        if (f.isDirectory()) {
            File[] children = f.listFiles();
            if (children != null) for (File c : children) deleteRecursively(c);
        }
        f.delete();
    }
}
