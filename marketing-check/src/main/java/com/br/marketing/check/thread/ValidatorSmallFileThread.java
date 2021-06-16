package com.br.marketing.check.thread;

import cn.hutool.crypto.SecureUtil;
import com.br.common.encryption.Sha256Util;
import com.br.common.encryption.Sm3Util;
import com.br.common.util.BrCipherMaker;
import com.br.marketing.check.CkeckApplication;
import com.br.marketing.check.dto.FileContext;
import com.br.marketing.check.utils.CheckDataUtil;
import com.br.marketing.client.DecodeClient;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.common.utils.Constants;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.common.validators.user.UserValidator;
import com.br.marketing.entity.MarketingUser;
import com.br.marketing.entity.MerchantParam;
import com.br.marketing.mapper.MarketingDirtyUserMapper;
import com.br.marketing.mapper.MarketingUserMapper;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * Created by Bairong on 2020/5/16.
 */
@Slf4j
public class ValidatorSmallFileThread implements Callable<String> {
    private String row;
    private String apiCode;
    private Writer errorfw;
    private DecodeClient decodeClient;
    private String head;
    private MarketingUserMapper marketingUserMapper;
    private MarketingDirtyUserMapper marketingDirtyUserMapper;
    private String batchNumber;
    private RedisChgService redisChgService;
    private String fileName;
    public ValidatorSmallFileThread(FileContext context,Map<String,String> param,Writer errorfw) {
        this.row=param.get("row");
        this.head=param.get("head");
        this.apiCode=context.getTask().getApiCode();
        this.errorfw=errorfw;
        this.decodeClient=CkeckApplication.ac.getBean(DecodeClient.class);
        this.marketingUserMapper = CkeckApplication.ac.getBean(MarketingUserMapper.class);
        this.marketingDirtyUserMapper = CkeckApplication.ac.getBean(MarketingDirtyUserMapper.class);;
        this.batchNumber=context.getTask().getBatchNumber();
        this.redisChgService=CkeckApplication.ac.getBean(RedisChgService.class);
        this.fileName=context.getDistinctTxtFileName();

    }

    @Override
    public String call() throws Exception {
        try{
            StringBuilder sb=new StringBuilder();
            boolean b = CheckDataUtil.checkData(head,row, apiCode, errorfw, sb,decodeClient);
            if(b){

                    String[] split = sb.toString().split(",",14);

                    if(isHitBlackList()){
                        return null;
                    }
                    MarketingUser lu=new MarketingUser();
                    lu.setApiCode(apiCode);
                    lu.setBatchNumber(batchNumber);
                    lu.setCusNum(split[0]);
                    lu.setName(split[1]);
                    lu.setIdCard(split[2]);
                    lu.setCell(split[3]);
                    lu.setPassDate(split[4]);
                    lu.setLoanMaturityDate(split[6]);
                    lu.setApprovalResult(split[7]);
                    lu.setLinkmanCell(split[8]);
                    if(!StringUtils.isEmpty(split[9])){
                        lu.setTimeRange(Integer.parseInt(split[9]));
                    }
                    lu.setHomeAddr(split[10]);
                    lu.setTelHome(split[11]);
                    lu.setMail(split[12]);
                    String s = split[13];
                    if(StringUtils.isNotEmpty(s)){
                        s=s.replace(",","");
                        lu.setDecodeFailType(s);
                    }
                    marketingUserMapper.insertUser(lu);
                    redisChgService.incr(Constants.INSERT_DB_NUMBER+fileName);
                }
        }catch (Exception e){
            log.error("数据校验失败--",e);
        }
        return null;
    }

    private Boolean isHitBlackList(){
        String[] columns = head.split(",");
        String[] rows = row.split(",");
        int cellIndex = CheckDataUtil.findIndex(columns, "cell");
        String cell=rows[cellIndex];
        if(StringUtils.isNotEmpty(cell)){
            MarketingUser user=new MarketingUser();
            user.setApiCode(apiCode);
            user.setCell(cell);
            List<MarketingUser> loanDirtyUserList= marketingDirtyUserMapper.queryDirtyUser(user);
            if (loanDirtyUserList.size()>0){
                log.warn("数据符合剔除条件，apicode--{}，cell--{}",apiCode,cell);
                return true;
            }
        }
        return false;
    }
}
