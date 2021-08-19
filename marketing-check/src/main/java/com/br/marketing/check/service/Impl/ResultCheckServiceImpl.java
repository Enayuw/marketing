package com.br.marketing.check.service.Impl;

import com.br.marketing.check.service.ResultCheckService;
import com.br.marketing.client.SftpClient;
import com.br.marketing.common.utils.Constants;
import com.br.marketing.common.utils.DateHelper;
import com.br.marketing.common.utils.file.MyFileUtil;
import com.br.marketing.entity.LoanFile;
import com.br.marketing.entity.MarketingTask;
import com.br.marketing.mapper.LoanFileMapper;
import com.br.marketing.mapper.MarketingUserMapper;
import com.br.marketing.service.Impl.BusinessAlarmServiceImpl;
import com.jcraft.jsch.SftpATTRS;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

@Slf4j
@Service
public class ResultCheckServiceImpl implements ResultCheckService {
    private final static Pattern SPLIT_PATTERN=Pattern.compile("_");
    private final static Pattern SPLIT_PATTERN_PATH=Pattern.compile("/");
    @Resource
    LoanFileMapper loanFileMapper;
    @Resource
    BusinessAlarmServiceImpl businessAlarmServiceImpl;
    @Resource
    MarketingUserMapper marketingUserMapper;
    @Value("${otherConfig.warning.sftpHost:00}")
    private String sftpHost;
    @Value("${otherConfig.warning.sftpPort:00}")
    private Integer sftpPort;
    @Value("${otherConfig.warning.sftpUser:00}")
    private String sftpUsername;
    @Value("${otherConfig.warning.sftpPwd:00}")
    private String sftpPwd;
    /**
     *   3004761_bairongniankuanguserinfo20200528_3004761_20200529221040_6931_DTB0000003_20200530_20200530.zip
     *   3005390_UploadCustomFileName20200608_9_3005390_20200608202301_1611_DTB0000001_20200609_20200609.zip
     * @param apiCode
     */
    @Override
    public  void taskResultCheck(String apiCode){
        Map<String, SftpATTRS> stringSftpATTRSMap;
        Map<String, SftpATTRS> signFileList;
        try {
            stringSftpATTRSMap = getFileList(apiCode,".zip");
             signFileList = getFileList(apiCode, ".complete");
        } catch (IOException e) {
            log.error("获取ftp上结果文件信息出错",e);
            return;
        }
        for(Map.Entry<String,SftpATTRS> entry :stringSftpATTRSMap.entrySet()){
            String name = entry.getKey();
            SftpATTRS sftpATTRS = entry.getValue();
            if(name.endsWith(".zip")){
                LoanFile loanFile = fileInfo(apiCode, sftpATTRS, name);
                checkFileSize(apiCode, loanFile,name);
                if(loanFile.isSkip()){
                    continue;
                }
                /**
                 * 3005913_3005913_20201028103006_1064_20201029.complete
                 * 3004761_3004761_20200703081734_6647_1_20201029.complete
                 * 3005390_3005390_20201024120005_2014_20201029.complete
                 * 校验内部标识文件是否正常上传
                 */
                String signFileName=apiCode+"_"+loanFile.getBatchNumber()+"_"+DateHelper.getDateAddYyMmDd(0)+".complete";
                if(!signFileList.keySet().contains(signFileName)){
                    log.error("批次对应的内部标识文件未上传，请关注:{},{}",signFileList.keySet(),signFileName);
                    loanFileMapper.updateSignFileStatus(apiCode);
                }
                checkFileline(loanFile);
            }
        }
        businessAlarmServiceImpl.resultVolumeCheck(apiCode);

    }

    /**
     * 校验结果文件行数和当前批次应该返回的行数是否一致
     * @param loanFile 文件信息
     */
    private void checkFileline( LoanFile loanFile) {
        int sum = getTxtFileLines(loanFile.getFilePath(), loanFile.getApiCode(), loanFile.getBatchNumber());
        String[] split = SPLIT_PATTERN_PATH.split(loanFile.getFilePath());
        if(split.length<7){
            log.warn("loanFile.getFilePath() is error{}",loanFile.getFilePath());
            return;
        }
        String type = split[6];
        MarketingTask marketingTask = new MarketingTask();
        marketingTask.setApiCode(loanFile.getApiCode());
        marketingTask.setBatchNumber(loanFile.getBatchNumber());
        if("incr".equals(type)){
            marketingTask.setTableName("b_marketing_user_chg");
        }else if("all".equals(type)||"once".equals(type)){
            marketingTask.setTableName("b_marketing_user_"+loanFile.getApiCode());
        }
        Integer expectedNum = loanFile.getExpectedNum();
        if(expectedNum==null){
            expectedNum = marketingUserMapper.queryCount(marketingTask);
        }
        if(sum!=expectedNum){
            log.warn("{}该批次实际返回数据量{}和应该返回数据量{}不一致",loanFile.getBatchNumber(),sum,expectedNum);
        }
        loanFile.setFileNum(1);
        loanFile.setActualNum(sum);
        loanFileMapper.updateFtpFileInfo(loanFile);

    }

    /**
     * 校验文件大小是否合法
     * 校验ftp上文件大小和本地磁盘上文件大小是否一致
     * @param apiCode apiCode
     * @param loanFile 文件信息
     */
    private void checkFileSize(String apiCode, LoanFile loanFile,String name) {
        long size = Long.parseLong(loanFile.getFileSize());
        if(size==0){
            log.error("{}文件大小为0",name);
            loanFileMapper.updateSignFileStatus(apiCode);
            businessAlarmServiceImpl.fileSizeException(apiCode,name+","+loanFile.getFileSize());
        }else {
            File file = new File(loanFile.getFilePath() +"/"+ name);
            long length = file.length();
            if(length!=size){
                log.error("{}本地文件大小{}B与sftp上文件大小{}B不一致",name,length,loanFile.getFileSize());
                loanFileMapper.updateSignFileStatus(apiCode);
                businessAlarmServiceImpl.fileUploadFtpException(apiCode,name+","+length+","+loanFile.getFileSize());
            }

        }
    }

    /**
     * 获取结果文件的信息
     * @param apiCode apiCode
     * @param sftpATTRS sftp上的文件对象
     * @param name 文件名称
     * @return ResultFileInfo
     */
    private  LoanFile fileInfo(String apiCode,SftpATTRS sftpATTRS, String name) {
        log.warn("sftpATTRS:{}",sftpATTRS);
        LoanFile loanFile;
        String uploadTime= DateHelper.timeStamp2Date(sftpATTRS.getMTime() + "", "yyyy-MM-dd HH:mm:ss");
        String batchNumber="";
        boolean flag=false;
        String[] split = SPLIT_PATTERN.split(name);
        if(split.length<5){
            log.warn("name is error{}",name);
        }else {
            batchNumber=split[2]+"_"+split[3]+"_"+split[4];
        }

        Map<String,String> param=new HashMap<>();
        param.put("batchNumber",batchNumber);
        param.put("apiCode",apiCode);
        param.put("fileName",name);
        log.warn("param:{}",param);
        loanFile = loanFileMapper.queryFilePath(param);
        loanFile.setFileSize(String.valueOf(sftpATTRS.getSize()));
        loanFile.setUploadTime(uploadTime);
        loanFile.setSkip(flag);
        log.warn("LoanFile:{}",loanFile);
        return loanFile;
    }

    private  Map<String, SftpATTRS>  getFileList(String apiCode,String suffix) throws IOException {
        Map<String, SftpATTRS> stringSftpATTRSMap=new HashMap<>();
        String dir="/UploadFiles/marketing/"+apiCode+"/output/"+ DateHelper.getDateAddYyMmDd(0)+"/";
        SftpClient sftpClient = new SftpClient(sftpHost,sftpPort,sftpUsername,sftpPwd);
        try {
            sftpClient.connect();
            stringSftpATTRSMap = sftpClient.listFiles(dir, suffix);
        } catch (Exception e) {
           log.error("Exception",e);
        }finally {
            try {
                sftpClient.disconnect();
            } catch (Exception e) {
                log.error("Exception",e);
            }
        }
        return stringSftpATTRSMap;
    }


    /**
     * 获取当前目录下的所有数据文件的总行数
     * @param filePath 压缩包文件全路径
     * @param apiCode apiCode
     * @param batchNumber 批次号
     * @return txt文件的总行数
     */
    private int getTxtFileLines(String filePath,String apiCode,String batchNumber){
        File dir=new File(filePath);
        if(!dir.exists()){
            log.warn("路径{} 不存在",filePath);
        }
        File[] files = dir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                if(name.startsWith(apiCode)&&name.contains(batchNumber)&&name.endsWith(".txt")&&!name.contains("_bi_")){
                    return true;
                }
                return false;
            }
        });
        int totalLines=0;
        for(int i=0;i<files.length;i++){
             totalLines = (totalLines+MyFileUtil.getTotalLines(files[i]))-1;
        }
        return totalLines;
    }
}
