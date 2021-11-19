package com.br.marketing.check.service.Impl;

import com.alibaba.fastjson.JSONObject;
import com.br.common.validator.CellUtils;
import com.br.marketing.check.dto.FileContext;
import com.br.marketing.check.enums.ErrorFileTypeEnum;
import com.br.marketing.check.utils.SftpToDbUtils;
import com.br.marketing.client.DecodeClient;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.client.SftpClient;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.utils.*;
import com.br.marketing.common.utils.file.MyFileUtil;
import com.br.marketing.dto.TxtToDbDTO;
import com.br.marketing.entity.LoadResult;
import com.br.marketing.entity.LocalFile;
import com.br.marketing.entity.MarketingTask;
import com.br.marketing.entity.PhoneSale;
import com.br.marketing.mapper.LoadResultMapper;
import com.br.marketing.mapper.LocalFileMapper;
import com.br.marketing.mapper.PhoneSaleMapper;
import com.br.marketing.rabbitmq.RabbitMqProducter;
import com.google.common.base.Function;
import com.google.common.base.Splitter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.map.HashedMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.*;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

/**
 * @Author: Bairong
 * @Time: 2020/12/9 15:06
 * @Company：百融
 * @Description: 功能描述
 */
@Service
@Slf4j
public class SftpToDbByCommonService {

    /**
     * The Load result mapper.
     */
    @Resource
    LoadResultMapper loadResultMapper;

    @Autowired
    RabbitMqProducter producter;
    /**
     * The File ckeck servicce.
     */
    @Resource
    FileCheckServiceImpl fileCheckService;
    /**
     * The Redis chg service.
     */
    @Resource
    RedisChgService redisChgService;

    @Autowired
    LocalFileMapper localFileMapper;

    @Autowired
    PhoneSaleMapper phoneSaleMapper;

    @Value("${api.dass.aesKey:00}")
    private String aesKey;

    @Autowired
    DecodeClient decodeClient;

    private static String phoneReg = "^([\\+]*[0-9]+)$";

    private final static Integer SPLITSIZE=5000;

    public Boolean dowloadFile(FileContext context) {
        String localFilePath=context.getLocalTxtFilePath();
        String zipFileName=context.getTxtFileName();
        SftpClient client =(SftpClient)context.getBaseFtpClient();
        File dir=new File(localFilePath);
        if(!dir.exists()||!dir.isDirectory()){
            boolean mkdirs = dir.mkdirs();
            if(!mkdirs){
                log.error("创建文件夹失败-{}",context.getLocalZipFilePath());
                return false;
            }
        }
        StringBuilder sb=new StringBuilder().append(localFilePath).append(zipFileName);
        boolean download = client.downloadFile(context.getSftpZipFilePath() , zipFileName, sb.toString());
        if(!download){
            log.error("文件下载出错-SftpZipFilePath={},zipFileName={}",context.getSftpZipFilePath(),zipFileName);
            return false;
        }
        return true;
    }

    public Boolean actionTxtFile(FileContext context, LocalFile localFile,List<String> baseHeads,String routKey,Function<TxtToDbDTO,Result> fuc) {
        String txtFilePathAndName=context.getLocalTxtFilePath().concat(context.getTxtFileName());
        StringBuilder head;
        int totalLines = MyFileUtil.getTotalLines(new File(txtFilePathAndName));
        if(totalLines==0){
            log.error(String.format("%s 文件内容为空",context.getTxtFileName()));
            LocalFile updateFile = new LocalFile();
            updateFile.setId(localFile.getId());
            updateFile.setComplete("4");
            localFileMapper.updateByPrimaryKeySelective(updateFile);
            return false;
        }
        head=MyFileUtil.gethead(txtFilePathAndName);

        HashMap<Integer, String> address = new HashMap<>();
        HashMap<Integer, String> extSetField = new HashMap<>();
        Result hashMapResult = SftpToDbUtils.statisticsHeadByCommon(head.toString(),address,extSetField,baseHeads);
        if(!ResultCode.SUCCESS.getValue().equals(hashMapResult.getCode())){
            log.error(String.format("%s 文件：%s",context.getTxtFileName(),hashMapResult.getMessage()));
            LocalFile updateFile = new LocalFile();
            updateFile.setId(localFile.getId());
            updateFile.setComplete("2");
            localFileMapper.updateByPrimaryKeySelective(updateFile);
            return false;
        }

        long start = System.currentTimeMillis();

        String filepath = context.getLocalTxtFilePath().concat(context.getTxtFileName());
        AtomicInteger errorMark = new AtomicInteger(0);
        try(
                FileReader read = new FileReader(filepath);
                BufferedReader br = new BufferedReader(read);) {
            String row;
            Integer line = 1;
            ThreadPoolExecutor threadPool = BrExecutors.getThreadPool(20, 20);
            while ((row = br.readLine()) != null) {
                String trim = row.trim();
                TxtToDbDTO txtToDbDTO = new TxtToDbDTO();
                txtToDbDTO.setLine(line);
                txtToDbDTO.setApiCode(localFile.getApiCode());
                txtToDbDTO.setLocalId(localFile.getId());
                txtToDbDTO.setContent(trim);
                txtToDbDTO.setAddress(address);
                txtToDbDTO.setExtSetField(extSetField);
                if(StringUtils.isNotEmpty(row)&&StringUtils.isNotEmpty(trim)){
                    if(line>1){
                        threadPool.submit(()->{
                            Result apply = fuc.apply(txtToDbDTO);
                            if(!ResultCode.SUCCESS.getValue().equals(apply.getCode())){
                                errorMark.getAndIncrement();
                            }
                        });
                    }
                }
                line++;
            }
            /**
             * 等待所有任务都执行完成
             **/
            threadPool.shutdown();
            while (true){
                if(threadPool.isTerminated()){
                    log.info("所有线程都执行结束");
                    break;
                }
                try {
                    Thread.sleep(3000);
                }catch (Exception e){
                }
            }
            LocalFile updateFile = new LocalFile();
            updateFile.setId(localFile.getId());
            updateFile.setActualNumber(line>0?line-1:line);
        if(errorMark.get()>0){
            updateFile.setComplete("3");
        }
            localFileMapper.updateByPrimaryKeySelective(updateFile);
            producter.send(routKey,localFile.getId().toString());
        }catch (Exception e){
            log.error(e.getMessage(),e);
        }
        long end = System.currentTimeMillis();
        if(log.isWarnEnabled()){
            log.warn(String.format("数据入库时长:%d",end-start));
        }
        return true;
    }
}
