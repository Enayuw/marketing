package com.br.marketing.push.util;

import cn.hutool.crypto.SecureUtil;
import com.br.marketing.common.bean.Score;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.common.utils.DateHelper;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.common.utils.file.ZipUtil;
import com.br.marketing.entity.TaskStatusDistribute;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Created by Bairong on 2019/9/2.
 */
@Slf4j
public class FileUtil {

    public static boolean merge(Map<String, List<String>> map, String fileName, String errorFileName) {
        log.info("merge  fileName:{}", fileName);
        boolean result = false;
        FileReader read = null;
        BufferedReader br = null;
        int errorRownum = 0;

        Map<String, String> clearMap = new HashMap<>();
        Set<String> keys = map.keySet();
        Set<String> sortSet = new TreeSet<String>(new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return (DateHelper.parseDate(o2).compareTo(DateHelper.parseDate(o1)));
            }
        });
        sortSet.addAll(keys);
        log.info("sortSet size:{}", sortSet.size());
        File file1 = new File(fileName);
        File errorFile = new File(errorFileName);

        try (Writer fw = new BufferedWriter(
                new OutputStreamWriter(
                        new FileOutputStream(file1), StandardCharsets.UTF_8));


             Writer errorFw = new BufferedWriter(
                     new OutputStreamWriter(
                             new FileOutputStream(errorFile), StandardCharsets.UTF_8));) {
            int rownum = 1;
            boolean flag = true;
            for (String key : sortSet) {
                List<String> strings = map.get(key);
                log.info("key:{},strings", key, strings.size());
                for (String name : strings) {
                    log.info("FileName ---{}", name);
                    File writeName = new File(name);
                    if (!writeName.exists()) {
                        log.info("name ---{} 不存在", name);
                        continue;
                    }
                    if (name.indexOf("error") > -1) {
                        read = new FileReader(name);
                        br = new BufferedReader(read);
                        String row;
                        while ((row = br.readLine()) != null) {
                            errorRownum++;
                            errorFw.append(row + "\r\n");
                        }
                    } else {
                        read = new FileReader(name);
                        br = new BufferedReader(read);
                        String row;
                        while ((row = br.readLine()) != null) {
                            //log.info("row ---{}",row);
                            rownum++;
                            String[] split = row.split(",");
                            //如果是表头，第一个文件需要写表头，第二个文件开始不再写表头
                            if ("request_time".equals(split[0])) {
                                if (flag) {
                                    fw.append(row + "\r\n");
                                }
                            } else {
                                String md5String = SecureUtil.md5(split[1] + split[2]);
                                String s = clearMap.get(md5String);
                                log.info("md5String ---{}", s);
                                if (StringUtils.isEmpty(s)) {
                                    fw.append(row + "\r\n");
                                    clearMap.put(md5String, String.valueOf(System.currentTimeMillis()));
                                }
                            }
                        }
                        flag = false;
                    }
                }
            }
            errorFw.close();
            log.info("rownum=" + rownum);
            log.info("errorRownum=" + errorRownum);
        } catch (FileNotFoundException e) {
            log.error("FileNotFoundException ", e);
        } catch (IOException e) {
            log.error("IOException ", e);
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    log.error("IOException ", e);
                }
            }
            if (read != null) {
                try {
                    read.close();
                } catch (IOException e) {
                    log.error("IOException ", e);
                }
            }
        }
        if (errorRownum > 0) {
            result = true;
        }
        return result;
    }

    private static int countStr(String str, String sToFind) {
        int num = 0;
        int len1 = str.length();
        String str1 = str.replaceAll(Pattern.quote(sToFind), "");
        int len2 = str1.length();
        num = len1 - len2;
        return num;
    }

    /**
     * 合并错误文件
     *
     * @param pathName
     * @param destPath
     */
    public static boolean mergeError(String pathName, String destPath) {
        boolean flag = false;
        log.info("开始合并文件 结果文件名称:{},需要合并的目录:{}", pathName, destPath);
        long l = System.currentTimeMillis();
        FileReader read = null;
        BufferedReader br = null;
        int rownum = 0;

        File writeName = new File(destPath);
        if (!writeName.exists()) {
            return flag;
        }
        File file1 = new File(pathName);

        try (Writer fw = new BufferedWriter(
                new OutputStreamWriter(
                        new FileOutputStream(file1), StandardCharsets.UTF_8));) {
            List<String> fileNmaes = getErrorFileNames(writeName);
            for (String name : fileNmaes) {
                read = new FileReader(destPath + "/" + name);
                br = new BufferedReader(read);
                String row;
                while ((row = br.readLine()) != null) {
                    rownum++;
                    fw.append(row + "\r\n");
                }
                br.close();
                read.close();
            }
            log.info("rownum=" + rownum);
        } catch (FileNotFoundException e) {
            log.error("FileNotFoundException ", e);
        } catch (Exception e) {
            log.error("合并文件出错", e);
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    log.error("IOException ", e);
                }
            }
            if (read != null) {
                try {
                    read.close();
                } catch (IOException e) {
                    log.error("IOException ", e);
                }
            }
        }
        log.info("合并文件结束--耗时：{}", System.currentTimeMillis() - l);

        if (rownum > 0) {
            flag = true;
        }
        return flag;
    }

    /**
     * @param pathName 结果文件名称
     * @param destPath 需要合并的目录
     */
    public static List<String> mergeAll(String head, String pathName, String destPath, String sep, Integer fileNum) {
        log.warn("开始合并文件 结果文件名称:{},需要合并的目录:{},分隔符:{}", pathName, destPath, sep);
        long l = System.currentTimeMillis();
        ExecutorService mergeExecutor = BrExecutors.getThreadPool(100, 100);
        FileReader read = null;
        BufferedReader br = null;
        List<String> res = new ArrayList<>();
        res.add(pathName);
        File file1 = new File(pathName);
        List<BufferedWriter> fws = new ArrayList<>();
        try {
            fws.add(new BufferedWriter(
                    new OutputStreamWriter(
                            new FileOutputStream(file1), StandardCharsets.UTF_8)));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        try {
            int rownum = 1;
            Integer fileIndex = 0;
            String headstring = head.substring(0, head.length() - 1);
            int i = countStr(headstring, sep);
            String path = destPath.concat("/");
            File writeName = new File(path);
            List<String> fileNmaes = getFileNames(writeName);
            for (String name : fileNmaes) {
                read = new FileReader(path + "/" + name);
                br = new BufferedReader(read);
                String row;
                while ((row = br.readLine()) != null) {
                    if(rownum>fileNum){
                        fileIndex++;
                        String fileAddPath = pathName.replace(".txt", "-" + fileIndex).concat(".txt");
                        res.add(fileAddPath);
                        File file = new File(fileAddPath);
                        BufferedWriter bufferedWriter = new BufferedWriter(
                                new OutputStreamWriter(
                                        new FileOutputStream(file), StandardCharsets.UTF_8));
                        fws.add(bufferedWriter);
                        rownum=1;
                    }
                    BufferedWriter fw = fws.get(fileIndex);
                    if(rownum == 1){
                        fw.append(headstring + "\r\n");
                    }
                    mergeExecutor.submit(new CheckRowSep(fw, row, i, sep, name));
                    rownum++;
                }
                br.close();
                read.close();
            }
            /**
             * 等待所有任务都执行完成
             **/
            mergeExecutor.shutdown();
            while (true) {
                if (mergeExecutor.isTerminated()) {
                    log.warn("所有合并线程都执行结束");
                    break;
                }
                try {
                    Thread.sleep(3000);
                } catch (Exception e) {
                    log.error("sleep ", e);
                }
            }
            for (BufferedWriter fw : fws) {
                if(fw!=null){
                    fw.close();
                }
            }
            log.warn("rownum=" + rownum);
        } catch (FileNotFoundException e) {
            log.error("FileNotFoundException ", e);
        } catch (Exception e) {
            log.error("合并文件出错", e);
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    log.error("IOException ", e);
                }
            }
            if (read != null) {
                try {
                    read.close();
                } catch (IOException e) {
                    log.error("IOException ", e);
                }
            }
            for (BufferedWriter fw : fws) {
                try {
                    if(fw!=null){
                        fw.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        log.warn("合并文件结束--耗时：{}", System.currentTimeMillis() - l);
        return res;
    }

    /**
     * @param head      统计文件头
     * @param fileName  统计文件路径+名字
     * @param scores    原始数据
     * @param separator 分隔符
     * @return 文件名称
     */
    public static void writeFile(String head, String fileName, ArrayList<Score> scores, String separator) {
        log.warn("开始生成统计文件，文件名称:{}", fileName);
        long l = System.currentTimeMillis();
        File file = new File(fileName);
//        NumberFormat percent = NumberFormat.getPercentInstance();
//        percent.setMaximumFractionDigits(2);
        try (Writer fw = new BufferedWriter(
                new OutputStreamWriter(
                        new FileOutputStream(file), StandardCharsets.UTF_8));) {
            fw.append(head + "\r\n");
            for (Score score : scores) {
                StringBuilder rowBuilder = new StringBuilder();
                rowBuilder.append(score.getScoringRange()).append(separator)
                        .append(score.getSampleCapacity()).append(separator)
                        .append(score.getProportion()
                                .multiply(new BigDecimal(100))
                                .divide(new BigDecimal(1), 2, BigDecimal.ROUND_HALF_UP).toString()).append("%").append(separator)
                        .append(score.getCumulativeProportion()
                                .multiply(new BigDecimal(100))
                                .divide(new BigDecimal(1), 2, BigDecimal.ROUND_HALF_UP).toString()).append("%");
                fw.append(rowBuilder.toString() + "\r\n");
            }
        } catch (FileNotFoundException e) {
            log.error("FileNotFoundException ", e);
        } catch (Exception e) {
            log.error("生成文件出错", e);
        }
        log.warn("生成文件结束--耗时：{}", System.currentTimeMillis() - l);
    }

    static class CheckRowSep implements Runnable {
        private Writer fw;
        private String row;
        private int headSepNum;
        private String sep;
        private String name;

        public CheckRowSep(Writer fw, String row, int headSepNum, String sep,String name) {
            this.fw = fw;
            this.row = row;
            this.headSepNum = headSepNum;
            this.sep = sep;
            this.name = name;
        }

        @Override
        public void run() {
            log.info("开始合并：{}，{}", Thread.currentThread().getName(), headSepNum);
            try {
                row = row.substring(0, row.length() - 1);
                int i1 = countStr1(row, sep);
                if (headSepNum == i1) {
                    fw.append(row + "\r\n");
                } else {
                    log.error("分隔符校验失败 number:{} head:{},row:{}",name, headSepNum, i1);
                    log.warn("Row--{}", row);
                }
            } catch (Exception e) {
                log.error("合并文件出错", e);
            }
        }

        private int countStr1(String str, String sToFind) {
            int num = 0;
            int len1 = str.length();
            String str1 = str.replaceAll(Pattern.quote(sToFind), "");
            int len2 = str1.length();
            num = len1 - len2;
            return num;
        }
    }

    /**
     * 获取路径下的错误文件
     *
     * @param writeName
     * @return
     */
    public static List<String> getErrorFileNames(File writeName) {
        List<String> fileNames = new ArrayList<>();
        File[] files = writeName.listFiles();
        for (int i = 0; i < files.length; i++) {
            String name = files[i].getName();
            if (name.indexOf("error") > -1) {
                fileNames.add(name);
            }
        }

        return fileNames;
    }

    /**
     * 对路径下的文件名进行排序
     *
     * @param writeName
     * @return
     */
    public static List<String> getFileNames(File writeName) {
        List<String> fileNames = new ArrayList<>();
        File[] files = writeName.listFiles();
        for (int i = 0; i < files.length; i++) {
            String name = files[i].getName();
            String str1 = name.replace(".txt", "");
            if(str1.startsWith("error")){
                continue;
            }
            try {
                Long.valueOf(str1);
                fileNames.add(name);
            } catch (Exception e) {
                log.info("filename:{}", name);
                log.info("Exception", e);
            }
        }
        return fileNames;
    }


    private static void writeResultFile(List<String> result, int fileNo, String s1, String destPath, List<String> zipFileResult,
                                        String startTime, String batchNumber, String strategyId, String apiCode, String headstring) {
        String fileName = apiCode + "_" + s1 + "_" + fileNo + "_" + batchNumber + "_" + strategyId.split(":")[0] + "_" + startTime
                + "_" + DateHelper.getDateAddYyMmDd(0) + ".txt";
        String pathName = destPath + fileName;
        String zipFile = fileName.replace(".txt", ".zip");
        File file = new File(pathName);
        try (Writer fw = new BufferedWriter(
                new OutputStreamWriter(
                        new FileOutputStream(file), StandardCharsets.UTF_8));) {
            fw.append(headstring + "\r\n");
            for (String row : result) {
                fw.append(row + "\r\n");
            }
        } catch (Exception e) {
            log.error("writeResultFile error", e);
        }
        ZipUtil.compress(destPath + "/" + fileName, destPath + "/" + zipFile);
        zipFileResult.add(destPath + "/" + zipFile);
    }


}
