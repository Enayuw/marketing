//package com.br.marketing.push.service.impl;
//
//import com.alibaba.fastjson.JSONArray;
//import com.br.marketing.client.*;
//import com.br.marketing.common.utils.DateHelper;
//import com.br.marketing.common.utils.StringUtils;
//import com.br.marketing.common.utils.file.FtpUtil2;
//import com.br.marketing.common.utils.file.ZipUtil;
//import com.br.marketing.entity.*;
//import com.br.marketing.mapper.LoanFileMapper;
//import com.br.marketing.mapper.LoanTaskMapper;
//import com.br.marketing.mapper.LoanTaskStatusMapper;
//import com.br.marketing.push.PushApplication;
//import com.br.marketing.push.service.PushService;
//import com.br.marketing.push.util.FileUtil;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Service;
//
//import javax.annotation.Resource;
//import java.io.File;
//import java.util.*;
//
///**
// * Created by Bairong on 2020/6/1.
// */
//@Service
//@Slf4j
//public class PushPpdServiceImpl implements PushService{
//    @Resource
//    LoanTaskMapper loanTaskMapper;
//    @Resource
//    LoanFileMapper loanFileMapper;
//    @Resource
//    DtbStrategyClient dtbStrategyClient;
//    @Value("${otherConfig.warning.path:00}")
//    private String path;
//    @Value("${otherConfig.warning.ruleList:00}")
//    private String rules;
//    @Value("${otherConfig.warning.ftpHost:00}")
//    private String ftpHost;
//    @Value("${otherConfig.warning.ftpPort:00}")
//    private Integer ftpPort;
//    @Value("${otherConfig.warning.ftpUsername:00}")
//    private String ftpUsername;
//    @Value("${otherConfig.warning.ftpPwd:00}")
//    private String ftpPwd;
//
//    @Resource
//    LoanTaskStatusMapper loanTaskStatusMapper;
//
//    @Resource
//    ProFieldsClient proFieldsClient;
//
//    @Override
//    public void process(String apiCode) {
//        try{
//            if(StringUtils.isNotEmpty(apiCode)){
//                List<LoanFile> incrList=new ArrayList<>();
//                List<LoanFile> allList=new ArrayList<>();
//                List<LoanFile> onceList=new ArrayList<>();
//                initBatchNumList(incrList,allList,onceList,apiCode);
//                this.pushAllAndOnce(allList,apiCode);
//            }
//
//        }catch (Exception e){
//            log.error("error-----",e);
//        }
//    }
//
//    @Override
//    public void push(List<LoanFile> files) {
//
//    }
//
//    /**
//     * 从redis中获取策略贷中的全部产品信息
//     * @return
//     *
//     */
//    public   Map<String,String> getAllLoanProductsVersion(String apiCode,String strategyid) {
//        Map<String,String> proMap=new HashMap<>();
//        if(strategyid.startsWith("DTB")){
//            dtbStrategyClient.needReturnProduct(apiCode,strategyid,proMap);
//        }
//        return proMap;
//    }
//    private void pushAllAndOnce(List<LoanFile> allList,  String apiCode) {
//        List<LoanFile> allFiles=new ArrayList<>();
//        for(LoanFile blf:allList){
//            String zipName = mergeResultFile(blf);
//            blf.setZipFileName(zipName);
//            allFiles.add(blf);
//        }
//        if(allFiles.size()>0){
//            PushPpdServiceImpl pushService= PushApplication.ac.getBean(PushPpdServiceImpl.class);
//            pushService.push(allFiles,apiCode);
//        }
//    }
//    private void successUpLoad(LoanFile blf,Integer isSec,FtpUtil2 ftp){
//        try{
//            String apiCode=blf.getApiCode();
//            String batchNumber=blf.getBatchNumber();
//            String[] split = blf.getZipFileName().split("/");
//            String name = split[split.length - 1];
//            String successFileName=name+".success";
//            String destPath=path+"/ftp_data/"+apiCode+"/";
//            File writePath = new File(destPath);
//            if (!writePath.exists()) {
//                writePath.mkdirs();
//            }
//            String fileaName=destPath+apiCode+"_"+batchNumber+"_"+isSec+"_"+DateHelper.getDateAddYyMmDd(0)+".complete";
//            File completeFile=new File(fileaName);
//            boolean newFile = completeFile.createNewFile();
//            String s = destPath + successFileName;
//            File successFile=new File(s);
//            boolean newFile1 = successFile.createNewFile();
//            log.info("客户批次回传标识文件---success:{}---create{}---complete:{}-----create:{}",successFileName,newFile1,fileaName,newFile);
//            if(successFile.exists()){
//                ftp.upload(successFile);
//            }
//            if(completeFile.exists()){
//                ftp.upload(completeFile);
//            }
//        }catch (Exception e){
//            log.error("上传周期日回传标识文件文件出错---{}",e);
//        }
//
//    }
//    public void push(List<LoanFile> files, String apiCode){
//        log.warn("start push files api_code:{}--{}",apiCode,files.size());
//        String zipFileName="";
//        String errorFile="";
//        FtpUtil2 ftp=new FtpUtil2();
//        try {
//            String today =  DateHelper.getDateAddYyMmDd(0);
//            boolean connect = ftp.connect("/loanwarn/" + apiCode + "/output/"+today+"/", ftpHost, ftpPort, ftpUsername, ftpPwd);
//            if(!connect){
//                log.error("获取ftp链接出错");
//                return;
//            }
//            for(LoanFile blf:files){
//                String fileName = blf.getZipFileName();
//                log.warn("filename:{}",fileName);
//                File file = new File(fileName);
//                if(file.exists()){
//                    boolean upload = ftp.upload(file);
//                    if(upload){
//                        this.successUpLoad(blf,blf.getIsSec(),  ftp);
//                    }else {
//                        log.error("上传文件到ftp失败");
//                    }
//                    String[] split = fileName.split("/");
//                    String name = split[split.length - 1];
//                    if(name.indexOf("error")>-1){
//                        errorFile=name;
//                    }else {
//                        zipFileName=name;
//                    }
//                    log.warn("zipFile_name:{}",zipFileName);
//                }
//
//                blf.setZipFileName(zipFileName);
//                blf.setErrorFile(errorFile);
//                loanFileMapper.updateFile(blf);
//                LoanTaskStatus bts = new LoanTaskStatus();
//                bts.setBatchNumber(blf.getBatchNumber());
//                bts.setFileId(blf.getId());
//                loanTaskStatusMapper.updateTaskStatus(bts);
//            }
//
//        } catch (Exception e) {
//            log.error("上传文件到ftp出错",e);
//        }finally {
//            ftp.closeFtp();
//        }
//
//    }
//
//
//
//
//    /**
//     * 合并周期为1的增量、全量、一次性的结果文件
//     *
//     * @param blf
//     * @return
//     */
//    private String mergeResultFile(LoanFile blf){
//        List<String> result=new ArrayList<>();
//        StringBuilder targetPath=new StringBuilder();
//        targetPath.append(blf.getFilePath())
//                .append("/");
//        LoanTask blt = loanTaskMapper.queryBlt(blf.getBatchNumber());
//
//        LoanTaskStatus bts=loanTaskStatusMapper.queryNewestBts(blf.getBatchNumber());
//        String fileNameStr = blt.getFileName();
//        if(StringUtils.isEmpty(fileNameStr)){
//            log.error("文件名称为空 {}",blt);
//        }
//        String fileNamePreFix=fileNameStr.replace(".txt","");
//        String s = fileNamePreFix.split("_")[1];
//        String startTime = bts.getCreateTime();
//        if(!StringUtils.isEmpty(startTime)){
//            startTime=startTime.split(" ")[0];
//            startTime=startTime.replace("-","");
//        }else{
//            startTime= DateHelper.getDateAddYyMmDd(0);
//        }
//        String strategyId="";
//        if(blf.getIsSec()==1){
//            strategyId=blt.getSecStrategyId();
//        }else if (blf.getIsSec()==0){
//            strategyId=blt.getStrategyId();
//        }
//        if(StringUtils.isEmpty(strategyId)){
//            log.error("策略编号为空 {}",blt);
//        }
//        StringBuilder sb=new StringBuilder(targetPath.toString())
//                .append(blf.getApiCode())
//                .append("_")
//                .append(s)
//                .append("_")
//                .append(blf.getBatchNumber())
//                .append("_")
//                .append(strategyId.split(":")[0])
//                .append("_")
//                .append(startTime)
//                .append("_")
//                .append(DateHelper.getDateAddYyMmDd(0))
//                .append(".txt");
//        String fileName=sb.toString();
//        StringBuilder head= new StringBuilder();
//
//        initHead(head,blt.getApiCode(),strategyId);
//        FileUtil.mergeAll(head.toString(),fileName,targetPath.toString(),",");
//
//        String zipFile=fileName.replace(".txt",".zip");
//        ZipUtil.compress(fileName,zipFile);
//        result.add(zipFile);
//
//        return zipFile;
//    }
//
//    /**
//     * 初始化表头
//     * @param head
//     * @param apiCode
//     * @param strategyId
//     *
//     * .append("姓名").append(",").append("身份证号").append(",").append("证书号").append(",").append("手机号")
//    .append(",")
//     */
//    private void  initHead(StringBuilder head,String apiCode,String strategyId){
//        Map<String, String> proMap = getAllLoanProductsVersion(apiCode, strategyId.split(":")[0]);
//        head.append("request_time").append(",").append("batch_number").append(",").append("cus_num")
//                .append(",").append("strategy_id").append(",").append("version").append(",");
//        Set<String> products=new HashSet<>();
//        if(strategyId.startsWith("DTB")){
//            products=proMap.keySet();
//        }
//
//        String stmtKey="";
//        if(strategyId.split(":").length>1){
//            stmtKey=strategyId.split(":")[1];
//        }
//        log.warn("需要返回的数据产品--{}",products);
//        ProductField pf=new ProductField();
//        if(products.contains("speciallist_c")){
//            String specialListc = proFieldsClient.getProFields("SpecialList_c", proMap.get("speciallist_c"), apiCode,stmtKey);
//            if(StringUtils.isEmpty(specialListc)){
//                List<String> specialListField = pf.getSpecialListField();
//                for(String field:specialListField){
//                    head.append(field).append(",");
//                }
//            }else{
//                JSONArray array=JSONArray.parseArray(specialListc);
//                for (int i=0;i<array.size();i++){
//                    String field=array.getString(i);
//                    head.append(field).append(",");
//                }
//            }
//
//        }
//        if(products.contains("inforelation")){
//            String inforelation = proFieldsClient.getProFields("InfoRelation", proMap.get("inforelation"), apiCode,stmtKey);
//            if(StringUtils.isEmpty(inforelation)){
//                List<String> infoRelationField = pf.getInfoRelationField();
//                for(String field:infoRelationField){
//                    head.append(field).append(",");
//                }
//            }else {
//                JSONArray array=JSONArray.parseArray(inforelation);
//                for (int i=0;i<array.size();i++){
//                    String field=array.getString(i);
//                    head.append(field).append(",");
//                }
//            }
//        }
//        if(products.contains("applyloanstr")){
//            String applyloanstr = proFieldsClient.getProFields("ApplyLoanStr", proMap.get("applyloanstr"), apiCode,stmtKey);
//            if(StringUtils.isEmpty(applyloanstr)){
//                List<String> applyloanstrField = pf.getApplyloanstrField();
//                for(String field:applyloanstrField){
//                    head.append(field).append(",");
//                }
//            }else{
//                JSONArray array=JSONArray.parseArray(applyloanstr);
//                for (int i=0;i<array.size();i++){
//                    String field=array.getString(i);
//                    head.append(field).append(",");
//                }
//            }
//
//        }
//        if(products.contains("applyloanusury")){
//            String applyloanusury = proFieldsClient.getProFields("ApplyLoanUsury", proMap.get("applyloanusury"), apiCode,stmtKey);
//            if(StringUtils.isEmpty(applyloanusury)){
//                List<String> applyloanusuryField = pf.getApplyloanusuryField();
//                for(String field:applyloanusuryField){
//                    head.append(field).append(",");
//                }
//            }else{
//                JSONArray array=JSONArray.parseArray(applyloanusury);
//                for (int i=0;i<array.size();i++){
//                    String field=array.getString(i);
//                    head.append(field).append(",");
//                }
//            }
//
//        }
//        if(products.contains("executionlimited")){
//            String executionlimited = proFieldsClient.getProFields("ExecutionLimited", proMap.get("executionlimited"), apiCode,stmtKey);
//            if(StringUtils.isEmpty(executionlimited)){
//                List<String> executionlimitedField = pf.getExecutionlimitedField();
//                for(String field:executionlimitedField){
//                    head.append(field).append(",");
//                }
//            }else {
//                JSONArray array=JSONArray.parseArray(executionlimited);
//                for (int i=0;i<array.size();i++){
//                    String field=array.getString(i);
//                    head.append(field).append(",");
//                }
//            }
//
//        }
//
//        if(products.contains("consumptionfeature")){
//            String consumptionfeature = proFieldsClient.getProFields("ConsumptionFeature", proMap.get("consumptionfeature"), apiCode,stmtKey);
//            if(StringUtils.isEmpty(consumptionfeature)){
//                List<String> consumptionFeatureField = pf.getConsumptionFeatureField();
//                for(String field:consumptionFeatureField){
//                    head.append(field).append(",");
//                }
//            }else{
//                JSONArray array=JSONArray.parseArray(consumptionfeature);
//                for (int i=0;i<array.size();i++){
//                    String field=array.getString(i);
//                    head.append(field).append(",");
//                }
//            }
//
//        }
//
//        if(products.contains("netshopping")){
//            String netshopping = proFieldsClient.getProFields("NetShopping", proMap.get("netshopping"), apiCode,stmtKey);
//            if(StringUtils.isEmpty(netshopping)){
//                List<String> netshoppingField = pf.getNetshoppingField();
//                for(String field:netshoppingField){
//                    head.append(field).append(",");
//                }
//            }else {
//                JSONArray array=JSONArray.parseArray(netshopping);
//                for (int i=0;i<array.size();i++){
//                    String field=array.getString(i);
//                    head.append(field).append(",");
//                }
//            }
//        }
//
//        initHead1(head, apiCode, proMap, products, stmtKey, pf);
//
//    }
//
//    private void initHead1(StringBuilder head, String apiCode, Map<String, String> proMap, Set<String> products, String stmtKey, ProductField pf) {
//        if(products.contains("scorecust")){
//            List<String> scoreField = pf.getScoreField();
//            for(String field:scoreField){
//                head.append(field).append(",");
//            }
//        }
//
//        if(products.contains("scoredata")){
//            head.append("flag_scoredata").append(",");
//            List<String> scoredataField = pf.getScoredataField();
//            for(String field:scoredataField){
//                head.append(field).append(",");
//            }
//        }
//
//        if(products.contains("scorecust1")){
//            List<String> scoreField = pf.getScore1Field();
//            for(String field:scoreField){
//                head.append(field).append(",");
//            }
//        }
//
//
//        /**
//         * 稳定性指数
//         */
//        if(products.contains("stability_c")){
//            String stabilityc = proFieldsClient.getProFields("Stability_c", proMap.get("stability_c"), apiCode,stmtKey);
//            if(StringUtils.isEmpty(stabilityc)){
//                List<String> stability = pf.getStabilitField();
//                for (String field:stability){
//                    head.append(field);
//                    head.append(",");
//                }
//            }else{
//                JSONArray array=JSONArray.parseArray(stabilityc);
//                for (int i=0;i<array.size();i++){
//                    String field=array.getString(i);
//                    head.append(field).append(",");
//                }
//            }
//
//        }
//
//        /**
//         * 借贷意向衍生特征
//         */
//        if(products.contains("applyfeature")){
//            String applyFeature = proFieldsClient.getProFields("ApplyFeature", proMap.get("applyfeature"), apiCode,stmtKey);
//            if(StringUtils.isEmpty(applyFeature)){
//                List<String> applyFeatures = pf.getApplyFeatureField();
//                for (String field:applyFeatures){
//                    head.append(field);
//                    head.append(",");
//                }
//            }else{
//                JSONArray array=JSONArray.parseArray(applyFeature);
//                for (int i=0;i<array.size();i++){
//                    String field=array.getString(i);
//                    head.append(field).append(",");
//                }
//            }
//        }
//        /**
//         * 借贷行为验证
//         */
//        if(products.contains("totalloan")){
//            String totalLoan = proFieldsClient.getProFields("TotalLoan", proMap.get("totalloan"), apiCode,stmtKey);
//            if(StringUtils.isEmpty(totalLoan)){
//                List<String> totalloan = pf.getTotalloanField();
//                for (String field:totalloan){
//                    head.append(field);
//                    head.append(",");
//                }
//            }else {
//                JSONArray array=JSONArray.parseArray(totalLoan);
//                for (int i=0;i<array.size();i++){
//                    String field=array.getString(i);
//                    head.append(field).append(",");
//                }
//            }
//        }
//
//        /**
//         * 反欺诈风险识别-信用卡（类信用卡）
//         */
//        if(products.contains("scoreafrevoloan")){
//            List<String> scoreafrevoloan = pf.getScoreafrevoloanField();
//            for (String field:scoreafrevoloan){
//                head.append(field);
//                head.append(",");
//            }
//        }
//        /**
//         * 客制化-信用风险识别-线上现金分期-拍拍贷老客标签一
//         */
//        if (products.contains("scorecashonppdlklabel1")) {
//                head.append("flag_scorecashonppdlklabel1").append(",").append("scpl1_score").append(",");
//        }
//
//        /**
//         * 客制化-信用风险识别-线上现金分期-拍拍贷老客标签一点二
//         */
//        if (products.contains("scorecashonppdlklabel12")) {
//            head.append("flag_scorecashonppdlklabel12").append(",").append("scpl12_score").append(",");
//        }
//
//        /**
//         * 客制化-信用风险识别-线上现金分期-拍拍贷老客标签一拒绝
//         */
//        if (products.contains("scorecashonppdlklabel1rej")) {
//            head.append("flag_scorecashonppdlklabel1rej").append(",").append("scpr_score").append(",");
//        }
//
//        /**
//         * 客制化-信用风险识别-线上现金分期-拍拍贷老客标签二
//         */
//        if (products.contains("scorecashonppdlklabel2")) {
//            head.append("flag_scorecashonppdlklabel2").append(",").append("scpl2_score").append(",");
//        }
//
//        /**
//         * 客制化-信用风险识别-线上现金分期-拍拍贷老客标签一点一
//         */
//        if (products.contains("scorecashonppdlklabel11")) {
//            head.append("flag_scorecashonppdlklabel11").append(",").append("scpl11_score").append(",");
//        }
//    }
//
//
//    /**
//     * 初始化当日需要监控的任务信息，并将增量监控和全量监控区分开来
//     * @param incrList
//     * @param allList
//     */
//    private void initBatchNumList(List<LoanFile> incrList, List<LoanFile> allList, List<LoanFile> onceList, String apiCode){
//
//        List<LoanFile> list= loanFileMapper.queryFile(apiCode);
//        for(LoanFile blf :list){
//           if(blf.getType()==1){
//                allList.add(blf);
//            }
//        }
//        log.info("[PUSH] incr size:{},all size:{},once size:{}",incrList.size(),allList.size(),onceList.size());
//    }
//}
