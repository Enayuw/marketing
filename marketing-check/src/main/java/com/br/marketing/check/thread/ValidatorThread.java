package com.br.marketing.check.thread;

import com.br.marketing.rpcclient.rpcclientImpl.DecodeGrpcClient;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Created by Bairong on 2020/3/17.
 */
@Slf4j
public class ValidatorThread implements Callable<String>{
    /**
     * 数据
     */
    private List<String> dataList;
    /**
     * apiCode
     */
    private String apiCode;
    /**
     * 路径
     */
    private String path;
    /**
     * currentNum
     */
    private Integer currentNum;
    /**
     * 解密客户端
     */
    private DecodeGrpcClient decodeClient;
    /**
     * 表头
     */
    private String head;
    /**
     * 文件名
     */
    private String filename;

    /**
     * 构造方法
     * @param dataList 数据
     * @param apiCode apiCode
     * @param path 路径
     * @param currentNum
     * @param decodeClient 解密客户端
     * @param head 文件头
     * @param filename 文件名
     */
    public ValidatorThread(List<String> dataList, String apiCode, String path, Integer currentNum,
                           DecodeGrpcClient decodeClient, String head, String filename){
        this.dataList=dataList;
        this.apiCode=apiCode;
        this.path=path;
        this.currentNum=currentNum;
        this.decodeClient=decodeClient;
        this.head=head;
        this.filename=filename;
    }
    @Override
    public String call() throws Exception {
        log.info("start check data __{}",dataList.size());
        String resultPath=path+"/result/";
        String errorPath=path+"/error/";
        File resultPathFile=new File(resultPath);
        if(!resultPathFile.exists()){
            resultPathFile.mkdirs();
        }
        File errorPathFile=new File(errorPath);
        if(!errorPathFile.exists()){
            errorPathFile.mkdirs();
        }

        File decodeFile = new File(resultPath+currentNum+".txt");
        File file1 = new File(errorPath+"error_"+currentNum+".txt");

        try(Writer fw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(decodeFile), "UTF-8"));
            Writer errorfw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file1), "UTF-8"));){
            for(String row:dataList){
                   StringBuilder sb=new StringBuilder();
                   boolean flag=false;
                   if(filename.indexOf("DeleteMonitor")>=0){
                        flag=true;
                   }
                /*    boolean b = CheckDataUtil.checkData(head,row, apiCode, errorfw, sb,flag,decodeClient);
                    if(b){
                        fw.append(sb+"\n");
                }*/
            }
        }catch (Exception e){
            log.error("数据校验出错--{}",e);
        }
        return null;
    }


}
