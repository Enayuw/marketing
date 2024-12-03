package com.br.marketing.innerapi.controller;

import com.alibaba.fastjson.JSONObject;
import com.br.common.util.BrCipherMaker;
import com.br.marketing.client.AlarmApiClient;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.common.utils.net.IpUtil;
import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 心跳检测
 */
@RestController
@RequestMapping("/ping")
public class PingController {

    @Resource
    private AlarmApiClient alarmClient;


    private static final Logger log = LoggerFactory.getLogger(PingController.class);

    /**
     * ping接口
     *
     * @return 当前时间戳
     */
    @GetMapping
    public String ping() {
        return "pong-" + System.currentTimeMillis();
    }

    @GetMapping("/logTest")
    public String logTest() {
        JSONObject json=new JSONObject();
        json.put("host", IpUtil.getHostName());
        json.put("serverName", "MARKETING-INNER-API");
        json.put("message", "测试报警内容 alarmClient.sendAlarm()");
        alarmClient.sendAlarm(json.toString(),"调用了测试报警接口,请忽略~", "1001");
        return "111";
    }

    @GetMapping("/logErrorTest")
    public String logErrorTest() {
        try{
            test();
        }catch (Exception e){
            log.error("测试报警接口，log.error()",e);
        }
        return "log.error()";
    }

    public void test(){
        int i = 20 / 0;
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
                                    }else{
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
}
