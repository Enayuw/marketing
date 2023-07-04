package com.br.marketing.tools.controller;

import com.br.common.encryption.Sha256Util;
import com.br.common.util.BrCipherMaker;
import com.br.marketing.common.utils.BrExecutors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

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
                            String decode = BrCipherMaker.getInstance().decode(content.trim());
                            if(decode.equals(content.trim())){
                                log.error("错误数据");
                            }
                            if(new Integer(1).equals(threaNum)){
//                                String[] split = content.split(",");
                                StringBuilder sb = new StringBuilder();
                                sb.append(DigestUtils.md5DigestAsHex(decode.getBytes()));
//                                sb.append(",");
//                                sb.append(split[1].trim());
//                                sb.append(",");
//                                sb.append(split[2].trim());
////                                sb.append(",");
////                                sb.append(split[3].trim());
////                                sb.append(",");
////                                sb.append(split[4].trim());
                                sb.append("\r\n");
                                writer.append(sb.toString());
                            }else{
//                                String[] split = content.split(",");
                                StringBuilder sb = new StringBuilder();
                                sb.append(DigestUtils.md5DigestAsHex(decode.getBytes()));
//                                sb.append(",");
//                                sb.append(concent(split[1].trim()));
//                                sb.append(",");
//                                sb.append(concent(split[2]));
//                                sb.append(",");
//                                sb.append(concent(split[3]));
//                                sb.append(",");
//                                sb.append(DigestUtils.md5DigestAsHex(BrCipherMaker.getInstance().decode(split[4].trim()).getBytes()));
//                                sb.append(",");
//                                sb.append(concent(split[5]));
//                                sb.append(",");
//                                sb.append(concent(split[6]));
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
