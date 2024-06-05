package com.br.marketing.bridge.common.thread;

import com.br.marketing.bridge.DataBridgeApplication;
import com.br.marketing.bridge.model.dto.FileContext;
import com.br.marketing.bridge.common.utils.CheckDataUtil;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.common.utils.Constants;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.entity.MarketingUser;
import com.br.marketing.mapper.MarketingDirtyUserMapper;
import com.br.marketing.mapper.MarketingUserMapper;
import com.br.marketing.rpcclient.rpcclientImpl.DecodeGrpcClient;
import lombok.extern.slf4j.Slf4j;

import java.io.Writer;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by Bairong on 2020/5/16.
 */
@Slf4j
public class ValidatorSmallFileThread implements Callable<String> {
    private String row;
    private String apiCode;
    private Writer errorfw;
    private DecodeGrpcClient decodeClient;
    private String head;
    private MarketingUserMapper marketingUserMapper;
    private MarketingDirtyUserMapper marketingDirtyUserMapper;
    private String batchNumber;
    private RedisChgService redisChgService;
    private String fileName;
    private AtomicLong desTime;
    private Integer checkOpen=1;
    private Boolean checkBlackList;
    public ValidatorSmallFileThread(FileContext context,Map<String,String> param,Writer errorfw,AtomicLong desTime,Integer checkOpen) {
        this.row=param.get("row");
        this.head=param.get("head");
        this.apiCode=context.getTask().getApiCode();
        this.errorfw=errorfw;
        this.checkBlackList="1".equals(param.get("checkBlackList"))?true:false;
        this.decodeClient= DataBridgeApplication.ac.getBean(DecodeGrpcClient.class);
        this.marketingUserMapper = DataBridgeApplication.ac.getBean(MarketingUserMapper.class);
        this.marketingDirtyUserMapper = DataBridgeApplication.ac.getBean(MarketingDirtyUserMapper.class);;
        this.batchNumber=context.getTask().getBatchNumber();
        this.redisChgService= DataBridgeApplication.ac.getBean(RedisChgService.class);
        this.fileName=context.getDistinctTxtFileName();
        this.desTime = desTime;
        this.checkOpen = checkOpen;
    }

    @Override
    public String call() throws Exception {
        try{
            StringBuilder sb=new StringBuilder();
            boolean b = CheckDataUtil.checkData(head, row, apiCode, errorfw, sb, decodeClient);
            if(b&&checkOpen.equals(1)){
                    String[] split = sb.toString().split(",",14);
                    if(checkBlackList&&isHitBlackList()){
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
        String cell="";
        try{
            cell=rows[cellIndex];
        }catch (ArrayIndexOutOfBoundsException e){
        }
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
