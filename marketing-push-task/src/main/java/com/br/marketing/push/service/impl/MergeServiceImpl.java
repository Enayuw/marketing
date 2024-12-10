package com.br.marketing.push.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.client.ProFieldsClient;
import com.br.marketing.common.bean.Score;
import com.br.marketing.common.utils.DateHelper;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.common.utils.file.MyFileUtil;
import com.br.marketing.common.utils.file.ZipUtil;
import com.br.marketing.entity.Customer;
import com.br.marketing.entity.LoanFile;
import com.br.marketing.entity.MarketingTask;
import com.br.marketing.mapper.LoanFileMapper;
import com.br.marketing.mapper.MarketingTaskMapper;
import com.br.marketing.mapper.TaskStatusDistributeMapper;
import com.br.marketing.push.service.MergeService;
import com.br.marketing.push.util.FileUtil;
import com.br.marketing.service.IProductResultSimpleService;
import com.br.marketing.service.Impl.StrategyCs;
import com.br.marketing.service.MarketingSepService;
import com.google.common.base.Joiner;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.*;
import java.math.BigDecimal;
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
    LoanFileMapper loanFileMapper;
    @Resource
    MarketingSepService marketingSepService;
    @Autowired
    IProductResultSimpleService iProductResultSimpleService;
    @Resource
    TaskStatusDistributeMapper taskStatusDistributeMapper;

    private Map<String,String> proFieldMap=new HashMap<>();
    private static final Pattern MYREGEX1 = Pattern.compile("_");
    @Override
    public List<LoanFile> process(Customer customer) {
        return process(null,customer);
    }

    @Override
    public List<LoanFile> process(List<LoanFile> fileList, Customer customer) {
        List<LoanFile> pushList =new ArrayList<>();
        try{
            if(fileList != null && fileList.size()>0){
                pushList =mergeAllOrOnce(fileList,customer);
            }else{
                List<LoanFile> list= loanFileMapper.queryFile(customer.getApiCode());
                pushList =mergeAllOrOnce(list,customer);
            }
        }catch (Exception e){
            log.error("error-----",e);
        }
        return pushList;
    }

    private  List<LoanFile> mergeAllOrOnce(List<LoanFile> loanFileList, Customer customer) {
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
    public String mergeResultFile(LoanFile blf,Customer customer){
        String zipFile="";
        try{
            StringBuilder targetPath=new StringBuilder();
            targetPath.append(blf.getFilePath())
                    .append("/");
            MarketingTask blt = marketingTaskMapper.queryBlt(blf.getBatchNumber());
            Boolean isOffLine = new Integer(2).equals(blt.getIsOnline());
            String s ="";
            if(blt==null){
                log.error("不存在的批次：{}",blf);
                return zipFile;

            }else {
                String fileName1 = blt.getFileName();
                fileName1=fileName1.replace(".txt","");
                s = MYREGEX1.split(fileName1)[1];
            }

            String startTime = blf.getCreateTime();
            startTime=startTime.split(" ")[0].replace("-","");

            String  strategyId=blt.getStrategyId();
            String fileName=blf.getApiCode().concat("_").concat(s).concat("_").concat(blf.getBatchNumber()).concat("_").concat(strategyId)
                    .concat("_").concat(startTime).concat("_").concat(DateHelper.getDateAddYyMmDd(0)).concat(".txt");
            String filePathAndName=targetPath.toString().concat(fileName);
            StringBuilder head= new StringBuilder();
            String separator=blt.getScoreSeparator();
            iProductResultSimpleService.initHead(head,separator,blt);
            Integer fileNum = isOffLine?300000000:30000000;
            if(!isOffLine&&StringUtils.isNotBlank(customer.getExtendConfigInfo())){
                try {
                    JSONObject extendJb = JSON.parseObject(customer.getExtendConfigInfo());
                    Integer fileNum1 = extendJb.getInteger("fileNum");
                    if(fileNum1!=null){
                        fileNum = fileNum1;
                    }
                }catch (Exception ex){
                    log.error(ex.getMessage(),ex);
                }
            }
            List<String> paths = FileUtil.mergeAll(head.toString(), filePathAndName, targetPath.toString(), separator, fileNum);
            zipFile = filePathAndName.replace(".txt", ".zip");
            Integer total = 0;
            List<String> names = new ArrayList<>();
            for (String path1 : paths) {
                String[] split = path1.split("\\/");
                String name = split[split.length - 1];
                names.add(name);
                total +=MyFileUtil.getTotalLines(new File(path1))-1;
            }
            blf.setActualNum(total);
            blf.setFileName(Joiner.on(",").join(names));
//            Result<ConfigByApiCodeVO> configByApiCode = iProductResultSimpleService.getConfigByApiCode(customer.getApiCode());
            if(new Integer(1).equals(customer.getPushCustomer())&&!isOffLine){
                blf.setScoreStatus(2);
            }
            if(!isOffLine){
                ZipUtil.compress(zipFile,paths);
            }
//            if(ResultCode.SUCCESS.getValue().equals(configByApiCode.getCode())
//            &&new Integer(1).equals(configByApiCode.getData().getIsFast())){
//                ArrayList<String> countFileNameList =standard(filePathAndName,separator,total);
//
//                //统计文件上传fastdfs
//                uploadFastDfs(countFileNameList,blf,fileName);
//
//                countFileNameList.add(filePathAndName);
//                ZipUtil.compress(zipFile,countFileNameList);
//            }else {
//                ZipUtil.compress(zipFile,paths);
//            }

        }catch (Exception e){
            log.error("合并文件出错",e);
        }finally {
            proFieldMap.clear();
        }
        return zipFile;
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
        } catch (Exception e) {
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


    /**
     * 初始化表头
     * @param head
     *
     * .append("姓名").append(",").append("身份证号").append(",").append("证书号").append(",").append("手机号")
    .append(",")
     */
    private void  initHead(StringBuilder head,String sep,String baseHeadInfo,String dataInfo,Integer taskType){
//        if(taskType.compareTo(new Integer(1))==0){
//            if(StringUtils.isNotBlank(baseHeadInfo.trim())){
//                head.append(baseHeadInfo).append(sep);
//            }
//            return;
//        }
        head.append("request_time").append(sep).append("batch_number").append(sep).append("cus_num")
                .append(sep).append("strategy_id").append(sep).append("version").append(sep);
        if(StringUtils.isNotBlank(baseHeadInfo.trim())){
            head.append(baseHeadInfo).append(sep);
        }
        if(StringUtils.isNotBlank(dataInfo)){
            head.append(dataInfo).append(sep);
        }
    }
}
