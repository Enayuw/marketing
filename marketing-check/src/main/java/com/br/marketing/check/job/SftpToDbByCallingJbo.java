package com.br.marketing.check.job;

import com.br.marketing.check.dto.FileContext;
import com.br.marketing.check.service.Impl.SftpToDbByCommonService;
import com.br.marketing.check.service.Impl.SftpToDbByDXService;
import com.br.marketing.check.utils.SftpToDbUtils;
import com.br.marketing.client.SftpClient;
import com.br.marketing.common.utils.DateHelper;
import com.br.marketing.common.utils.MQConstants;
import com.br.marketing.entity.LocalFile;
import com.br.marketing.mapper.LocalFileMapper;
import com.br.marketing.service.ITxtToDbService;
import com.dangdang.ddframe.job.api.ElasticJob;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import com.jcraft.jsch.SftpATTRS;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Pattern;


/**
 * @author guangchao.zhang
 * @Classname SftpToDbByCallingJbo
 * @Description 客服拨打明细落库
 * @Date 2022/2/11 2:25 PM
 */
@Component
@Slf4j
public class SftpToDbByCallingJbo extends AbstractSimpleElasticJob {
    @Value("${otherConfig.warning.path:00}")
    private String path;
    @Value("${otherConfig.warning.sftpHost:00}")
    private String sftpHost;
    @Value("${otherConfig.warning.sftpPort:00}")
    private Integer sftpPort;
    @Value("${otherConfig.warning.sftpUser:00}")
    private String sftpUsername;
    @Value("${otherConfig.warning.sftpPwd:00}")
    private String sftpPwd;
    private static final Pattern FILE_NAME_REGEX = Pattern.compile("^[0-9]{7}");

    private static final String CALLING_SFTP_PATH = "/UploadFiles/marketing-calling";




    @Autowired
    SftpToDbByDXService sftpToDbByDXService;

    @Autowired
    LocalFileMapper localFileMapper;

    @Autowired
    SftpToDbByCommonService sftpToDbByCommonService;



    @Autowired
    ITxtToDbService iTxtToDbService;

    @Override
    public void process(JobExecutionMultipleShardingContext jobExecutionMultipleShardingContext) {

        /** 1. 根据用户配置表 查询用户配置信息**/
        /** 2. 根据配置表的信息到指定路径拉取文件**/
        /** 2.1 判断当前路径是否为文件夹，若是文件夹则继续进入拉取文件**/
        /** 2.2 判断文件后缀，将需要入去的文件放入set中**/
        /** 3 根据set的内容，读取指定文件信息**/
        /** 3.1 判断表信息是否正常**/
        /** 4 落库**/
        /** 5 落库**/
        /** 5.1 记录落库处理信息日志**/




        System.out.println(System.currentTimeMillis());
        SftpClient sftpClient = new SftpClient(sftpHost, sftpPort,sftpUsername,sftpPwd);
        try {
            sftpClient.connect();
            Map<String, Set<String>> map = new HashMap<>();
            Map<String, SftpATTRS> attrsMap = sftpClient.listFiles(CALLING_SFTP_PATH);
            for (Map.Entry<String, SftpATTRS> entry : attrsMap.entrySet()) {

                String fileName = entry.getKey();
                SftpATTRS attrs = entry.getValue();
                if(attrs.isDir()) {
                    if (FILE_NAME_REGEX.matcher(fileName).matches() || "input".equals(fileName)) {
                        log.debug("isDirectory file:{}", fileName);
                        listStpFile(CALLING_SFTP_PATH + "/" + fileName, map, sftpClient);
                    }
                }
                log.warn(fileName);
            }
            if(!map.isEmpty()){
                for (Map.Entry<String, Set<String>> entry : map.entrySet()) {
                    String srcPath = entry.getKey();
                    Set<String> fileNames = entry.getValue();
                    for(String fileName : fileNames){
                        if(fileName.endsWith(".txt")){
                            FileContext context = new FileContext();
                            context.setBaseFtpClient(sftpClient);
                            context.setSftpZipFilePath(srcPath);
                            context.setApiCode("123123");
                            context.setTxtFileName(fileName);
                            String successFile = fileName + ".success";
                            //if (fileNames.contains(successFile)) {
                                context.setLocalTxtFilePath(path.concat("marketing-calling/").concat("7410437").concat("/"));
                                //if(!sftpToDbByDXService.dowloadFile(context)){
                                //    continue;
                                //}
                                LocalFile localFile = new LocalFile();
                                localFile.setApiCode("7410437");
                                localFile.setSrcPath(context.getSftpZipFilePath());
                                localFile.setFileName(context.getTxtFileName());
                                localFile.setLocalPath(context.getLocalTxtFilePath());
                                localFile.setStatus("1");
                                localFile.setCreateTime(new Date());
                                localFile.setFileType("haluo");
                                //localFileMapper.insertSelective(localFile);

                                try {
                                    String yyyyMMddHHmmss = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
                                    String name =  srcPath + successFile;
                                    String rename = srcPath + successFile+"_"+yyyyMMddHHmmss+".bak";
                                    sftpClient.rename(srcPath+"/" + successFile, srcPath+"/" + successFile+"_"+yyyyMMddHHmmss+".bak");
                                    sftpClient.rename(srcPath+"/" + fileName, srcPath+"/" + fileName+"_"+yyyyMMddHHmmss+ ".bak");
                                        ArrayList<String> baseHeads = new ArrayList<String>(Arrays.asList("custNum","callStartTime","groupType","taskId"));
                                        //sftpToDbByCommonService.actionTxtFile(context
                                        //        , localFile
                                        //        , baseHeads
                                        //        , MQConstants.ROUTING_KEY_MARKETING_PUSH_DASS_SCORE
                                        //        , iTxtToDbService::phoneTodbByXW);

//                            sftpToDbByDXService.actionTxtFile(context,localFile);
                                } catch (Exception e) {
                                    log.warn("rename file error ", e);
                                    try {
                                        sftpClient.rename(srcPath + successFile, srcPath + successFile + ".bak");
                                        sftpClient.rename(srcPath + fileName, srcPath + fileName + ".bak");
                                        sftpClient.disconnect();
                                        sftpClient.connect();
                                    } catch (Exception ex) {
                                        log.error("rename file error ", ex);
                                    }
                                }
                            }
                        }
                    }
                }
            //}
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }


    public static void listStpFile(String path, Map<String, Set<String>> map, SftpClient sftpClient) {
        try {
            Map<String, SftpATTRS> attrsMap = sftpClient.listFiles(path);
            for (Map.Entry<String, SftpATTRS> entry : attrsMap.entrySet()) {
                String fileName = entry.getKey();
                SftpATTRS attrs = entry.getValue();
                if (attrs.isDir()) {
                    log.debug("fileName:{}", fileName);
                    if (FILE_NAME_REGEX.matcher(fileName).matches() || "input".equals(fileName)) {
                        log.debug("isDirectory file:{}", fileName);
                        listStpFile(path + fileName + "/", map, sftpClient);
                    }
                } else {
                    String createFileTime = DateHelper.timeStamp2Date(attrs.getMTime() + "", "yyyy-MM-dd HH:mm:ss");
                    long minutes = DateHelper.getDistanceMinutes(createFileTime);
                    if (minutes < 1) {
                        log.warn("文件上传时间距离当前时间小于1分钟，暂时不处理");
                        continue;
                    }
                    if (StringUtils.isNotEmpty(fileName) && (fileName.endsWith(".zip")
                            || fileName.endsWith(".finish") || fileName.endsWith(".success"))) {
                        Set<String> set = map.get(path);
                        if (set == null) {
                            set = new HashSet<>();
                            map.put(path, set);
                        }
                        set.add(fileName.substring(0,fileName.length()-8));
                    }
                }
            }
        } catch (Exception e) {
            log.error("遍历sftp文件出错", e);
        }
        if(!map.isEmpty()){
            log.warn("map :{}", map);
        }
    }
}
