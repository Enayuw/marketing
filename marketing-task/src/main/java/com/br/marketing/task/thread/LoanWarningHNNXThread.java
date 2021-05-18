package com.br.marketing.task.thread;

import com.alibaba.fastjson.JSONObject;
import com.br.common.encryption.BrCipherMaker;
import com.br.marketing.client.LoanWarningClient;
import com.br.marketing.client.ProFieldsClient;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.common.utils.DateHelper;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.entity.MarketingUser;
import com.br.marketing.entity.RequestLog;
import com.br.marketing.task.utils.*;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.util.*;
import java.util.concurrent.Callable;

/**
 * Created by Bairong on 2020/5/8.
 */
@Slf4j
public class LoanWarningHNNXThread implements Callable<String> {

    private List<MarketingUser> list;
    private String apiCode;
    private String strategyId;
    private int currentPage;
    private String path;
    private RedisChgService redisChgService;
    private boolean isIncr;
    private JSONObject meal=new JSONObject();
    private Map<String,String> proFieldMap=new HashMap<>();
    private String sep;
    private String batchNumber;
    private LoanWarningClient loanWarningClient;
    public LoanWarningHNNXThread(List<MarketingUser> list, Map<String,String> param,
                                 int currentPage, String path, RedisChgService redisChgService,
                                 ProFieldsClient proFieldsClient, boolean isIncr, LoanWarningClient loanWarningClient) {
        this.list = list;
        this.apiCode = param.get("apiCode");
        this.strategyId = param.get("strategyId") ;
        this.currentPage = currentPage;
        this.path = path;
        this.redisChgService = redisChgService;
        this.isIncr = isIncr;
        this.sep= param.get("sep") ;
        this.batchNumber=param.get("batchNumber");
        this.loanWarningClient=loanWarningClient;
        proFieldsClient.setLoanPro(strategyId,apiCode,param.get("strategyStr"),meal,proFieldMap,"");
    }

    @Override
    public String call() throws Exception {
        log.warn("开始执行监控任务。。{}。。{}",currentPage,list.size());
        File writeName = new File(path );
        if (!writeName.exists()) {
            writeName.mkdirs();
        }
        String dateAddYyMmDdHhMmSs = DateHelper.getDateAddYyMmDdHhMmSs(0);
        File file1 = new File(path + "/" + currentPage + ".txt");
        File errorFile = new File(path + "/error"+ currentPage +"_"+dateAddYyMmDdHhMmSs+ ".txt");
        try (
                Writer  fw = new BufferedWriter(
                        new OutputStreamWriter(
                                new FileOutputStream(file1), "UTF-8"));
                Writer  errorFw = new BufferedWriter(
                        new OutputStreamWriter(
                                new FileOutputStream(errorFile), "UTF-8"));
                ){

            this.query(fw,errorFw,list);
        }catch (Exception e){
            log.error("流失预警变动任务执行失败",e);
        }
        return null;
    }

    /**
     * 调用画像接口查询样本结果
     * @param fw 结果输出
     * @param list 样本列表
     * @throws IOException
     */
    private void query(Writer fw, Writer  errorFw, List<MarketingUser> list) throws IOException {
        BrCipherMaker instance = BrCipherMaker.getInstance();
        for (MarketingUser blu : list) {
            if(blu.getStatus()!=1){
                continue;
            }
            RequestLog requestLog=new RequestLog();
            requestLog.setRequestTime(new Date());
            JSONObject param = new JSONObject();
            param.put("strategyId", strategyId);
            JSONObject jsonData = new JSONObject();
            jsonData.put("cusNum", blu.getCusNum());
            jsonData.put("idCard", instance.decode(blu.getIdCard()));
            jsonData.put("name", instance.decode(blu.getName()));
            jsonData.put("cell", instance.decode(blu.getCell()));
            jsonData.put("passDate", blu.getPassDate());
            jsonData.put("user_date", blu.getUserDate());
            jsonData.put("loanMaturityDate", blu.getLoanMaturityDate());
            jsonData.put("batch_number", blu.getBatchNumber());
            if(StringUtils.isNotEmpty(blu.getDecodeFailType())){
                jsonData.put("decodeFailType", blu.getDecodeFailType());
            }
            String approvalResult = blu.getApprovalResult();
            String result="";
            if(com.br.common.util.StringUtils.isNotEmpty(approvalResult)){
                switch (approvalResult){
                    case "通过" :
                        result= "1";
                        break;
                    case "拒绝" :
                        result= "2";
                        break;
                    case "复议" :
                        result= "3";
                        break;
                    case "无结果" :
                        result= "4";
                        break;
                    case "无贷前审批" :
                        result= "5";
                        break;
                    default:
                        break;
                }
            }
            if(com.br.common.util.StringUtils.isNotEmpty(result)){
                jsonData.put("approveResult", result);
            }
            param.put("jsonData", jsonData.toString());
            String  s= loanWarningClient.queryApi(param, apiCode);
            dealResult(s, fw,blu.getCusNum(),blu.getBatchNumber(),strategyId,errorFw);
        }
    }


    /**
     * 用流失预警api的返回生成结果文件
     * @param s
     */
    private void dealResult(String s, Writer fw,String cusNum,
                            String batchNumber,  String strategyId,Writer errorFw) throws IOException {

        try {
            if(strategyId.startsWith("STRB")&&!com.br.common.util.StringUtils.isEmpty(s)){
                JSONObject resultJson=JSONObject.parseObject(s);
                if(com.br.common.util.StringUtils.isNotEmpty(resultJson.getString("code"))||"00".equals(resultJson.getString("code"))
                        ||"100002".equals(resultJson.getString("code"))){
                    ResultUtil.generateFile(resultJson,strategyId,fw,sep,cusNum,batchNumber,proFieldMap,apiCode);
                }else{
                    log.error("策略返回错误--{}",cusNum);
                    ResultUtil.generateErrorFile(resultJson,errorFw,batchNumber,sep,cusNum);
                }
            }
        }catch (Exception e){
            log.error("dealResult出错了",e);
        }
    }

}
