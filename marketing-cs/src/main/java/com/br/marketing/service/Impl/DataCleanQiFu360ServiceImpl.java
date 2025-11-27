package com.br.marketing.service.Impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.client.marketingapi.MarketingApiService;
import com.br.marketing.client.marketingapi.input.PushTransferDataDetailDTO;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.common.utils.DateHelper;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.dto.TransferDataDTO;
import com.br.marketing.dto.TransferDataItemDTO;
import com.br.marketing.entity.QueryUserRealMessage;
import com.br.marketing.entity.QueryUserRealMessageExample;
import com.br.marketing.mapper.QueryUserRealMessageMapper;
import com.br.marketing.service.DataCleanQiFu360Service;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.util.ThreadPoolAdjustmentUtil;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@Slf4j
public class DataCleanQiFu360ServiceImpl implements DataCleanQiFu360Service {
    @Resource
    private MarketingCommonConfig marketingCommonConfig;
    @Resource
    private DynamicParameterServiceImpl dynamicParameterServiceImpl;
    @Resource
    MarketingApiService marketingApiService;
    @Resource
    private QueryUserRealMessageMapper queryUserRealMessageMapper;

    @Override
    public void cleaning(String jobParameter) {
        String beginQueryDate = null;
        String endQueryDate = null;
        String createDate = null;
        JSONArray apiCodeFromJobParam = null;
        if(StringUtils.isNotBlank(jobParameter)){
            JSONObject paramJson = JSON.parseObject(jobParameter);
            beginQueryDate = paramJson.getString("beginQueryDate");
            endQueryDate = paramJson.getString("endQueryDate");
            createDate = paramJson.getString("createDate");
            apiCodeFromJobParam = paramJson.getJSONArray("apiCode");
        }
        ThreadPoolExecutor pushPool = BrExecutors.getThreadPool(5, 5);
        while(true){
            Integer pageSize = dynamicParameterServiceImpl.getPageSize("DataCleanQiFu360");
            Integer coreNum = marketingCommonConfig.getDataCleanQiFu360CoreAndMaxNum();
            if(null != coreNum){
                ThreadPoolAdjustmentUtil.adjustThreadPoolSize(pushPool, coreNum);
            }
            // 从数据库读取满足条件的数据
            QueryUserRealMessageExample example = new QueryUserRealMessageExample();
            QueryUserRealMessageExample.Criteria criteria = example.createCriteria();
            criteria.andStatusEqualTo(0).andIsDeletedEqualTo(0);
            if(StringUtils.isNotBlank(beginQueryDate) && StringUtils.isNotBlank(endQueryDate) ){
                // 使用beginQueryDate和endQueryDate必须配置上createDate
                SimpleDateFormat sdf = new SimpleDateFormat(DateHelper.LINE_DATE_COLON_TIME_FORMAT);
                try {
                    Date begin = sdf.parse(beginQueryDate);
                    Date end = sdf.parse(endQueryDate);
                    criteria.andCreateTimeGreaterThanOrEqualTo(begin);
                    criteria.andCreateTimeLessThanOrEqualTo(end);
                } catch (ParseException e) {
                    log.warn("DataCleanQiFu360Job-参数中时间格式格式化异常[{}]",jobParameter);
                }
            }
            if(StringUtils.isBlank(createDate)){
                String nowFormat = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                criteria.andCreateDateEqualTo(nowFormat);
            }else{
                criteria.andCreateDateGreaterThanOrEqualTo(createDate);
            }
            if(null != apiCodeFromJobParam && !apiCodeFromJobParam.isEmpty()){
                criteria.andApiCodeIn(apiCodeFromJobParam.toJavaList(String.class));
            }
            example.setOrderByClause(String.format(" id limit %d", pageSize));
            List<QueryUserRealMessage> list = queryUserRealMessageMapper.selectByExample(example);
            if(null == list || list.size()<1){
                break;
            }
            List<Long> allIdListlist = list.stream().map(QueryUserRealMessage::getId).collect(Collectors.toList());
            queryUserRealMessageMapper.updateStatusByIdList(1, allIdListlist);

            // 最后一个对象
            QueryUserRealMessage queryUserRealMessage = list.get(list.size() - 1);
            // 目前只同时支持一个apiCode操作，后续需要优化
            String apiCode = queryUserRealMessage.getApiCode();
            // list数据按照1000条切割
            List<List<QueryUserRealMessage>> list1000 = Lists.partition(list, 1000);
            list1000.forEach((List<QueryUserRealMessage> listQurm)->{
                pushPool.submit(() -> {
                    List<Long> idList = new ArrayList();
                    try{
                        // 数据清洗
                        List<TransferDataItemDTO> transferDataItemDTOS = dataTransfer(listQurm,idList);
                        // 调用转化接口参数拼接并调用
                        asyncTransferUpload(apiCode, transferDataItemDTOS, idList);
                    }catch (Exception e){
                        log.error("apiCode:{}奇富360数据清洗-线程池中执行异常-", apiCode, e);
                        // 根据响应结果更新数据库数据表-status5数据处理失败
//                        queryUserRealMessageMapper.updateStatusByIdList(5, idList);
                    }
                });
            });
        }
        pushPool.shutdown();
        try {
            while (!pushPool.awaitTermination(10L, TimeUnit.SECONDS)) {
                // do nothing
            }
        } catch (InterruptedException e) {
            log.error("奇富360-DataCleanQiFu360Job调用转化数据上传接口-线程池中断异常-", e);
            pushPool.shutdownNow();
            Thread.currentThread().interrupt();
        } catch (Exception e){
            log.error("奇富360-DataCleanQiFu360Job调用转化数据上传接口-线程池停止异常-", e);
            pushPool.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    /**
     * 数据对象转换方法
     * @param list list
     * @param idList idList
     * @return List<TransferDataItemDTO>
     */
    private List<TransferDataItemDTO> dataTransfer(List<QueryUserRealMessage> list, List<Long> idList){
        List<TransferDataItemDTO> dataItems = new ArrayList<>();
        list.stream().forEach((QueryUserRealMessage qrm)->{
            String lastLoginTime = null;
//            String dateApplSubmit = null;
            String sxSuccess = null;
            String creditAmt = null;
            String creditTime = null;
            String isSucc = null;
            // 下面放到扩展字段里面
            String name = null;
//            String stopMarketingSign = null;
            String sex = null;
            String age = null;
            String isLightMarkting = null;
            String operationScene = null;
            String isLoan = null;
            String succAmtType = null;
            String userMessage = qrm.getUserMessage();
            if(StringUtils.isNotBlank(userMessage)){
                JSONObject object = JSON.parseObject(userMessage);
                if(null != object && !object.isEmpty()){
                    lastLoginTime = object.getString("lastLoginTime");
                    name = object.getString("name");
                    String sexOriginal = object.getString("sex");
                    String[] sexValue = {"1","0"};
                    String[] sexKey = {"M","F"};
                    sexOriginal = emptyDefault(sexOriginal);
                    sex = getMapByList(sexOriginal,sexKey,sexValue);
                    age = object.getString("age");
//                    mobileMd5 = object.getString("mobileMd5");
                    String userExtraInfoJsonString = object.getString("userExtraInfo");
                    if(StringUtils.isNotBlank(userExtraInfoJsonString)){
                        JSONObject userExtraInfoJson = JSON.parseObject(userExtraInfoJsonString);
                        if(null != userExtraInfoJson && !userExtraInfoJson.isEmpty()){
                            isLightMarkting = userExtraInfoJson.getString("isLightMarkting");
                            operationScene = userExtraInfoJson.getString("operationScene");
                        }
                    }
                }
            }
            String riskMessage = qrm.getRiskMessage();
            if(StringUtils.isNotBlank(riskMessage)){
                JSONObject object = JSON.parseObject(riskMessage);
                if(null != object && !object.isEmpty()){
                    creditAmt = object.getString("creditAmt");
                    creditTime = object.getString("creditTime");
                }
            }
            String tradeMessage = qrm.getTradeMessage();
            String hisSettleTime = null;
            String curAvailableQuotays_derived = null;
            Integer curAvailableQuota = null;
            if(StringUtils.isNotBlank(tradeMessage)){
                JSONObject object = JSON.parseObject(tradeMessage);
                if(null != object && !object.isEmpty()){
                    String isSuccOriginal = object.getString("isSucc");
                    hisSettleTime = object.getString("hisSettleTime");
                    curAvailableQuota = object.getInteger("curAvailableQuota");
                    if (!Objects.isNull(curAvailableQuota)) {
                        curAvailableQuotays_derived = String.valueOf((curAvailableQuota * 3000 - 3000));
                    }
                    String[] isSuccValue = {"1","0"};
                    String[] isSuccKey = {"Y","N"};
                    isSuccOriginal = emptyDefault(isSuccOriginal);
                    isSucc = getMapByList(isSuccOriginal,isSuccKey,isSuccValue);

                    String isLoanOriginal = object.getString("isLoan");
                    isLoanOriginal = emptyDefault(isLoanOriginal);
                    isLoan = getMapByList(isLoanOriginal,isSuccKey,isSuccValue);
                    succAmtType = object.getString("succAmtType");
                }
            }

            TransferDataItemDTO transferDataItemDTO = new TransferDataItemDTO();
            transferDataItemDTO.setApiCode(qrm.getApiCode());
            //必填
            transferDataItemDTO.setCustNum(qrm.getUniqueReqNo());
//            transferDataItemDTO.setSource(qrm.get);
            //必填（跟产品商量后使用固定值1）
            transferDataItemDTO.setUserType("1");
//            transferDataItemDTO.setType(qrm.get);
//            transferDataItemDTO.setCustomName(qrm.get);
//            transferDataItemDTO.setIfRegister(qrm.get);
//            transferDataItemDTO.setRegisterTime(qrm.get);
//            transferDataItemDTO.setIfLogin(qrm.get);
            transferDataItemDTO.setLoginTime(lastLoginTime);
//            transferDataItemDTO.setIfApply(qrm.get);
//            transferDataItemDTO.setApplyDt(dateApplSubmit);
            transferDataItemDTO.setApplyResult(sxSuccess);
//            transferDataItemDTO.setApplyTime(qrm.get);
//            transferDataItemDTO.setRefuseTime(qrm.get);
//            transferDataItemDTO.setAuditTime(qrm.get);
            transferDataItemDTO.setAuditAmount(creditAmt);
            transferDataItemDTO.setIfLent(isSucc);

            // 下面进行扩展子段参数拼接
            JSONObject reserveField1JSON = new JSONObject();
            reserveField1JSON.put("firstName", name);
            reserveField1JSON.put("cell", qrm.getMobileMd5());
            reserveField1JSON.put("stopMarketingSign", qrm.getStopMarketingSign());
            reserveField1JSON.put("gender",sex);
            reserveField1JSON.put("age",age);
            reserveField1JSON.put("isLightMarkting", isLightMarkting);
            reserveField1JSON.put("operationScene", operationScene);
            reserveField1JSON.put("applyLoan", isLoan);
            reserveField1JSON.put("succAmtType",succAmtType);
            reserveField1JSON.put("hisSettleTime",hisSettleTime);
            reserveField1JSON.put("curAvailableQuota",curAvailableQuota);
            reserveField1JSON.put("curAvailableQuotays_derived",curAvailableQuotays_derived);
            reserveField1JSON.put("creditTime",creditTime);
            transferDataItemDTO.setReserveField1(reserveField1JSON.toJSONString());
            dataItems.add(transferDataItemDTO);
            idList.add(qrm.getId());
        });
        return dataItems;
    }

    /**
     * 2023-05-10 11:20
     * 值为null时，赋值''
     */
    private String emptyDefault(String value) {
        return StringUtils.isNotBlank(value) ? value : "";
    }

    private String getMapByList(String value, String[] keyArray, String[] valueArray){
        if(keyArray.length != valueArray.length){
            log.warn("参数长度不一致value:{}-keyArray[{}]valueArray[{}]",value,keyArray,valueArray);
            return value;
        }
        try{
            for (int i = 0; i < keyArray.length; i++) {
                if(value.trim().equals(keyArray[i])){
                    return valueArray[i];
                }
            }
        }catch (Exception e){
            log.warn("参数映射异常value:{}-keyArray[{}]valueArray[{}]--",value,keyArray,valueArray,e);
        }
        return value;
    }

    /**
     * 异步上传转化接口
     * @param apiCode apiCode
     * @param dataItems dataItems
     * @param idList idList
     */
    private void asyncTransferUpload(String apiCode, List<TransferDataItemDTO> dataItems, List<Long> idList){
        PushTransferDataDetailDTO dto = new PushTransferDataDetailDTO();
        TransferDataDTO transferDataDTO = new TransferDataDTO();
        transferDataDTO.setDataItems(dataItems);
        String yyyyMMdd = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String taskId = yyyyMMdd.concat("_").concat(apiCode);
        String requestId = taskId.concat("_").concat(UUID.randomUUID().toString().substring(0, 5)) + System.currentTimeMillis();
        transferDataDTO.setRequestId(requestId);
        dto.setApiCode(apiCode);
        dto.setJsonData(JSON.toJSONString(transferDataDTO));
        Result result = marketingApiService.pushMarketingApiTransfer(dto, null);
        // 响应结果解析
        if(ResultCode.SUCCESS.getValue().equals(result.getCode())){
            // 根据响应结果更新数据库数据表-status成功
            queryUserRealMessageMapper.updateStatusByIdList(2, idList);
        }else{
            // 根据响应结果更新数据库数据表-status失败
            queryUserRealMessageMapper.updateStatusByIdList(3, idList);
        }
    }

}
