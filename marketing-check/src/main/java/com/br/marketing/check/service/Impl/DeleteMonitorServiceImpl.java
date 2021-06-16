package com.br.marketing.check.service.Impl;

import com.br.marketing.check.service.DeleteMonitorService;
import com.br.marketing.check.thread.ValidatorDeleteMonitorFileThread;
import com.br.marketing.check.utils.DeleteFileUtil;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.common.utils.Constants;
import com.br.marketing.common.utils.DateHelper;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.common.utils.file.FtpUtil2;
import com.br.marketing.common.utils.file.MyFileUtil;
import com.br.marketing.common.utils.file.ZipUtil;
import com.br.marketing.common.utils.file.ZipUtils;
import com.br.marketing.entity.LoadResult;
import com.br.marketing.entity.MarketingTask;
import com.br.marketing.entity.MerchantParam;
import com.br.marketing.mapper.LoadResultMapper;
import com.br.marketing.mapper.MarketingDirtyUserMapper;
import com.br.marketing.mapper.MarketingTaskMapper;
import com.br.marketing.mapper.MarketingUserMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.regex.Pattern;

@Service
@Slf4j
public class DeleteMonitorServiceImpl implements DeleteMonitorService {

    @Resource
    MarketingDirtyUserMapper marketingDirtyUserMapper;
    @Resource
    LoadResultMapper loadResultMapper;
    @Resource
    RedisChgService redisChgService;
    @Resource
    MarketingTaskMapper marketingTaskMapper;
    @Resource
    MarketingUserMapper marketingUserMapper;
    private static final Pattern MYREGEX = Pattern.compile("\\.");
    private final static Integer BATCHSIZE=2000;
    private final static Integer SPLITSIZE=5000;
    private static String MYREGEX1="\\p{C}";
    @Override
    public void parsingFile(String key, String fileName, String  localFilePath, String apiCode,
                            MerchantParam merchantParam,String cusBatch, FtpUtil2 ftp) {
        log.info("parsingFile key：{} ,fileName:{},localFilePath:{},apiCode:{}",key,fileName,localFilePath,apiCode);
        ExecutorService validatorExecutor = BrExecutors.getThreadPool(5,5);
        String[] split = MYREGEX.split(fileName);
        String name = split[0];
        File dir=new File(localFilePath);
        if(!dir.exists()||!dir.isDirectory()){
            boolean mkdirs = dir.mkdirs();
            if(!mkdirs){
                return;
            }
        }
        StringBuilder sb=new StringBuilder();
        sb.append(localFilePath)
                .append(fileName);
        File file=new File(sb.toString());
        ftp.downFile(key,fileName,file);
        try {
            if("0".equals(merchantParam.getFileEncryptionMethods())){
                ZipUtil.unZip(file,localFilePath+"/"+name);
            }else if("2".equals(merchantParam.getFileEncryptionMethods())){
                ZipUtils.unZip(file,localFilePath+"/"+name,merchantParam.getFileEncryptionKey());
            }
        }catch (Exception e){
            log.error("解压失败",e);
            StringBuilder errorMessage=new StringBuilder("压缩文件异常,");
            errorMessage.append("压缩文件解密异常");
            DeleteFileUtil.returnErrorFile(apiCode,localFilePath, fileName, errorMessage,ftp);
            LoadResult lr=new LoadResult(apiCode,cusBatch,fileName,errorMessage.toString(),"0","",0,0,"delete");
            loadResultMapper.insertLoadResult(lr);
            return;
        }

        String txtFileName=fileName.replace(".zip",".txt");
        StringBuilder errorMessage=new StringBuilder("数据文件异常,");
        if(!checkTxtfile(fileName,apiCode,localFilePath+"/"+name,txtFileName,errorMessage,cusBatch,ftp)){
            return;
        }
        String deleteErrorFileName=apiCode+"_"+txtFileName.split("\\.")[0]+"_error_"+ DateHelper.getDateAddYyMmDd(0)+".txt";
        StringBuilder errorSb=new StringBuilder();
        errorSb.append(localFilePath).append("/").append(name).append("/")
                .append(deleteErrorFileName);
        File errorFile=new File(errorSb.toString());
        StringBuilder head=new StringBuilder();
        log.info("================开始去重============");
        int hash=0;
            int totalLines = MyFileUtil.getTotalLines(new File(localFilePath + "/" + name + "/" + txtFileName));
            hash= (totalLines + SPLITSIZE-1) /SPLITSIZE;
        File[] files = MyFileUtil.splitFile(localFilePath+"/"+name+"/"+txtFileName,hash);
        MyFileUtil.distinct(files,localFilePath+"/"+name+"/","result-"+txtFileName,hash);
        log.info("================去重结束============");
        try (Writer fw= new BufferedWriter(
                new OutputStreamWriter(
                        Files.newOutputStream(Paths.get(errorSb.toString())), StandardCharsets.UTF_8));
             FileReader read = new FileReader(localFilePath+"/"+name+"/"+"result-"+txtFileName);
             BufferedReader br = new BufferedReader(read);) {
            Map<String,Integer> headIndexMap=new HashMap();
            if(StringUtils.isNotEmpty(head)){
                String[] headArray = head.toString().split(",");
                headIndexMap.put("batchNumberIndex",findIndex(headArray, "batch_number"));
                headIndexMap.put("cusNumIndex",findIndex(headArray, "cus_num"));
                headIndexMap.put("idIndex",findIndex(headArray, "id"));
                headIndexMap.put("cellIndex",findIndex(headArray, "cell"));
                headIndexMap.put("nameIndex",findIndex(headArray, "name"));
                fw.append("error_message,"+head+"\n");
            }
            String row;
            int linenumber = 0;
            int num = 1;
            Set<String> list=new HashSet<>();
            while ((row = br.readLine()) != null) {
                log.debug("row:{}",row);
                String trim = row.trim();
                trim=trim.replaceAll(MYREGEX1, "");
                if(StringUtils.isNotEmpty(trim)){
                        linenumber++;
                        if((linenumber / BATCHSIZE) > (num - 1)){
                            num ++ ;
//                            validatorExecutor.submit(new ValidatorDeleteMonitorFileThread(list, apiCode, marketingDirtyUserMapper
//                                    , merchantParam, headIndexMap, fw,redisChgService,name));
                            list=new HashSet<>();
                        }
                        list.add(trim);
                }
            }

            /**
             * 处理文件中最后的不够2000条的数据
             */
//            if(list!=null&&list.size()>0){
//                validatorExecutor.submit(new ValidatorDeleteMonitorFileThread(list,apiCode, marketingDirtyUserMapper
//                        ,merchantParam,headIndexMap,fw,redisChgService,name));
//
//            }

            if(!checkTxtContent(linenumber+1,localFilePath+"/"+name,errorMessage,apiCode,fileName,head.toString(),cusBatch,ftp)){
                return;
            }
            /**
             * 等待所有任务都执行完成
             **/
            validatorExecutor.shutdown();
            while (true){
                if(validatorExecutor.isTerminated()){
                    log.info("所有线程都执行结束");
                    break;
                }
                try {
                    Thread.sleep(3000);
                }catch (Exception e){
                    log.error("等待所有任务都执行完成",e);
                }
            }
            fw.close();
            String s2 = name.toUpperCase();
            String s = redisChgService.get(Constants.DELETE_MONITOR_ERROR + s2);
            String s1 = redisChgService.get(Constants.DELETE_MONITOR_SUCCESS + s2);
            if(StringUtils.isNotEmpty(s)&&Integer.parseInt(s)>0){
                log.info("匹配出错条数：{}",s);
                ftp.changeWorkingDirectory("/loanwarn/"+apiCode+"/error/");
                ftp.upload(errorFile);
                File successFile=new File( errorSb.toString()+".success");
                successFile.createNewFile();
                if(successFile.exists()){
                    ftp.upload(successFile);
                }
                redisChgService.del(Constants.DELETE_MONITOR_ERROR + s2);
            }
            int actualNum=0;
            if(StringUtils.isNotEmpty(s1)){
                actualNum=Integer.parseInt(s1);
            }
            redisChgService.del(Constants.DELETE_MONITOR_SUCCESS + s2);
            ftp.changeWorkingDirectory("/loanwarn/"+apiCode+"/input/");
            ftp.rename(fileName,fileName+".bak");
            ftp.rename(fileName+".success",fileName+".success.bak");
            LoadResult lr=new LoadResult(apiCode,cusBatch,fileName,"","1","",actualNum,linenumber,"delete");
            log.info("LoadResult :{}",lr);
            loadResultMapper.insertLoadResult(lr);

            List<MarketingTask> marketingTaskList = marketingTaskMapper.queryMonitorBatch(apiCode);
            log.info("loanTaskList:{},apiCode:{}", marketingTaskList.size(),apiCode);
            for(MarketingTask lt: marketingTaskList){
                lt.setTableName("b_marketing_user_"+apiCode);
                Integer integer = marketingUserMapper.queryCount(lt);
                log.info("batch_number:{},actualnumber:{},count:{}",lt.getBatchNumber(),lt.getActualNumber(),integer);
                if(!integer.equals(lt.getActualNumber())){
                    lt.setActualNumber(integer);
                    marketingTaskMapper.updateTaskActualNumber(lt);
                }
            }
        } catch (Exception e) {
            log.error("parsingFile error",e);
        }
    }


    /**
     * 校验txt文件内容
     * @param linenumber 文件行数
     * @param localFilePath 当前路径
     * @param errorMessage 错误信息
     * @param apiCode 客户编号
     * @param fileName 压缩包文件名称
     * @param head 表头
     * @param ftp
     * @return 校验成功或者失败
     */
    public boolean checkTxtContent(int linenumber, String localFilePath, StringBuilder errorMessage,
                                   String apiCode, String fileName, String head, String cusBatch, FtpUtil2 ftp){
        if(linenumber==0){
            errorMessage.append("文件内容为空");
            DeleteFileUtil.returnErrorFile(apiCode, localFilePath, fileName, errorMessage,ftp);
            LoadResult lr=new LoadResult(apiCode,cusBatch,fileName,errorMessage.toString(),"0","",0,0,"delete");
            loadResultMapper.insertLoadResult(lr);
            log.error("txt 文件内容为空 ");
            return false;
        }else{
            boolean headFlag=true;
            if(StringUtils.isEmpty(head)){
                headFlag=false;
            }else{
                int cusNum = head.indexOf("cus_num");
                int id = head.indexOf("id");
                int cell = head.indexOf("cell");
                int name = head.indexOf("name");
                if(cusNum==-1||(id==-1&&cell==-1&&name==-1)){
                    headFlag=false;
                }
            }
            if(!headFlag){
                errorMessage.append("文件表头异常");
                DeleteFileUtil.returnErrorFile(apiCode, localFilePath, fileName, errorMessage,ftp);
                LoadResult lr=new LoadResult(apiCode,cusBatch,fileName,errorMessage.toString(),"0","",0,0,"delete");
                loadResultMapper.insertLoadResult(lr);
                log.error("txt 文件表头异常:{} ",head);
                return false;
            }
        }
        return true;
    }

    /**
     * 校验txt文件格式
     * @param fileName 压缩包文件名
     * @param apiCode 客户编号
     * @param localFilePath 当前路径
     * @param txtFileName 应有的txt文件名称
     * @param errorMessage 错误信息
     * @param ftp
     * @return 校验成功或者失败
     */
    private boolean checkTxtfile(String fileName, String apiCode, String localFilePath,
                                 String txtFileName, StringBuilder errorMessage, String cusBatch, FtpUtil2 ftp){
        File dir=new File(localFilePath);
        File[] fileList = dir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathName) {
                String name = pathName.getName();
                if(name.endsWith(".txt")){
                    return true;
                }
                return false;
            }
        });

        boolean flag=true;
        if(fileList==null||fileList.length==0){
            errorMessage.append("压缩文件找不到上传数据文件");
            DeleteFileUtil.returnErrorFile(apiCode, localFilePath, fileName, errorMessage,ftp);
            LoadResult lr=new LoadResult(apiCode,cusBatch,fileName,errorMessage.toString(),"0","",0,0,"delete");
            loadResultMapper.insertLoadResult(lr);
            return false;
        }
        if(fileList.length!=1){
            errorMessage.append("压缩文件找不到上传数据文件");
            flag=false;
        }else if(!fileList[0].getName().equals(txtFileName)){
            File file1 = fileList[0];
            String name = file1.getName();
            flag = DeleteFileUtil.vaildFileName(name, apiCode, errorMessage);
        }

        if(!flag){
            DeleteFileUtil.returnErrorFile(apiCode, localFilePath, fileName, errorMessage,ftp);
            LoadResult lr=new LoadResult(apiCode,cusBatch,fileName,errorMessage.toString(),"0","",0,0,"delete");
            loadResultMapper.insertLoadResult(lr);
            log.error("txt 文件名称错误 txt :{},zip:{}",fileList[0].getName(),fileName);
            return false;
        }
        return true;
    }
    /**
     * 查找某个值在数组中的索引
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
