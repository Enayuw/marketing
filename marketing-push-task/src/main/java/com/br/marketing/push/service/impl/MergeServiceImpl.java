package com.br.marketing.push.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.client.ProFieldsClient;
import com.br.marketing.client.StrategyClient;
import com.br.marketing.common.bean.Score;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.utils.*;
import com.br.marketing.common.utils.file.MyFileUtil;
import com.br.marketing.common.utils.file.ZipUtil;
import com.br.marketing.entity.*;
import com.br.marketing.mapper.LoanFileMapper;
import com.br.marketing.mapper.MarketingTaskMapper;
import com.br.marketing.mapper.TaskStatusMapper;
import com.br.marketing.push.service.MergeService;
import com.br.marketing.push.util.FileUtil;
import com.br.marketing.service.IProductResultSimpleService;
import com.br.marketing.service.Impl.StrategyCs;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.ReadContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.*;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.*;
import java.util.regex.Pattern;

/**
 * //				    _ooOoo_
 * //				   o8888888o
 * //				   88" . "88
 * //				   (| -_- |)
 * //				   O\  =  /O
 * //			    ____/`---'\____
 * //			  .'  \\|     |//  `.
 * //		     /  \\|||  :  |||//  \
 * //		    /  _|||||--:--|||||_  \
 * //		    | / | \\\  -  /// | \ |
 * //		    | \_|  ''\-:-/''  |_/ |
 * //		    \  .-\__  `-`  ___/-. /
 * //		  ___`...'  /--.--\  '...`___
 * //	   ."" '< `.___\_<|>_/___.'  >' "".
 * //	   | | : `- \`.;`\ _ /`;.`/ -` : | |
 * //	    \ \ `-.  \_ __\ /__ _/  .-` / /
 * // ======`-.____`-.____\____/.-`____.-`======
 * //				    `=---='
 * //^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
 * //			  Buddha Bless, No Bug !
 *
 * @Author xiaoxin.pang
 * @Date 2021/5/7 15:08
 * @Description:
 **/
@Service
@Slf4j
public class MergeServiceImpl implements MergeService {
    @Resource
    MarketingTaskMapper marketingTaskMapper;
    @Resource
    StrategyCs strategyCS;
    @Resource
    ProFieldsClient proFieldsClient;
    @Resource
    TaskStatusMapper taskStatusMapper;
    @Resource
    LoanFileMapper loanFileMapper;

    @Autowired
    IProductResultSimpleService iProductResultSimpleService;

    private Map<String,String> proFieldMap=new HashMap<>();
    private static final Pattern MYREGEX1 = Pattern.compile("_");

    @Value("${otherConfig.warning.ruleList:00}")
    private String rules;
    @Value("${otherConfig.warning.path:00}")
    private String path;
    @Override
    public List<LoanFile> process(Customer customer) {
        List<LoanFile> pushList =new ArrayList<>();
        try{
            if(customer !=null){
                List<LoanFile> allList=new ArrayList<>();
                List<LoanFile> onceList=new ArrayList<>();
                initBatchNumList(allList,onceList,customer.getApiCode());
                pushList.addAll(mergeAllOrOnce(allList,customer));
                pushList.addAll(mergeAllOrOnce(onceList,customer));
            }

        }catch (Exception e){
            log.error("error-----",e);
        }
        return pushList;
    }



    private  List<LoanFile> mergeAllOrOnce(List<LoanFile> loanFileList,Customer customer) {
        List<LoanFile> pushList=new ArrayList<>();
        for(LoanFile blf:loanFileList){
            String zipName = mergeResultFile(blf,customer);
            if(StringUtils.isEmpty(zipName)){
                continue;
            }
            String[] split = zipName.split("/");
            String name = split[split.length - 1];
            blf.setZipFileName(name);
            loanFileMapper.updateFile(blf);

            pushList.add(blf);
        }
       return pushList;
    }

    /**
     * 合并周期为1的增量、全量、一次性的结果文件
     *
     * @param blf
     * @return
     */
    private String mergeResultFile(LoanFile blf,Customer customer){
        String zipFile="";
        try{
            StringBuilder targetPath=new StringBuilder();
            targetPath.append(blf.getFilePath())
                    .append("/");
            MarketingTask blt = marketingTaskMapper.queryBlt(blf.getBatchNumber());
            String s ="";
            if(blt==null){
                log.error("不存在的批次：{}",blf);
                return zipFile;

            }else {
                String fileName1 = blt.getFileName();
                fileName1=fileName1.replace(".txt","");
                s = MYREGEX1.split(fileName1)[1];
            }
            Result<String> baseHeadInfoByTaskId = iProductResultSimpleService.getBaseHeadInfoByTaskId(Long.valueOf(blt.getId().toString()));
            String baseHeadInfo = "";
            if(ResultCode.SUCCESS.getValue().equals(baseHeadInfoByTaskId.getCode())){
                baseHeadInfo = baseHeadInfoByTaskId.getData();
            }
            String dataInfo = "";
            Result<String> fieldsInfo = iProductResultSimpleService.getFieldsStrInfo(blt.getApiCode(), blt.getStrategyId());
            if(ResultCode.SUCCESS.getValue().equals(fieldsInfo.getCode())){
                dataInfo = fieldsInfo.getData();
            }
            String strategyStr = strategyCS.strategyIdCheck(blt.getApiCode(), blt.getStrategyId());
            if(StringUtils.isEmpty(strategyStr)){
                return zipFile;
            }
            proFieldsClient.setLoanPro(blt.getStrategyId(),blt.getApiCode(),strategyStr,new JSONObject(),proFieldMap,"");
            String startTime = blf.getCreateTime();
            startTime=startTime.split(" ")[0].replace("-","");

            String  strategyId=blt.getStrategyId();
            String fileName=blf.getApiCode().concat("_").concat(s).concat("_").concat(blf.getBatchNumber()).concat("_").concat(strategyId.split(":")[0])
                    .concat("_").concat(startTime).concat("_").concat(DateHelper.getDateAddYyMmDd(0)).concat(".txt");
            String filePathAndName=targetPath.toString().concat(fileName);
            StringBuilder head= new StringBuilder();
            Integer sep= marketingTaskMapper.querySep(blt.getApiCode());
            String separator=Constants.sepMap.get(sep);
            initHead(head,blt.getApiCode(),strategyId,separator,baseHeadInfo,dataInfo);
            FileUtil.mergeAll(head.toString(),filePathAndName,targetPath.toString(),separator);

            zipFile=filePathAndName.replace(".txt",".zip");
            Integer total =MyFileUtil.getTotalLines(new File(filePathAndName))-1;
            blf.setExpectedNum(total);
            if(customer.getPushCustomer()==1){
                ArrayList<String> countFileNameList =standard(filePathAndName,separator,total);

                //统计文件上传fastdfs
                uploadFastDfs(countFileNameList,blf,fileName);

                countFileNameList.add(filePathAndName);
                ZipUtil.compress(zipFile,countFileNameList);
            }else {
                ZipUtil.compress(filePathAndName,zipFile);
            }

        }catch (Exception e){
            log.error("合并文件出错",e);
        }finally {
            proFieldMap.clear();
        }
        return zipFile;
    }
    private void uploadFastDfs(ArrayList<String> countFileNameList,LoanFile blf,String fileName){
        try{
            fileName=fileName.replace(".txt",".zip");
            String filePath=blf.getFilePath().concat("/fastdfs/");
            File dir=new File(filePath);
            if(!dir.exists()||!dir.isDirectory()){
                boolean mkdirs = dir.mkdirs();
                if(!mkdirs){
                    log.error("创建文件夹失败-{}",filePath);
                    return ;
                }
            }

            String filePathAndName=filePath.concat(fileName);
            ZipUtil.compress(filePathAndName,countFileNameList);

            byte[] buffer;
            FileInputStream in=new FileInputStream(new File(filePathAndName));
            OutputStream outputStream = new ByteArrayOutputStream();
            byte[] b = new byte[1024];
            int n = 0;
            while ((n = in.read(b)) != -1){
                outputStream.write(b, 0, n);
            }
            buffer = ((ByteArrayOutputStream) outputStream).toByteArray();
            String  url = FastdfsUtils.uploadDFSFileByte(buffer,fileName);
            blf.setStatisticFilePath(url);
            blf.setScoreStatus(2);

        }catch (Exception e){
            log.error("上传fastdfs异常，{}",blf.getFilePath());
        }
    }
    private ArrayList<String> standard(String fileName, String separator, Integer total) {
        ArrayList<String> fileNameList = new ArrayList<>();
        try {
            StringBuilder head = MyFileUtil.gethead(fileName);
            String headStr = head.toString();
            String[] headArray = headStr.split(separator);
            Set<String> products = new HashSet<>();
            for (String pro : proFieldMap.keySet()) {
                log.info("pro:{}", pro);
                products.add(pro.toLowerCase());
            }
            String countFileHead = "scoring_range,sample_capacity,proportion,cumulative_proportion";
            countFileHead = countFileHead.replace(",", separator);
            for (String product : products) {
                if (headStr.contains(product)) {
                    ArrayList<Score> scores = initScoreList(300, 1000, 25);
                    int i = findIndex(headArray, product);
                    readFile(fileName, scores, i, separator);
                    count(scores, total);
                    StringBuilder end = new StringBuilder();
                    end.append("_bi_").append(product).append(".txt");
                    String countFileName = fileName.replace(".txt", end.toString());
                    FileUtil.writeFile(countFileHead, countFileName, scores, separator);
                    fileNameList.add(countFileName);
                }
            }
        } catch (IOException e) {
            log.error("获取文件头异常", e);
        }
        return fileNameList;
    }

    private void count(ArrayList<Score> scores,Integer total){
        if(total !=null &&total.compareTo(0)==0){
            total=total+1;
        }
        BigDecimal total1=new BigDecimal(total);
        BigDecimal cumulativeProportion = new BigDecimal(0);
        for (Score score : scores) {
            BigDecimal sampleCapacity=new BigDecimal(score.getSampleCapacity());
            BigDecimal proportion=sampleCapacity.divide(total1,5,BigDecimal.ROUND_HALF_UP);
            cumulativeProportion = cumulativeProportion.add(proportion);
            score.setProportion(proportion);
            score.setCumulativeProportion(cumulativeProportion);
        }
    }

    private void readFile(String fileName,ArrayList<Score> scores,int index,String separator){
        try(FileReader read = new FileReader(fileName);
            BufferedReader br = new BufferedReader(read)) {
            String row;
            while ((row = br.readLine()) != null) {
                row = row.trim();
                if(StringUtils.isNotEmpty(row)){
                    if(row.indexOf("request_time")==-1){
                        String[] rowArray =row.split(separator);
                        String score="";
                        if(index<rowArray.length){
                             score=rowArray[index];
                        }
                        if(StringUtils.isNotEmpty(score)){
                            setScore(scores,Float.valueOf(score).intValue(),300,1000,25);
                        }
                    }
                }
            }
        } catch (FileNotFoundException e) {
            log.error("FileNotFoundException ",e);
        } catch (IOException e) {
            log.error("IOException ",e);
        }
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
    private  void  setScore(ArrayList<Score> scoreList,Integer score,Integer min,Integer max,Integer range){
       if(score !=null){
           Integer index =(score-min)/range;
           if(score<min||score>max){
               return;
           }
           if(score.compareTo(max)==0){
               index=index-1;
           }
           Score data = scoreList.get(index);
           data.setSampleCapacity(data.getSampleCapacity()+1);
       }
    }
    private  ArrayList<Score> initScoreList(Integer min,Integer max,Integer range){
        ArrayList<Score> scores=new ArrayList<>();
        Integer index =(max-min)/range;
        for (int i=0;i<index;i++){
            Score score = new Score();
            StringBuilder builder =new StringBuilder();
            if(i==index-1){
                builder.append("[").append(min+range*i).append("-").append(min+(i+1)*range).append("]");
            }else{
                builder.append("[").append(min+range*i).append("-").append(min+(i+1)*range).append(")");
            }
            score.setScoringRange(builder.toString());
            score.setSampleCapacity(0);
            scores.add(score);
        }
        return scores;
    }

    private List<String> mergeResultFile(LoanFile blf, String date){
        List<String> result=new ArrayList<>();
        try{
            String filePath = blf.getFilePath();
            filePath= filePath.substring(0,filePath.length()-10);
            StringBuilder targetPath=new StringBuilder();
            targetPath.append(filePath).append(date)
                    .append("/");
            MarketingTask blt = marketingTaskMapper.queryBlt(blf.getBatchNumber());
            Result<String> baseHeadInfoByTaskId = iProductResultSimpleService.getBaseHeadInfoByTaskId(Long.valueOf(blt.getId().toString()));
            String baseHeadInfo = "";
            if(ResultCode.SUCCESS.getValue().equals(baseHeadInfoByTaskId.getCode())){
                baseHeadInfo = baseHeadInfoByTaskId.getData();
            }
            String dataInfo = "";
            Result<String> fieldsInfo = iProductResultSimpleService.getFieldsStrInfo(blt.getApiCode(), blt.getStrategyId());
            if(ResultCode.SUCCESS.getValue().equals(fieldsInfo.getCode())){
                dataInfo = fieldsInfo.getData();
            }
            String strategyStr = strategyCS.strategyIdCheck(blt.getApiCode(), blt.getStrategyId());
            proFieldsClient.setLoanPro(blt.getStrategyId(),blt.getApiCode(),strategyStr,new JSONObject(),proFieldMap,"");
            String fileName1 = blt.getFileName();
            fileName1=fileName1.replace(".txt","");
            String s = fileName1.split("_")[1];
            String startTime = blf.getCreateTime();
            startTime=startTime.split(" ")[0].replace("-","");

            String fileName=targetPath.toString()+blf.getApiCode()+"_"+s+"_"+blf.getBatchNumber()+"_"
                    +blt.getStrategyId()+"_"+startTime+"_"+DateHelper.getDateAddYyMmDd(0)+".txt";
            StringBuilder head= new StringBuilder();
            Integer sep= marketingTaskMapper.querySep(blt.getApiCode());
            String separator=Constants.sepMap.get(sep);
            initHead(head,blt.getApiCode(),blt.getStrategyId(),separator,baseHeadInfo,dataInfo);
            FileUtil.mergeAll(head.toString(),fileName,targetPath.toString(),separator);
            result.add(fileName);
            String errorFileName=targetPath.toString()+blf.getApiCode()+"_"+s+"_error_"+DateHelper.getDateAddYyMmDd(0)+".txt";
            boolean b = FileUtil.mergeError(errorFileName, targetPath.toString());
            if(b){
                result.add(errorFileName);
            }
        }catch (Exception e){
            log.error("合并出错--{}",e);
        }finally {
            proFieldMap.clear();
        }

        return result;
    }

    /**
     * 初始化表头
     * @param head
     * @param apiCode
     * @param strategyId
     *
     * .append("姓名").append(",").append("身份证号").append(",").append("证书号").append(",").append("手机号")
    .append(",")
     */
    private void  initHead(StringBuilder head,String apiCode,String strategyId,String sep,String baseHeadInfo,String dataInfo){
        List<String> list;
        head.append("request_time").append(sep).append("batch_number").append(sep).append("cus_num")
                .append(sep).append("strategy_id").append(sep).append("version").append(sep);
        if(StringUtils.isNotBlank(baseHeadInfo.trim())){
            head.append(baseHeadInfo).append(sep);
        }
        if(strategyId.startsWith("STRB")){
            head.append("strategyDecision").append(sep);
            String strategy = StrategyClient.getStrategy(apiCode, strategyId);
            JSONObject strategyJson = JSONObject.parseObject(strategy);
            if(StringUtils.isNotEmpty(strategy)&&"1".equals(strategyJson.getString("status"))&&"0".equals(strategyJson.getString("canUse"))){
                JSONObject ruleType = JSONObject.parseObject(strategy).getJSONObject("ruleType");
                if(ruleType!=null&&!ruleType.isEmpty()&&"1".equals(ruleType.getString("status"))){
                    list = this.sortRuleList(ruleType);
                    for(String key:list){
                        head.append("flag_").append(key).append(sep);
                        head.append("rulerisk").append(sep);
                        head.append("weight").append(sep);
                        List<String> ruleFields = getRuleField(key);
                        if(ruleFields!=null&&ruleFields.size()>0){
                            for(String rule:ruleFields){
                                head.append(rule).append(sep);
                            }
                        }
                    }
                }
            }
        }
        Set<String> products=new HashSet<>();
        for(String pro:proFieldMap.keySet()){
            log.info("pro:{}",pro);
            products.add(pro.toLowerCase());
        }
        if(StringUtils.isNotBlank(dataInfo)){
            head.append(dataInfo).append(sep);
        }else {
            appendProInfo(head, sep, products);
        }
    }


    private List<String>  getRuleField(String ruleType){
        RuleField rf=new RuleField();
        if("Rule_W_SpecialList_c_mix_c".equals(ruleType)){
            return rf.getRuleSpecialListField();
        }else if("Rule_W_InfoRelation_mix_c".equals(ruleType)){
            return rf.getRuleInfoRelationField();
        }else if("Rule_W_ApplyLoanStr_mix_c".equals(ruleType)){
            return rf.getRuleApplyloanstrField();
        }else if("Rule_W_ApplyLoanUsury_mix".equals(ruleType)){
            return rf.getRuleApplyloanusuryField();
        }else if("Rule_W_ExecutionLimited_mix".equals(ruleType)){
            return rf.getRuleExecutionlimitedField();
        }
        return new ArrayList<>();
    }

    /**
     * 对策略中的规则集进行排序
     * @param ruleType
     * @return
     */
    private List<String> sortRuleList(JSONObject ruleType){
        List<String> result=new ArrayList<>();
        ReadContext context = JsonPath.parse(ruleType);
        String[] split = rules.split(",");
        for(int i=0;i<split.length;i++){
            Object read = context.read("$..ruleTypeList[?(@.ruleType=='"+split[i]+"')]");
            if(read!=null){
                JSONArray array = JSONArray.parseArray(read.toString());
                if(array!=null&&array.size()>0){
                    result.add(split[i]);
                }
            }
        }
        return result;
    }

    private void appendProInfo(StringBuilder head,String sep, Set<String> products) {
        log.info("需要返回的数据产品--{}",products);
        if(products.contains("scorencashonszyxxy")){
            String fields = PropertiesUtil.getProperty("scorencashonszyxxy");
            String[] split = fields.split(",");
            for (int i=0;i<split.length;i++){
                head.append(split[i]).append(sep);
            }
        }
        if(products.contains("scoremcashonxhqbdzcd")){
            String fields = PropertiesUtil.getProperty("scoremcashonxhqbdzcd");
            String[] split = fields.split(",");
            for (int i=0;i<split.length;i++){
                head.append(split[i]).append(sep);
            }
        }
        if(products.contains("scoremcashon360xktwo")){
            String fields = PropertiesUtil.getProperty("scoremcashon360xktwo");
            String[] split = fields.split(",");
            for (int i=0;i<split.length;i++){
                head.append(split[i]).append(sep);
            }
        }
        if(products.contains("scorebrevoloanmszd3")){
            String fields = PropertiesUtil.getProperty("scorebrevoloanmszd3");
            String[] split = fields.split(",");
            for (int i=0;i<split.length;i++){
                head.append(split[i]).append(sep);
            }
        }

    }

    /**
     * 初始化当日需要监控的任务信息，并将增量监控和全量监控区分开来
     * @param allList
     */
    private void initBatchNumList( List<LoanFile> allList, List<LoanFile> onceList, String apiCode){

        List<LoanFile> list= loanFileMapper.queryFile(apiCode);
        for(LoanFile blf :list){
            if(blf.getType()==2){
                onceList.add(blf);
            }else if(blf.getType()==1){
                allList.add(blf);
            }
        }
        log.info("[PUSH]all size:{},once size:{}",allList.size(),onceList.size());
    }

}
