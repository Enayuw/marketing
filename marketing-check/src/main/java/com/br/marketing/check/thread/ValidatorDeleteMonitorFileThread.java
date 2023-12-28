//package com.br.marketing.check.thread;
//
//import cn.hutool.crypto.SecureUtil;
//import com.br.common.encryption.Sha256Util;
//import com.br.common.encryption.Sm3Util;
//import com.br.common.util.BrCipherMaker;
//import com.br.common.util.StringUtils;
//import com.br.marketing.check.CkeckApplication;
//import com.br.marketing.check.dto.FileContext;
//import com.br.marketing.check.utils.CheckDataUtil;
//import com.br.marketing.client.RedisChgService;
//import com.br.marketing.common.utils.Constants;
//import com.br.marketing.entity.MarketingUser;
//import com.br.marketing.entity.MerchantParam;
//import com.br.marketing.mapper.MarketingDirtyUserMapper;
//import com.br.marketing.rpcclient.rpcclientImpl.DecodeGrpcClient;
//import lombok.extern.slf4j.Slf4j;
//
//import java.io.IOException;
//import java.io.Writer;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Map;
//import java.util.Set;
//
//@Slf4j
//public class ValidatorDeleteMonitorFileThread implements Runnable {
//    private Set<String> list;
//    private String apiCode;
//    private MarketingDirtyUserMapper marketingDirtyUserMapper;
//    private MerchantParam merchantParam;
//    private Map<String,Integer> headIndexMap;
//    private  Writer fw;
//    private RedisChgService redisChgService;
//    private String fileName;
//    private DecodeGrpcClient decodeClient;
//
//    public ValidatorDeleteMonitorFileThread(Set<String> list, FileContext context, Map<String,Integer> headIndexMap, Writer fw, DecodeGrpcClient decodeClient) {
//        this.list = list;
//        this.apiCode = context.getApiCode();
//        this.marketingDirtyUserMapper = CkeckApplication.ac.getBean(MarketingDirtyUserMapper.class);
//        this.merchantParam = context.getMerchantParam();
//        this.headIndexMap = headIndexMap;
//        this.fw=fw;
//        this.redisChgService= CkeckApplication.ac.getBean(RedisChgService.class);
//        this.fileName=context.getDistinctTxtFileName();
//        this.decodeClient=decodeClient;
//    }
//
//
//    @Override
//    public void run() {
//        log.info("ValidatorDeleteMonitorFileThread list:{}headIndexMap:{}",list,headIndexMap);
//        List<MarketingUser> insertList=new ArrayList<>();
//        checkDeleteDataV2(insertList);
//        try {
//            if (insertList.size()>0){
//                marketingDirtyUserMapper.insertDirtyUser(insertList);
//            }
//        }catch (Exception e){
//            log.error("更新数据库出错",e);
//        }
//    }
//    /**
//     * 在数据库中匹配剔除数据
//     * @param insertList 匹配后的需要插入剔除表的数据
//     * @param updateList 匹配后需要更新用户表的数据
//     */
//    private void checkDeleteData(List<MarketingUser> insertList, List<MarketingUser> updateList) {
//
//        for(String row:list){
//
//            log.debug("checkDeleteData :row {}",row);
//            if(StringUtils.isEmpty(row)){
//                log.warn("空行");
//                continue;
//            }
//            String[] rowArray = row.split(",");
//            log.warn("checkDeleteData :row {}，rowArray：{}",row,rowArray);
//            String batchNumber="";
//            String id="";
//            String cell="";
//            String name="";
//            String cusNum ="";
//
//            if(headIndexMap.get("cusNumIndex")==-1){
//                log.info("客户编号不存在");
//                continue;
//            }
//            if(headIndexMap.get("cusNumIndex")!=-1){
//                try {
//                    cusNum = rowArray[headIndexMap.get("cusNumIndex")];
//                }catch (ArrayIndexOutOfBoundsException e){
//                    log.warn("客户编号不存在 cusNumIndex：{}",headIndexMap.get("cusNumIndex"));
//                    continue;
//                }
//            }
//            BrCipherMaker instance = BrCipherMaker.getInstance();
//
//            if(headIndexMap.get("batchNumberIndex")!=-1){
//                try {
//                    batchNumber = rowArray[headIndexMap.get("batchNumberIndex")];
//                }catch (ArrayIndexOutOfBoundsException e){
//                    log.warn("batchNumberIndex：{}",headIndexMap.get("batchNumberIndex"));
//                }
//            }
//            if(headIndexMap.get("idIndex")!=-1){
//                try {
//                    id = rowArray[headIndexMap.get("idIndex")];
//                }catch (ArrayIndexOutOfBoundsException e){
//                    log.warn("idIndex：{}",headIndexMap.get("idIndex"));
//                }
//            }
//            if(headIndexMap.get("cellIndex")!=-1){
//                try {
//                    cell = rowArray[headIndexMap.get("cellIndex")];
//                }catch (ArrayIndexOutOfBoundsException e){
//                    log.warn("cellIndex：{}",headIndexMap.get("cellIndex"));
//                }
//            }
//            if(headIndexMap.get("nameIndex")!=-1){
//                try{
//                    name = rowArray[headIndexMap.get("nameIndex")];
//                }catch (ArrayIndexOutOfBoundsException e){
//                    log.warn("nameIndex：{}",headIndexMap.get("nameIndex"));
//                }
//            }
//
//            log.info("id:{},cell:{},name:{},cus_num:{},batch_number:{}",id,cell,name,cusNum,batchNumber);
//            MarketingUser user=new MarketingUser();
//            user.setApiCode(apiCode);
//            user.setCusNum(cusNum);
//            if(StringUtils.isNotEmpty(batchNumber)){
//                user.setBatchNumber(batchNumber);
//            }
//            try {
//                List<MarketingUser> marketingUserListlist = marketingDirtyUserMapper.queryUser(user);
//                log.info("loanUserListlist size:{}", marketingUserListlist.size());
//                boolean isDelete=false;
//                for(MarketingUser marketingUser : marketingUserListlist){
//                    boolean flag=true;
//                    String logDecodeId = instance.decode(marketingUser.getIdCard());
//                    String logDecodeCell = instance.decode(marketingUser.getCell());
//                    String logDecodeName = instance.decode(marketingUser.getName());
//                    if(!logDecodeId.equals(id)){
//                        String encodeId = encode(merchantParam.getRequestCode(), logDecodeId);
//                        if(!encodeId.equalsIgnoreCase(id)){
//                            flag=false;
//                        }
//                    }
//                    if(!logDecodeCell.equals(cell)){
//                        String encodeCell = encode(merchantParam.getRequestCode(), logDecodeCell);
//                        if(!encodeCell.equalsIgnoreCase(cell)){
//                            flag=false;
//                        }
//                    }
//                    if(!logDecodeName.equals(name)){
//                        String encodeName = encode(merchantParam.getRequestCode(), logDecodeName);
//                        if(!encodeName.equalsIgnoreCase(name)){
//                            flag=false;
//                        }
//                    }
//                    if(flag){
//                        log.debug("updateUser :{}", marketingUser);
//                        updateList.add(marketingUser);
//                        isDelete=true;
//                    }
//                }
//                String s = fileName.toUpperCase();
//                MarketingUser dirtyUser=new MarketingUser(apiCode,batchNumber,cusNum,id,name,cell);
//                insertList.add(dirtyUser);
//                if(isDelete){
//
//                    String key = Constants.DELETE_MONITOR_SUCCESS + s;
//                    log.warn("匹配成功:{}",row);
//                    redisChgService.incr(key);
//                }else {
//                    log.warn("匹配失败:{}",row);
//                    fw.append("匹配失败,"+row+"\n");
//                    String key = Constants.DELETE_MONITOR_ERROR + s;
//                    redisChgService.incr(key);
//                }
//            }catch (Exception e){
//                log.error("checkDeleteData error ",e);
//            }
//        }
//    }
//
//    /**
//     * 校验黑名单数据
//     * @param insertList 匹配后的需要插入剔除表的数据
//     */
//    private void checkDeleteDataV2(List<MarketingUser> insertList) {
//        for(String row:list){
//            Boolean flag=false;
//            log.debug("checkDeleteData :row {}",row);
//            if(StringUtils.isEmpty(row)){
//                log.warn("空行");
//                continue;
//            }
//            String[] rowArray = row.split(",");
//            String cell="";
//            String cusNum="";
//            if(headIndexMap.get("cusNumIndex")==-1){
//                log.info("客户编号不存在");
//                continue;
//            }
//            if(headIndexMap.get("cusNumIndex")!=-1){
//                try {
//                    cusNum = rowArray[headIndexMap.get("cusNumIndex")];
//                }catch (ArrayIndexOutOfBoundsException e){
//                    log.warn("客户编号不存在 cusNumIndex：{}",headIndexMap.get("cusNumIndex"));
//                    continue;
//                }
//            }
//            if(headIndexMap.get("cellIndex")!=-1){
//                try {
//                    cell = rowArray[headIndexMap.get("cellIndex")];
//                }catch (ArrayIndexOutOfBoundsException e){
//                    log.warn("cellIndex：{}",headIndexMap.get("cellIndex"));
//                }
//            }
//            try {
//                if(StringUtils.isNotEmpty(cell)){
//                    Map<String, String> resultMap = CheckDataUtil.checkColumn(cell,"cell",merchantParam,decodeClient);
//                    String result = resultMap.get("result");
//                    if (StringUtils.isNotEmpty(result)) {
//                        flag=true;
//                    }
//                }
//                String s = fileName.toUpperCase();
//                if(flag){
//                    MarketingUser dirtyUser=new MarketingUser(apiCode,"",cusNum,"","",cell);
//                    insertList.add(dirtyUser);
//                    String key = Constants.DELETE_MONITOR_SUCCESS + s;
//                    redisChgService.incr(key);
//                }else {
//                    log.warn("校验失败:{}",row);
//                    StringBuilder errorSb = new StringBuilder();
//                    String s1 = Constants.headMap.get("cell");
//                    errorSb.append(s1 + "错误,").append(row + "\n");
//                    fw.append(errorSb);
//                    String key = Constants.DELETE_MONITOR_ERROR + s;
//                    redisChgService.incr(key);
//                }
//            }catch (Exception e){
//                log.error("checkDeleteData error ",e);
//            }
//        }
//    }
//    /**
//     * 按照客户的加密配置进行加密
//     * @param requestCode 加密方式
//     * @param param 明文
//     * @return 加密后的值
//     */
//    private String encode(String requestCode,String param){
//        String result="";
//        if("1001".equals(requestCode)){
//            result= SecureUtil.md5(param);
//        }
//        if("1002".equals(requestCode)){
//            result= Sha256Util.getSHA256Encrypt(param);
//        }
//        if("1003".equals(requestCode)){
//            try {
//                result = Sm3Util.getSM3Value(param);
//            } catch (IOException e) {
//                log.error("getSm3 error",e);
//                result=param;
//            }
//        }
//        return result;
//    }
//
//}
