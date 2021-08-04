package com.br.marketing.common.utils.file;

import com.br.marketing.common.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Created by Bairong on 2019/9/2.
 */
@Slf4j
public class MyFileUtil {
    private final static Pattern MYREGEX = Pattern.compile("\\p{C}");


    public static String getMd5(InputStream inputStream) throws IOException {
        return DigestUtils.md5Hex(inputStream);
    }


    /**
     * 将文件hash取模之后放到不同的小文件中
     *
     * @param targetFile 要去重的文件路径
     * @param splitSize  将目标文件切割成多少份hash取模的小文件个数
     * @return
     */
    public static File[] splitFile(String targetFile, int splitSize) {
        File file = new File(targetFile);
        PrintWriter[] pws = new PrintWriter[splitSize];
        File[] littleFiles = new File[splitSize];
        String parentPath = file.getParent();
        File tempFolder = new File(parentPath + File.separator + "tmp");
        if (!tempFolder.exists()) {
            boolean mkdir = tempFolder.mkdir();
            if (!mkdir) {
                log.error("创建文件 失败 ");
            }
        }
        for (int i = 0; i < splitSize; i++) {
            littleFiles[i] = new File(tempFolder.getAbsolutePath() + File.separator + i + ".txt");
            if (littleFiles[i].exists()) {
                Path path = littleFiles[i].toPath();
                try {
                    Files.delete(path);
                } catch (IOException e) {
                    log.error("Files.delete error", e);
                }
            }
            try {
                pws[i] = new PrintWriter(littleFiles[i]);
            } catch (FileNotFoundException e) {
                log.error("fileNotFound error", e);
            }
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(file));) {
            String tempString = null;
            while ((tempString = reader.readLine()) != null) {
                tempString = tempString.trim();
                tempString = MYREGEX.matcher(tempString).replaceAll("");
                if (StringUtils.isNotEmpty(tempString)) {
                    if (!(tempString.indexOf("cus_num") != -1 && (tempString.indexOf("id") != -1
                            || tempString.indexOf("name") != -1 || tempString.indexOf("cell") != -1))) {
                        //关键是将每行数据hash取模之后放到对应取模值的文件中，确保hash值相同的字符串都在同一个文件里面
                        int index = Math.abs(tempString.hashCode() % splitSize);
                        pws[index].println(tempString);
                    }
                }
            }

        } catch (Exception e) {
            log.error("BufferedReader error", e);
        } finally {
            for (int i = 0; i < splitSize; i++) {
                if (pws[i] != null) {
                    pws[i].close();
                }
            }
        }
        return littleFiles;
    }

    public static int getTotalLines(File file) {
        long startTime = System.currentTimeMillis();
        String tempString;
        int lines = 0;
        try (BufferedReader reader = new BufferedReader(new FileReader(file));) {
            while ((tempString = reader.readLine()) != null) {
                tempString = tempString.trim();
                tempString = MYREGEX.matcher(tempString).replaceAll("");
                if (StringUtils.isNotEmpty(tempString)) {
                    lines++;
                }
            }
            long endTime = System.currentTimeMillis();
            log.warn("统计文件行数{},运行时间： {}ms", lines, (endTime - startTime));
        } catch (Exception e) {
            log.error("getTotalLines error", e);
        }
        return lines;
    }

    public static StringBuilder gethead(String filePath) {
        try (Reader r = new FileReader(filePath);
             BufferedReader br = new BufferedReader(r)) {
            String s = br.readLine();
            if (StringUtils.isEmpty(s)) {
                return new StringBuilder();
            }
            s = MYREGEX.matcher(s).replaceAll("");
            return new StringBuilder(s);
        } catch (IOException e) {
            log.error("gethead error", e);
        }
        return null;
    }

    /**
     * 对小文件进行去重合并
     *
     * @param littleFiles      切割之后的小文件数组
     * @param distinctFilePath 去重之后的文件路径
     * @param splitSize        小文件大小
     */
    public static void distinct(File[] littleFiles, String distinctFilePath, String distinctFileName, int splitSize) {
        File dir = new File(distinctFilePath);
        if (!dir.exists()) {
            boolean mkdir = dir.mkdir();
            if (!mkdir) {
                log.error("mkdir error");
            }
        }
        String concat = distinctFilePath.concat(distinctFileName);
        File distinctedFile = new File(concat);
        if (distinctedFile.exists()) {
            try {
                Files.delete(Paths.get(concat));
            } catch (IOException e) {
                log.error("Files.delete error", e);
            }
        }
        try {
            distinctedFile.createNewFile();
        } catch (IOException e) {
            log.error("createNewFile error", e);
        }
        try (
                PrintWriter pw = new PrintWriter(distinctedFile);) {
            Set<String> unicSet = new HashSet<String>();
            for (int i = 0; i < splitSize; i++) {
                if (littleFiles[i].exists()) {
                    unique(littleFiles, unicSet, i);
                    for (String s : unicSet) {
                        pw.println(s);
                    }
                    unicSet.clear();
                }
            }
        } catch (FileNotFoundException e) {
            log.error("fileNotFound error", e);
        } catch (IOException e1) {
            log.error("printWriter error", e1);
        } finally {
            for (int i = 0; i < splitSize; i++) {
                //合并完成之后删除临时小文件
                if (littleFiles[i].exists()) {
                    Path path = littleFiles[i].toPath();
                    try {
                        Files.delete(path);
                    } catch (IOException e) {
                        log.error("Files.delete error", e);
                    }
                }
            }
        }
    }

    private static void unique(File[] littleFiles, Set<String> unicSet, int i) {
        try (FileReader frs = new FileReader(littleFiles[i]);
             BufferedReader brs = new BufferedReader(frs);) {
            String line = null;
            while ((line = brs.readLine()) != null) {
                if (StringUtils.isNotEmpty(line)) {
                    unicSet.add(line);
                }
            }
        } catch (Exception e) {
            log.error("unique error", e);
        }
    }

    private static void uniqueByNum(File[] littleFiles, Map<String, String> unicMap, int i, int indexCusNum) {
        try (FileReader frs = new FileReader(littleFiles[i]);
             BufferedReader brs = new BufferedReader(frs);) {
            String line = null;
            while ((line = brs.readLine()) != null) {
                if (StringUtils.isNotEmpty(line)) {
                    String s = "";
                    try {
                        s = line.split(",")[indexCusNum];
                    } catch (ArrayIndexOutOfBoundsException e) {
                        log.warn("indexCusNum {} error", indexCusNum, e);
                        unicMap.put(s, line);
                    }
                    unicMap.put(s, line);
                }
            }
        } catch (Exception e) {
            log.error("Exception", e);
        }
    }

    /**
     * 对小文件按CusNum进行去重合并
     *
     * @param littleFiles      切割之后的小文件数组
     * @param distinctFilePath 去重之后的文件路径
     * @param splitSize        小文件大小
     * @param head
     */
    public static void distinctByCusNum(File[] littleFiles, String distinctFilePath, String distinctFileName, int splitSize, StringBuilder head) {
        File dir = new File(distinctFilePath);
        if (!dir.exists()) {
            boolean mkdir = dir.mkdir();
            if (!mkdir) {
                log.error("mkdir error");
            }
        }
        String concat = distinctFilePath.concat(distinctFileName);
        File distinctedFile = new File(concat);
        if (distinctedFile.exists()) {
            try {
                Files.delete(Paths.get(concat));
            } catch (IOException e) {
                log.error("Files.delete error", e);
            }
        }
        try (PrintWriter pw = new PrintWriter(distinctedFile);) {

            Map<String, String> unicMap = new HashMap<>();
            pw.println(head);
            int indexCusNum = findIndex(head.toString().split(","), "cus_num");
            for (int i = 0; i < splitSize; i++) {
                if (littleFiles[i].exists()) {
                    uniqueByNum(littleFiles, unicMap, i, indexCusNum);
                    for (Map.Entry<String, String> entry : unicMap.entrySet()) {
                        String value = entry.getValue();
                        pw.println(value);
                    }
                    unicMap.clear();
                }
            }
        } catch (FileNotFoundException e) {
            log.error("FileNotFoundException", e);
        } catch (Exception e1) {
            log.error("Exception", e1);
        } finally {
            for (int i = 0; i < splitSize; i++) {
                //合并完成之后删除临时小文件
                if (littleFiles[i].exists()) {
                    Path path = littleFiles[i].toPath();
                    try {
                        Files.delete(path);
                    } catch (IOException e) {
                        log.error("Files.delete error", e);
                    }
                }
            }
        }
    }

    /**
     * 将文件hash取模之后放到不同的小文件中
     *
     * @param targetFile 要去重的文件路径
     * @param splitSize  将目标文件切按CusNum割成多少份hash取模的小文件个数
     * @return
     */
    public static File[] splitFileByCusNum(String targetFile, int splitSize) {
        File file = new File(targetFile);
        PrintWriter[] pws = new PrintWriter[splitSize];
        File[] littleFiles = new File[splitSize];
        String parentPath = file.getParent();
        File tempFolder = new File(parentPath + File.separator + "tmp");
        if (!tempFolder.exists()) {
            boolean mkdir = tempFolder.mkdir();
            if (!mkdir) {
                log.error("mkdir error");
            }
        }
        for (int i = 0; i < splitSize; i++) {
            littleFiles[i] = new File(tempFolder.getAbsolutePath() + File.separator + i + ".txt");
            if (littleFiles[i].exists()) {
                Path path = littleFiles[i].toPath();
                try {
                    Files.delete(path);
                } catch (IOException e) {
                    log.error("Files.delete error", e);
                }
            }
            try {
                pws[i] = new PrintWriter(littleFiles[i]);
            } catch (FileNotFoundException e) {
                log.error("fileNotFound error", e);
            }
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(file));) {

            String tempString = null;
            int indexCusNum = 0;
            int num = 0;
            while ((tempString = reader.readLine()) != null) {
                tempString = tempString.trim();
                tempString = MYREGEX.matcher(tempString).replaceAll("");
                if (StringUtils.isNotEmpty(tempString)) {
                    if (tempString.indexOf("cus_num") != -1 && (tempString.indexOf("id") != -1
                            || tempString.indexOf("name") != -1 || tempString.indexOf("cell") != -1)) {
                        indexCusNum = findIndex(tempString.split(","), "cus_num");
                    } else {
                        //关键是将每行数据hash取模之后放到对应取模值的文件中，确保hash值相同的字符串都在同一个文件里面
                        String s = "";
                        try {
                            s = tempString.split(",")[indexCusNum];
                        } catch (ArrayIndexOutOfBoundsException e) {
                            log.warn("indexCusNum {} error", indexCusNum, e);
                        }
                        int index = Math.abs(s.hashCode() % splitSize);
                        pws[index].println(tempString);
                    }
                    num++;
                }
            }
            log.warn("num:{}", num);
        } catch (Exception e) {
            log.error("Exception", e);
        } finally {
            for (int i = 0; i < splitSize; i++) {
                if (pws[i] != null) {
                    pws[i].close();
                }
            }
        }
        return littleFiles;
    }

    /**
     * 查找某个值在数组中的索引
     *
     * @param array 数组
     * @param value 给定的值
     * @return 索引
     */
    public static int findIndex(String[] array, String value) {
        for (int i = 0; i < array.length; i++) {
            if (array[i].equals(value)) {
                return i;
            }
        }
        return -1;
    }
}
