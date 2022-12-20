package com.br.marketing.tools.controller;

import com.br.marketing.common.utils.BrExecutors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

@Slf4j
@RestController
@RequestMapping("/file")
public class FileController {

    @GetMapping("/splitFile")
    public String splitFile(){
        long l = System.currentTimeMillis();
        ExecutorService mergeExecutor = BrExecutors.getThreadPool(100, 100);
        FileReader read = null;
        BufferedReader br = null;
        String pathName = "/opt/temp_file/id.txt";
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

            read = new FileReader("/opt/temp_file/1_1.txt");
            br = new BufferedReader(read);
            String row;
            while ((row = br.readLine()) != null) {
                String content=","+row;
                if(rownum>10000){
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
}
