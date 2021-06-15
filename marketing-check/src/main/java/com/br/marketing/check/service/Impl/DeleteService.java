//package com.br.marketing.check.service.Impl;
//
//import com.br.marketing.check.dto.FileContext;
//import com.br.marketing.check.enums.ErrorFileTypeEnum;
//import com.br.marketing.check.service.AbstractDataToDbService;
//import com.br.marketing.check.thread.ValidatorDeleteMonitorFileThread;
//import com.br.marketing.check.utils.SftpToDbUtils;
//import com.br.marketing.client.BaseFtpClient;
//import com.br.marketing.client.DecodeClient;
//import com.br.marketing.client.RedisChgService;
//import com.br.marketing.client.SftpClient;
//import com.br.marketing.common.utils.BrExecutors;
//import com.br.marketing.common.utils.Constants;
//import com.br.marketing.common.utils.DateHelper;
//import com.br.marketing.common.utils.StringUtils;
//import com.br.marketing.common.utils.file.MyFileUtil;
//import com.br.marketing.entity.LoadResult;
//import com.br.marketing.entity.MarketingTask;
//import com.br.marketing.mapper.LoadResultMapper;
//import lombok.extern.slf4j.Slf4j;
//import org.apache.commons.collections.map.HashedMap;
//import org.springframework.stereotype.Service;
//
//import javax.annotation.Resource;
//import java.io.*;
//import java.nio.charset.StandardCharsets;
//import java.nio.file.Files;
//import java.nio.file.Paths;
//import java.util.*;
//import java.util.concurrent.ExecutorService;
//
///**
// * @Author: Bairong
// * @Time: 2020/12/9 15:06
// * @Company：百融
// * @Description: 功能描述
// */
//@Service
//@Slf4j
//public class DeleteService extends AbstractDataToDbService {
//
//    /**
//     * The Load result mapper.
//     */
//    @Resource
//    LoadResultMapper loadResultMapper;
//    /**
//     * The File ckeck servicce.
//     */
//    @Resource
//    FileCheckServiceImpl fileCheckService;
//
//    @Resource
//    DecodeClient decodeClient;
//    /**
//     * The Redis chg service.
//     */
//    @Resource
//    RedisChgService redisChgService;
//    private final static Integer SPLITSIZE=5000;
//
//    private final static Integer BATCHSIZE=2000;
//
//    @Override
//    public Boolean checkTxtFile(FileContext context) {
//        ExecutorService validatorExecutor = BrExecutors.getThreadPool(5,5);
//        StringBuilder errorMessage=new StringBuilder("数据文件异常,");
//        if(!fileCheckService.checkTxtfile(context,errorMessage)){
//            return false;
//        }
//        String txtFilePathAndName=context.getLocalDeleteFilePath().concat(context.getZipFileName());
//        StringBuilder head;
//        int totalLines = MyFileUtil.getTotalLines(new File(txtFilePathAndName));
//        if(totalLines==0){
//            errorMessage.append("文件内容为空");
//            fileCheckService.errorDetail(context,errorMessage.toString(),ErrorFileTypeEnum.ERROR_FILE);
//            return false;
//        }
//
//        try {
//         head=MyFileUtil.gethead(txtFilePathAndName);
//        } catch (IOException e) {
//            log.error("获取表头失败",e);
//            return false;
//        }
//
//
//
//        if(!SftpToDbUtils.checkHead(context,head.toString())){
//            log.error("文件表头异常-文件名-{}，head-{}",context.getTxtFileName(),head);
//            errorMessage.append("文件表头异常");
//            fileCheckService.errorDetail(context,errorMessage.toString(),ErrorFileTypeEnum.ERROR_FILE);
//            return false;
//        }
//
//        log.info("================开始去重============");
//        int pageSize= (totalLines + SPLITSIZE-1) /SPLITSIZE;
//        File[] files = MyFileUtil.splitFileByCusNum(txtFilePathAndName,pageSize);
//        MyFileUtil.distinctByCusNum(files,context.getDistinctTxtFilePath(),context.getDistinctTxtFileName(),pageSize,head);
//        log.info("================去重结束============");
//
//
//        try (Writer fw= new BufferedWriter(
//                new OutputStreamWriter(
//                        Files.newOutputStream(Paths.get(context.getErrorFilePath().concat(context.getErrorDataFileName()))), StandardCharsets.UTF_8));
//             FileReader read = new FileReader(context.getDistinctTxtFilePath().concat(context.getDistinctTxtFileName()));
//             BufferedReader br = new BufferedReader(read);) {
//            Map<String,Integer> headIndexMap=new HashMap();
//            if(StringUtils.isNotEmpty(head)){
//                String[] headArray = head.toString().split(",");
//                headIndexMap.put("batchNumberIndex",findIndex(headArray, "batch_number"));
//                headIndexMap.put("cusNumIndex",findIndex(headArray, "cus_num"));
//                headIndexMap.put("idIndex",findIndex(headArray, "id"));
//                headIndexMap.put("cellIndex",findIndex(headArray, "cell"));
//                headIndexMap.put("nameIndex",findIndex(headArray, "name"));
//                fw.append("error_message,"+head+"\n");
//            }
//            String row;
//            int linenumber = 0;
//            Set<String> list=new HashSet<>();
//            while ((row = br.readLine()) != null) {
//                log.debug("row:{}",row);
//                String trim = row.trim();
//                trim=trim.replaceAll(Constants.MYREGEX1, "");
//                if(StringUtils.isNotEmpty(trim)){
//                    linenumber++;
//                    if(linenumber %BATCHSIZE==0){
//                        validatorExecutor.submit(new ValidatorDeleteMonitorFileThread(list, context, headIndexMap, fw,decodeClient));
//                        list=new HashSet<>();
//                    }
//                    list.add(trim);
//                }
//            }
//            if(!list.isEmpty()){
//                validatorExecutor.submit(new ValidatorDeleteMonitorFileThread(list, context, headIndexMap, fw,decodeClient));
//
//            }
//            validatorExecutor.shutdown();
//            while (true){
//                if(validatorExecutor.isTerminated()){
//                    log.info("所有线程都执行结束");
//                    break;
//                }
//                try {
//                    Thread.sleep(3000);
//                }catch (Exception e){
//                    log.error("等待所有任务都执行完成",e);
//                }
//            }
//            fw.close();
//            String s2 = context.getTxtFileName().toUpperCase();
//            String s = redisChgService.get(Constants.DELETE_MONITOR_ERROR + s2);
//            String s1 = redisChgService.get(Constants.DELETE_MONITOR_SUCCESS + s2);
//            SftpClient sftpClient=context.getSftpClient();
//            String remotePath=Constants.SFTP_IN_ERROR_PATH.replace("apiCode",context.getApiCode());
//            if(StringUtils.isNotEmpty(s)&&Integer.parseInt(s)>0){
//                log.warn("匹配出错条数：{}",s);
//                String errorFilePathAndName=context.getLocalTxtFilePath().concat(context.getTxtFileName());
//                sftpClient.uploadFile(remotePath,context.getErrorFileName(),errorFilePathAndName);
//                File successFile=new File( errorFilePathAndName+".success");
//                successFile.createNewFile();
//                if(successFile.exists()){
//                    sftpClient.uploadFile(remotePath,context.getTxtFileName()+".success",remotePath+".success");
//                }
//                redisChgService.del(Constants.DELETE_MONITOR_ERROR + s2);
//            }
//            int actualNum=0;
//            if(StringUtils.isNotEmpty(s1)){
//                actualNum=Integer.parseInt(s1);
//            }
//
//            String sftpPath = "/UploadFiles/loanwarn/" + apiCode + "/input/";
//            sftpClient.rename(sftpPath+fileName,sftpPath+fileName+".bak");
//            LoadResult lr=new LoadResult(apiCode,cusBatch,fileName,"","1","",actualNum,linenumber,"delete");
//            log.info("LoadResult :{}",lr);
//            loadResultMapper.insertLoadResult(lr);
//            redisChgService.del(Constants.DELETE_MONITOR_SUCCESS + s2);
//        } catch (Exception e) {
//            log.error("parsingFile error",e);
//        }
//
//        return true;
//    }
//
//    @Override
//    public void checkConfigFile(FileContext context) {
//
//    }
//
//
//    /**
//     * 查找某个值在数组中的索引
//     * @param array 数组
//     * @param value 给定的值
//     * @return 索引
//     */
//    public static int findIndex(String[] array, String value) {
//        for (int i = 0; i < array.length; i++) {
//            if (array[i].equals(value)) {
//                return i;
//            }
//        }
//        return -1;
//    }
//}
