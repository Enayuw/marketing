package com.br.marketing.check.thread;

import cn.hutool.crypto.SecureUtil;
import com.br.common.encryption.Sha256Util;
import com.br.common.encryption.Sm3Util;
import com.br.common.util.BrCipherMaker;
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
    private Writer fw;
    private Writer errorfw;
    private DecodeClient decodeClient;
    private String head;
    private boolean isShell;
    private MarketingUserMapper marketingUserMapper;
    private MarketingDirtyUserMapper marketingDirtyUserMapper;
    private String batchNumber;
    private RedisChgService redisChgService;
    private String fileName;
    private MerchantParam merchantParam;
    public ValidatorSmallFileThread(Map<String,String> param, Writer fw, Writer errorfw, DecodeClient decodeClient,
                                    boolean isShell, MarketingUserMapper marketingUserMapper, MarketingDirtyUserMapper marketingDirtyUserMapper,
                                    RedisChgService redisChgService, MerchantParam merchantParam) {
        this.row=param.get("row");
        this.apiCode=param.get("apiCode");
        this.fw=fw;
        this.errorfw=errorfw;
        this.decodeClient=decodeClient;
        this.head=param.get("head");
        this.isShell=isShell;
        this.marketingUserMapper = marketingUserMapper;
        this.marketingDirtyUserMapper = marketingDirtyUserMapper;
        this.batchNumber=param.get("batchNumber");
        this.redisChgService=redisChgService;
        this.fileName=param.get("fileName");
        this.merchantParam=merchantParam;

    }

    @Override
    public String call() throws Exception {
        try{
            StringBuilder sb=new StringBuilder();
            BrCipherMaker instance = BrCipherMaker.getInstance();
            UserValidator userValidator = new UserValidator(merchantParam.getIsCheck());
            boolean b = CheckDataUtil.checkData(head,row, apiCode, errorfw, sb,decodeClient);

            if(b){
                /**
                 * 如果是shell脚本处理，则写入本地磁盘
                 * 否则直接写入数据库
                 */
                if(isShell){
                    fw.append(sb+"\n");
                }else{
                    /**
                     * 1900037062,5p2_5bΒ82R5p2D,ClECΒ9DAQFAQlcCwBUCA1VUwBR,UgsΒ0MDFUADlFcUFM,,,,,,,,,,,
                     * cus_num
                     * name
                     * id_card
                     * cell
                     * pass_date
                     * user_date
                     * loanMaturity_date
                     * approval_result
                     * linkman_cell
                     * time_range
                     * home_addr
                     * tel_home
                     * mail
                     * decodeFailType
                     */
                    //log.info("result:{}",sb);
                    String[] split = sb.toString().split(",",14);

                    if(Constants.APICODE_SHAZI.contains(apiCode)){
                        String cell=split[3];
                        if(StringUtils.isNotEmpty(cell)){
                            String decodeCell =instance.decode(cell);
                            String originCell;
                            if(userValidator.validatePhone(decodeCell)){
                                originCell = encode(merchantParam.getRequestCode(), decodeCell);
                            }else {
                                originCell=decodeCell;
                            }
                            MarketingUser user=new MarketingUser();
                            user.setApiCode(apiCode);
                            ArrayList<String> cellArray = new ArrayList<>();
                            cellArray.add(originCell);
                            cellArray.add(originCell.toLowerCase());
                            cellArray.add(originCell.toUpperCase());
                            user.setCellArray(cellArray);
                            List<MarketingUser> loanDirtyUserList= marketingDirtyUserMapper.queryDirtyUser(user);
                            if (loanDirtyUserList.size()>0){
                                log.warn("数据符合剔除条件，apicode--{}，cell--{}",apiCode,cell);
                                return null;
                            }
                        }
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
            }
        }catch (Exception e){
            log.error("数据校验失败--",e);
        }
        return null;
    }
    /**
     * 按照客户的加密配置进行加密
     * @param requestCode 加密方式
     * @param param 明文
     * @return 加密后的值
     */
    private String encode(String requestCode,String param){
        String result="";
        if("1001".equals(requestCode)){
            result= SecureUtil.md5(param);
        }
        if("1002".equals(requestCode)){
            result= Sha256Util.getSHA256Encrypt(param);
        }
        if("1003".equals(requestCode)){
            try {
                result = Sm3Util.getSM3Value(param);
            } catch (IOException e) {
                log.error("getSm3 error",e);
                result=param;
            }
        }
        return result;
    }
}
