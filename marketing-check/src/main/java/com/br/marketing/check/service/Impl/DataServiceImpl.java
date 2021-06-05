package com.br.marketing.check.service.Impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.check.service.DataService;
import com.br.marketing.check.thread.FilterDataThread;
import com.br.marketing.client.IceClient;
import com.br.marketing.client.ProFieldsClient;
import com.br.marketing.client.StrategyClient;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.entity.LoanFile;
import com.br.marketing.entity.MarketingTask;
import com.br.marketing.entity.MerchantParam;
import com.br.marketing.mapper.LoanFileMapper;
import com.br.marketing.mapper.MarketingTaskMapper;
import com.br.marketing.mapper.MarketingUserMapper;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

@Service
@Slf4j
public class DataServiceImpl implements DataService {
    @Resource
    LoanFileMapper loanFileMapper;
    @Resource
    MarketingTaskMapper marketingTaskMapper;
    @Resource
    ProFieldsClient proFieldsClient;
    @Resource
    MarketingUserMapper marketingUserMapper;
    private final static Integer SPLITNUM=5000;





    @Override
    public boolean dataEliminate() {
        ExecutorService filterExecutor = BrExecutors.getThreadPool(20,20);
        try{
                List<MarketingTask> marketingTasks = marketingTaskMapper.queryBatchNum();
                log.info("需要校验的批次总数--{}", marketingTasks.size());
                for(MarketingTask blt: marketingTasks){
                    //剔除逻辑数组
                    JSONArray tcArray=new JSONArray();

                    String strategyId = blt.getStrategyId();
                    if(strategyId.startsWith("STRB")){
                        String apiCode = blt.getApiCode();
                        String strategy = StrategyClient.getStrategy(apiCode, strategyId);
                        MerchantParam merchantParam = IceClient.getMerchantParam(apiCode);
                        String meal = merchantParam.getMeal();
                        if(StringUtils.isNotEmpty(meal)){
                            DocumentContext parseMeal = JsonPath.parse(meal);
                            //校验策略可用性
                            DocumentContext parse = JsonPath.parse(strategy);
                            String read = parse.read("$.status").toString();
                            String read1 = parse.read("$.canUse").toString();
                            String s = parse.read("$.ruleType").toString();
                            DocumentContext parse1 = JsonPath.parse(s);
                            String s1 = parse1.read("$.status").toString();
                            if("1".equals(read)&&"0".equals(read1)&&"1".equals(s1)){
                                JSONArray ruletypeArray= JSONObject.parseObject(s).getJSONArray("ruleTypeList");
                                for(int i=0;i<ruletypeArray.size();i++){
                                    JSONObject jsonObject = ruletypeArray.getJSONObject(i);
                                    //获取规则集名称
                                    String ruleType = jsonObject.getString("ruleType");

                                    //获取规则集版本，因为策略后台用的是用户中心的规则集版本号，所以这里的版本号与策略后台保持一致
                                    String version= parseMeal.read("$."+ruleType+".version").toString();

                                    /**
                                     * 获取策略后台配置的当前规则集的剔除逻辑
                                     */
                                    String json = proFieldsClient.getdataEliminateJson(apiCode, ruleType, version);
                                    if(StringUtils.isNotEmpty(json)){
                                        JSONObject jsonObject1=JSONObject.parseObject(json);
                                        tcArray.add(jsonObject1);
                                    }
                                }

                                if(!tcArray.isEmpty()&&tcArray.size()>0){
                                    log.info("【{}】批次策略配置了【{}】条剔除规则",blt.getBatchNumber(),tcArray.size());
                                    /**
                                     * 获取当前批次结果文件的信息，进行数据剔除
                                     */
                                    LoanFile loanFile = loanFileMapper.queryBlf(blt.getBatchNumber());
                                    if(loanFile !=null&&StringUtils.isNotEmpty(loanFile.getFilePath())
                                            &&StringUtils.isNotEmpty(loanFile.getZipFileName())){
                                        filterData(loanFile.getFilePath(), loanFile.getZipFileName(),tcArray,apiCode,
                                                blt.getBatchNumber(), filterExecutor);
                                    }else{
                                        log.error("【{}】批次没有找到生成的结果文件,请关注",blt.getBatchNumber());
                                    }
                                }else{
                                    log.info("【{}】批次策略没有配置剔除规则",blt.getBatchNumber());
                                }
                            }
                        }
                    }
                }
            }catch (Exception e){
                log.error("出错了--{}",e);
                return false;
            }

        /**
         * 等待所有任务都执行完成
         **/
        filterExecutor.shutdown();
        while (true){
            if(filterExecutor.isTerminated()){
                log.info("所有线程都执行结束");
                break;
            }
            try {
                Thread.sleep(3000);
            }catch (Exception e){
                if(log.isErrorEnabled()){
                    log.error(e.getMessage(),e);
                }
                e.printStackTrace();
            }
        }
        return true;
    }



    private void filterData(String filePath, String zipFileName,JSONArray tcArray,String apiCode,String batchNumber,ExecutorService filterExecutor) {
        //log.info("filterData--{},{},{},{},{]",file_path,zipFile_name,tc_array,api_code,batch_number);
        if(StringUtils.isEmpty(zipFileName)){
            log.error("zipFile_name为空--{}",zipFileName);
            return;
        }
        String txtFileName=zipFileName.substring(0,zipFileName.lastIndexOf('.'))+".txt";
        File file=new File(filePath+"/"+txtFileName);
        if(!file.exists()){
            log.error("结果文件不存在--{}",filePath+"/"+txtFileName);
            return;
        }
        try(FileReader read = new FileReader(filePath+"/"+txtFileName);
            BufferedReader br = new BufferedReader(read)){
            int rownum = 0;
            int fileNo = 1;
            String row="";
            String head="";
            List<String> dataList=new ArrayList<>();
            /**
             * 读取结果文件，将文件数据切分为5000条一个的任务进行异步处理
             */
            while ((row = br.readLine()) != null) {
                String trim = row.trim();
                if(StringUtils.isNotEmpty(row)&&StringUtils.isNotEmpty(trim)){
                    if(!row.startsWith("request_time")){
                        rownum++;
                        dataList.add(row);
                        if((rownum / SPLITNUM) > (fileNo - 1)){
                            log.info("dataList size--{},fileNo--{}",dataList.size(),fileNo);
                            filterExecutor.submit(new FilterDataThread(dataList,tcArray,head,fileNo,apiCode,batchNumber, marketingUserMapper));
                            fileNo ++ ;
                            dataList=new ArrayList<>();
                        }
                    }else{
                        head=row;
                    }
                }
            }

            log.info("List size--{},fileNo--{}",dataList.size(),fileNo);
            if(dataList.size()>0){
                filterExecutor.submit(new FilterDataThread(dataList,tcArray,head,fileNo,apiCode,batchNumber, marketingUserMapper));
            }
            br.close();
            read.close();
        }catch (Exception e){
            log.error("剔除数据出错--{}",e);
        }
    }
}
