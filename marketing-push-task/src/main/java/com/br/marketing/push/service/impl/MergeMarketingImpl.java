package com.br.marketing.push.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.br.marketing.client.ProFieldsClient;
import com.br.marketing.common.utils.Constants;
import com.br.marketing.common.utils.DateHelper;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.common.utils.file.ZipUtil;
import com.br.marketing.entity.LoanFile;
import com.br.marketing.entity.MarketingTask;
import com.br.marketing.entity.TaskStatus;
import com.br.marketing.mapper.LoanFileMapper;
import com.br.marketing.mapper.MarketingTaskMapper;
import com.br.marketing.mapper.TaskStatusMapper;
import com.br.marketing.push.PushApplication;
import com.br.marketing.push.service.MergeService;
import com.br.marketing.push.util.FileUtil;
import com.br.marketing.service.Impl.StrategyCs;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
 * @Date 2021/5/7 15:45
 * @Description:
 **/
@Service
@Slf4j
public class MergeMarketingImpl implements MergeService {
    @Resource
    LoanFileMapper loanFileMapper;
    @Resource
    MarketingTaskMapper marketingTaskMapper;
    @Resource
    TaskStatusMapper taskStatusMapper;
    @Resource
    StrategyCs strategyCS;
    @Resource
    ProFieldsClient proFieldsClient;
    private static final Pattern MYREGEX1 = Pattern.compile("_");
    private Map<String,String> proFieldMap=new HashMap<>();
    private static Map<String,Integer> sort=new HashMap<>();
    static {
        sort.put("scoremcashon360xkone",1);
        sort.put("scoremcashon360xktwo",2);
    }
    @Override
    public List<LoanFile> process(String apiCode) {
        List<LoanFile> pushList=new ArrayList<>();
        try{
            List<LoanFile> onceList=new ArrayList<>();
            if(StringUtils.isNotEmpty(apiCode)){
                initBatchNumList(onceList,apiCode);
                pushList.addAll(mergeOnce(onceList));
            }
        }catch (Exception e){
            log.error("error-----",e);
        }
        return pushList;
    }

    private List<LoanFile> mergeOnce(List<LoanFile> onceList) {
        List<LoanFile> onceFiles=new ArrayList<>();
        for(LoanFile blf:onceList){
            String zipName = mergeResultFile(blf);
            if(StringUtils.isEmpty(zipName)){
                continue;
            }
            blf.setZipFileName(zipName);
            onceFiles.add(blf);
        }
        return onceFiles;
    }


    /**
     * 合并周期为1的增量、全量、一次性的结果文件
     *
     * @param blf
     * @return
     */
    private String mergeResultFile(LoanFile blf){
        String zipFile="";
        try{
            List<String> result=new ArrayList<>();
            StringBuilder targetPath=new StringBuilder();
            targetPath.append(blf.getFilePath())
                    .append("/");
            MarketingTask blt = marketingTaskMapper.queryBlt(blf.getBatchNumber());
            String s ="";
            String fileName1 = blt.getFileName();
            fileName1=fileName1.replace(".txt","");
            s = MYREGEX1.split(fileName1)[1];
            String strategyStr = strategyCS.strategyIdCheck(blt.getApiCode(), blt.getStrategyId());
            if(StringUtils.isEmpty(strategyStr)){
                return zipFile;
            }
            proFieldsClient.setLoanPro(blt.getStrategyId(),blt.getApiCode(),strategyStr,new JSONObject(),proFieldMap,"");

            TaskStatus bts= taskStatusMapper.queryNewestBts(blf.getBatchNumber());
            String startTime = bts.getCreateTime();
            if(!StringUtils.isEmpty(startTime)){
                startTime=startTime.split(" ")[0];
                startTime=startTime.replace("-","");
            }else{
                startTime= DateHelper.getDateAddYyMmDd(0);
            }
            String strategyId=blt.getStrategyId();
            Integer sep= marketingTaskMapper.querySep(blt.getApiCode());
            String separator= Constants.sepMap.get(sep);
            List<String> pathNames=new ArrayList<>();
            int expectedNum=0;
            for(String product:proFieldMap.keySet()){
                String fileName=targetPath.toString()+blf.getApiCode()+"_"+s+"_"+blf.getBatchNumber()+"_"
                        +strategyId.split(":")[0]+"_"+startTime+"_"+DateHelper.getDateAddYyMmDd(0)+"_"+sort.get(product)+".txt";

                StringBuilder head= new StringBuilder();
                initHead(head,separator,product);
                expectedNum= FileUtil.mergeMarketing(head.toString(),fileName,targetPath.toString()+"/"+product+"/",
                        separator,blt.getActualNumber())+expectedNum;
                pathNames.add(fileName);
            }
            blf.setExpectedNum(expectedNum);
            zipFile=targetPath.toString()+blf.getApiCode()+"_"+s+"_"+blf.getBatchNumber()+"_"
                    +strategyId.split(":")[0]+"_"+startTime+"_"+DateHelper.getDateAddYyMmDd(0)+".zip";
            ZipUtil.compress(zipFile,pathNames);
            result.add(zipFile);
        }catch (Exception e){
            log.error("合并文件出错",e);
        }finally {
            proFieldMap.clear();
        }


        return zipFile;
    }
    /**
     * 初始化表头
     * @param head
     *
     * .append("姓名").append(",").append("身份证号").append(",").append("证书号").append(",").append("手机号")
    .append(",")
     */
    private void  initHead(StringBuilder head,String sep,String product){
        head.append("request_time").append(sep).append("batch_number").append(sep).append("cus_num")
                .append(sep).append("strategy_id").append(sep).append("version").append(sep);
        log.info("需要返回的数据产品--{}",product);
        if("scoremcashon360xkone".equals(product)){
            String scoremcashon360xkoneFields = proFieldMap.get("scoremcashon360xkone");
            String[] split = scoremcashon360xkoneFields.split(",");
            for (int i=0;i<split.length;i++){
                head.append(split[i]).append(sep);
            }
        }
        if("scoremcashon360xktwo".equals(product)){
            String scoremcashon360xktwoFields = proFieldMap.get("scoremcashon360xktwo");
            String[] split = scoremcashon360xktwoFields.split(",");
            for (int i=0;i<split.length;i++){
                head.append(split[i]).append(sep);
            }
        }
    }


    /**
     * 初始化当日需要监控的任务信息，并将增量监控和全量监控区分开来
     * @param onceList
     */
    private void initBatchNumList( List<LoanFile> onceList, String apiCode){

        List<LoanFile> list= loanFileMapper.queryFile(apiCode);
        for(LoanFile blf :list){
            if(blf.getType()==1){
                onceList.add(blf);
            }
        }
        log.info("[PUSH] once size:{}",onceList.size());
    }
}
