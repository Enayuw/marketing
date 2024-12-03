package com.br.marketing.tools.controller;

import com.br.common.encryption.Sha256Util;
import com.br.common.util.BrCipherMaker;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.tools.rpcclient.RpcClientProxy;
import com.br.marketing.tools.util.EncAndDecUtil;
import com.br.marketing.tools.util.ThreeKeyEncryptEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 常用文件操作方法
 */
@Slf4j
@RequestMapping("/file")
@RestController
public class FileController {

    @GetMapping("/splitFile")
    public String splitFile(){
        long l = System.currentTimeMillis();
        ExecutorService mergeExecutor = BrExecutors.getThreadPool(100, 100);
        FileReader read = null;
        BufferedReader br = null;
        String pathName = "/opt/temp_file/20230221_phoneAction.txt";
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

            read = new FileReader("/opt/temp_file/20230221_01.txt");
            br = new BufferedReader(read);
            String row;
            while ((row = br.readLine()) != null) {
                String content=row+"\r\n";
                if(rownum>1000000){
                    fileIndex++;
                    String fileAddPath = pathName.replace(".txt", "-" + fileIndex).concat(".txt");
                    File file = new File(fileAddPath);
                    BufferedWriter bufferedWriter = new BufferedWriter(
                            new OutputStreamWriter(
                                    new FileOutputStream(file), StandardCharsets.UTF_8));
                    fws.add(bufferedWriter);
                    rownum=1;
                }
                BufferedWriter fw = fws.get(fileIndex);
                mergeExecutor.submit(()->{
                    try {
                        fw.append(content);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
                rownum++;
            }
            br.close();
            read.close();
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
        return "123";
    }


    @GetMapping("/cellDecByFile")
    public String cellDecByFile(@RequestParam(value = "path") String path){
        long l = System.currentTimeMillis();
        FileReader read = null;
        BufferedReader br = null;
        File file1 = new File(path);
        File[] files = file1.listFiles();
        for (File file : files) {
            ExecutorService mergeExecutor = BrExecutors.getThreadPool(10, 10);
            String[] fileSplit = file.getPath().split("\\.");
            String wFilePath = fileSplit[0] + "_phoneAction." + fileSplit[1];
            File wFile = new File(wFilePath);
            try {
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(
                                new FileOutputStream(wFile), StandardCharsets.UTF_8));
                int rownum = 1;
                AtomicInteger error = new AtomicInteger();
                AtomicInteger success = new AtomicInteger();
                read = new FileReader(file.getPath());
                br = new BufferedReader(read);
                String row;
                while ((row = br.readLine()) != null) {
                    String content = row;
                    Integer threaNum = rownum;
                    mergeExecutor.submit(()->{
                        try {
                            if(!"".equals(content.trim())||!"\"\"".equals(content.trim())){
                                String trim = content.trim();
                                if(StringUtils.isBlank(trim)){
                                    log.warn(content.trim());
                                    error.incrementAndGet();
                                    StringBuilder sb = new StringBuilder();
                                    sb.append(content.trim());
                                    sb.append("\r\n");
                                    writer.append(sb);
                                }else{
                                    String[] split = trim.split("\t");
                                    StringBuilder sb = new StringBuilder();
                                    sb.append(split[0]);
                                    sb.append(",");
                                    sb.append(split[1]);
                                    sb.append(",");
                                    if(!new String("NULL").equals(split[2])){
                                        String decode = BrCipherMaker.getInstance().decode(split[2]);
                                        sb.append(DigestUtils.md5Hex(decode));
                                        sb.append(",");
                                    }else{
                                        sb.append(",");
                                    }
                                    sb.append("\r\n");
                                    writer.append(sb.toString());
                                    success.incrementAndGet();
                                }
//
                            }

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
                    rownum++;
                }
                br.close();
                read.close();
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
                writer.close();
                log.warn("rownum=" + rownum);
                log.warn("解密失败="+error.get());
                log.warn("解密成功="+success.get());
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
            log.warn("合并文件结束--耗时：{}", System.currentTimeMillis() - l);
        }

        return "123";
    }

    /**
     * log解密，sha256加密
     * @param path
     * @return
     */
    @GetMapping("/cellDecByFileTwo")
    public String cellDecByFileTwo(@RequestParam(value = "path") String path){
        long l = System.currentTimeMillis();
        FileReader read = null;
        BufferedReader br = null;
        File file1 = new File(path);
        File[] files = file1.listFiles();
        for (File file : files) {
            ExecutorService mergeExecutor = BrExecutors.getThreadPool(100, 100);
            String[] fileSplit = file.getPath().split("\\.");
            String wFilePath = fileSplit[0] + "_phoneAction." + fileSplit[1];
            File wFile = new File(wFilePath);
            try {
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(
                                new FileOutputStream(wFile), StandardCharsets.UTF_8));
                int rownum = 1;
                read = new FileReader(file.getPath());
                br = new BufferedReader(read);
                String row;
                while ((row = br.readLine()) != null) {
                    String content = row;
                    Integer threaNum = rownum;
                    mergeExecutor.submit(()->{
                        try {
                            if(new Integer(1).equals(threaNum)){
//                                String[] split = content.split(",");
                                StringBuilder sb = new StringBuilder();
                                sb.append(content.trim());
                                sb.append("\r\n");
                                writer.append(sb.toString());
                            }else{
                                String cell = EncAndDecUtil.logTodigest(content.trim(), ThreeKeyEncryptEnum.sha256);
//                                String[] split = content.split(",");
                                StringBuilder sb = new StringBuilder();
                                sb.append(cell);
                                sb.append("\r\n");
                                writer.append(sb.toString());
                            }

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
                    rownum++;
                }
                br.close();
                read.close();
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
                writer.close();
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
            }
            log.warn("合并文件结束--耗时：{}", System.currentTimeMillis() - l);
        }

        return "123";
    }

    /**
     * requestTime=2023-02-01 -2023-10-23&convType=107（预估量级40万）
     * @param path
     * @return
     */
    @GetMapping("/cellDecByFileThree")
    public String cellDecByFileThree(@RequestParam(value = "path") String path){
        long l = System.currentTimeMillis();
        FileReader read = null;
        BufferedReader br = null;
        File file1 = new File(path);
        File[] files = file1.listFiles();
        for (File file : files) {
            ExecutorService mergeExecutor = BrExecutors.getThreadPool(100, 100);
            String[] fileSplit = file.getPath().split("\\.");
            String wFilePath = fileSplit[0] + "_transferData." + fileSplit[1];
            File wFile = new File(wFilePath);
            try {
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(
                                new FileOutputStream(wFile), StandardCharsets.UTF_8));
                int rownum = 1;
                read = new FileReader(file.getPath());
                br = new BufferedReader(read);
                String row;
                Set<String> set = new HashSet<>();
                while ((row = br.readLine()) != null) {
                    String content = row;
                    Integer threaNum = rownum;
                    LocalDate beginDate = LocalDate.parse("2023-02-01", DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                    LocalDate endDate = LocalDate.parse("2023-10-23", DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                    mergeExecutor.submit(()->{
                        try {
                            if(new Integer(1).equals(threaNum)){
                                StringBuilder sb = new StringBuilder();
                                sb.append(content.trim());
                                sb.append("\r\n");
                                writer.append(sb.toString());
                            }else{
                                String[] split = content.split(",");
                                String cell = split[0];
                                String convType = split[1];
                                String requestTime = split[2];
                                LocalDate reqTime = LocalDateTime.parse(requestTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")).toLocalDate();
                                if ("107".equals(convType) && (reqTime.isAfter(beginDate) || reqTime.isEqual(beginDate)) && (reqTime.isBefore(endDate) || reqTime.isEqual(endDate))) {
                                    StringBuilder sb = new StringBuilder();
                                    sb.append(cell);
                                    sb.append("\r\n");
                                    writer.append(sb.toString());
                                    set.add(cell);
                                }
                            }

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
                    rownum++;
                }
                br.close();
                read.close();
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
                writer.close();
                log.warn("rownum=" + rownum);
                log.warn("setNum=" + set.size());
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
            log.warn("合并文件结束--耗时：{}", System.currentTimeMillis() - l);
        }

        return "123";
    }

    @GetMapping("/replaceContentByFile")
    public String replaceContentByFile(@RequestParam(value = "path") String path){
        long l = System.currentTimeMillis();
        FileReader read = null;
        BufferedReader br = null;
        File file1 = new File(path);
        File[] files = file1.listFiles();
        for (File file : files) {
            ExecutorService mergeExecutor = BrExecutors.getThreadPool(100, 100);
            String[] fileSplit = file.getPath().split("\\.");
            String wFilePath = fileSplit[0] + "_phoneAction." + fileSplit[1];
            File wFile = new File(wFilePath);
            try {
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(
                                new FileOutputStream(wFile), StandardCharsets.UTF_8));
                int rownum = 1;
                AtomicInteger error = new AtomicInteger();
                AtomicInteger success = new AtomicInteger();
                read = new FileReader(file.getPath());
                br = new BufferedReader(read);
                String row;
                while ((row = br.readLine()) != null) {
                    String content = row;
                    mergeExecutor.submit(()->{
                        try {
                            if(StringUtils.isNotBlank(content)) {
                                StringBuilder sb = new StringBuilder();
                                sb.append(content.replace("\t", ","));
                                sb.append("\r\n");
                                writer.append(sb.toString());
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
                    rownum++;
                }
                br.close();
                read.close();
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
                writer.close();
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
            }
            log.warn("合并文件结束--耗时：{}", System.currentTimeMillis() - l);
        }

        return "123";
    }

    String concent(String a){
        String replace = a.trim().replace("\"", "");
        String b = "NULL".equals(replace)?"": replace;
        return b;
    }
}
