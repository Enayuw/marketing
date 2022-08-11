package com.br.marketing.rule.ppd;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.common.util.BrCipherMaker;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.client.dassservice.input.DassImportDataDTO;
import com.br.marketing.client.dassservice.input.userdata.BatchRealTimeUserDataDTO;
import com.br.marketing.common.constants.rediskey.RedisKeyConstant;
import com.br.marketing.common.utils.AESUtil;
import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.context.RuleDataCollectionEnum;
import com.br.marketing.context.impl.PPDCollectDataImpl;
import com.br.marketing.entity.MarketingSyncUser;
import com.br.marketing.entity.MarketingTransferSyncUser;
import com.br.marketing.entity.PhoneSaleExtendInfo;
import com.br.marketing.entity.PhoneSaleExtendInfoExample;
import com.br.marketing.mapper.PhoneSaleExtendInfoMapper;
import com.br.marketing.origin.MqFact;
import com.br.marketing.rule.AssembleData;
import com.br.marketing.strategy.InterfaceHandlerEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * code is far away from bug with the animal protecting
 * ┏┓　　　┏┓
 * ┏┛┻━━━┛┻┓
 * ┃　　　　　　　┃
 * ┃　　　━　　　┃
 * ┃　┳┛　┗┳　┃
 * ┃　　　　　　　┃
 * ┃　　　┻　　　┃
 * ┃　　　　　　　┃
 * ┗━┓　　　┏━┛
 * 　　┃　　　┃神兽保佑
 * 　　┃　　　┃代码无BUG！
 * 　　┃　　　┗━━━┓
 * 　　┃　　　　　　　┣┓
 * 　　┃　　　　　　　┏┛
 * 　　┗┓┓┏━┳┓┏┛
 * 　　　┃┫┫　┃┫┫
 * 　　　┗┻┛　┗┻┛
 *
 * @Description : 拍拍贷自动化转人工-3710014
 * ---------------------------------
 * @Author : jilong.xu
 * @Date : Create in 2022/6/15 17:12
 */

@Service
@Slf4j
public class PPDAutoArtificialTransferImpl implements AssembleData<BatchRealTimeUserDataDTO> {


    @Value("${api.dass.aesKey:00}")
    private String aesKey;
    @Resource
    private PhoneSaleExtendInfoMapper phoneSaleExtendInfoMapper;

    @Resource
    RedisChgService redisChgService;

    private final static Map<String,String> map = new HashMap();
    private final static Map<String,String> auditAmountMap = new HashMap();

    static{
        map.put("1","a");
        map.put("2","b");
        map.put("3","c");
        map.put("a","a,b,c");
        map.put("b","a,b,c");
        map.put("c","c");
        auditAmountMap.put("1","1W以下");
        auditAmountMap.put("2","1W-2W");
        auditAmountMap.put("3","2W-3W");
        auditAmountMap.put("4","3W以上");
    }

    @Override
    public BatchRealTimeUserDataDTO assemble(Object transmitFact, ProcessHandlerContext context) {
        MarketingTransferSyncUser transfer = (MarketingTransferSyncUser) transmitFact;
        BatchRealTimeUserDataDTO batchRealTimeUserDataDTO = new BatchRealTimeUserDataDTO();

        PPDCollectDataImpl.PPDRuleNecessaryData ruleNecessaryData =
                (PPDCollectDataImpl.PPDRuleNecessaryData) context.getRuleNecessaryData();
        Map<String, MarketingSyncUser> customerMap = ruleNecessaryData.getCustomerMap();
        MarketingSyncUser marketingSyncUser = getSyncUser(customerMap, transfer.getCustNum());
        if (marketingSyncUser == null) {
            return null;
        }
        batchRealTimeUserDataDTO.setDassImportDataDTO(packageDassImportData(transfer, marketingSyncUser));
        return batchRealTimeUserDataDTO;
    }

    @Override
    public boolean isNeedAssemble(Object transmitFact, ProcessHandlerContext context) throws Exception {
        MqFact mqFact = context.getMqFact();
        //处理拍拍贷静置后的数据
        Integer isDelay = mqFact.getIsDelay();
        if (isDelay != null && isDelay == 1){
            MarketingTransferSyncUser transfer = (MarketingTransferSyncUser) transmitFact;
            PPDCollectDataImpl.PPDRuleNecessaryData ruleNecessaryData =
                    (PPDCollectDataImpl.PPDRuleNecessaryData) context.getRuleNecessaryData();
            MarketingTransferSyncUser newestTransferSyncUser = ruleNecessaryData.getCustomerTransferMap().get(transfer.getCustNum());
            MarketingSyncUser marketingSyncUser = ruleNecessaryData.getCustomerMap().get(transfer.getCustNum());
            if (StringUtils.isEmpty(marketingSyncUser) ){
                log.warn("上传表中无对应记录 --{}",transfer.getCustNum());
                return false;
            } else if (StringUtils.isEmpty(marketingSyncUser.getCell())) {
                log.warn("上传表中无对应手机号 --{}",transfer.getCustNum());
                return false;
            }
            //转化数据cust_num最新一条 ifTransform =1 或 ifTransform =-1
            if (Arrays.asList("1","-1").contains(newestTransferSyncUser.getIfTransform())){
                log.warn("转化数据cust_num:{} 最新一条ifTransform in(1,-1) --{}",transfer.getCustNum(),JSON.toJSONString(newestTransferSyncUser));
                return false;
            }
            // 根据userType获取对应的情况类型1,2,3->a,b,c
            String situation = map.get(transfer.getUserType());
            //根据情况类型获取判断该cust_num已经推送过的情况
            String pushed = map.get(situation);

            //根据客户上传日期，查询前三天到当天的数据
            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
            Date parse = simpleDateFormat.parse(transfer.getRequestData());
            calendar.setTime(parse);
            calendar.add(Calendar.DAY_OF_MONTH,-3);
            Date time = calendar.getTime();
            String _3Day = simpleDateFormat.format(time);

            String key = RedisKeyConstant.ppdPushDx.concat(":")
                    .concat(transfer.getApiCode()).concat(":")
                    .concat(transfer.getCustNum());
            String value = UUID.randomUUID().toString();

            //分布式锁，控制推电销判断逻辑顺序执行
            redisChgService.lock(key,value);
            PhoneSaleExtendInfoExample extendInfoExample = new PhoneSaleExtendInfoExample();
            extendInfoExample.createCriteria().andApiCodeEqualTo(transfer.getApiCode()).
                    andCustNumEqualTo(transfer.getCustNum()).andStatusIn(Arrays.asList(pushed.split(","))).
                    andAppletDateBetween(_3Day,transfer.getRequestData());
            int count = phoneSaleExtendInfoMapper.countByExample(extendInfoExample);
            if (count > 0){
                log.warn("当前cust_num: {} 情况: {},在时间段：{} - {} 出现{}",transfer.getCustNum(),situation,
                        _3Day,transfer.getRequestData(),pushed);
                redisChgService.unlock(key,value);
                return false;
            }else{
                savePhoneSaleExtendInfo(transfer,marketingSyncUser.getCusBatch());
                redisChgService.unlock(key,value);
                return true;
            }

        }

        return false;
    }

    @Override
    public String label() {
        return "PPD_TransferData_ArtificialBatch";
    }

    @Override
    public Integer dataDirection() {
        return InterfaceHandlerEnum.ARTIFICIAL_BATCH_REALTIME_DATA.getCode();
    }

    @Override
    public Integer ruleDataCollection() {
        return RuleDataCollectionEnum.PPD_DATA_COLLECTION.getCode();
    }

    private void savePhoneSaleExtendInfo(MarketingTransferSyncUser transfer,String cusBatch) {
        PhoneSaleExtendInfo phoneSaleExtendInfo = new PhoneSaleExtendInfo();
        phoneSaleExtendInfo.setApiCode(transfer.getApiCode());
        phoneSaleExtendInfo.setCustNum(transfer.getCustNum());
        phoneSaleExtendInfo.setTaskId(cusBatch);
        phoneSaleExtendInfo.setUserType(transfer.getUserType());
        phoneSaleExtendInfo.setAppletDate(transfer.getRequestData());
        phoneSaleExtendInfo.setAppletTime(transfer.getRequestTime());
        phoneSaleExtendInfo.setPStatus(1);
        phoneSaleExtendInfo.setStatus(map.get(transfer.getUserType()));
        phoneSaleExtendInfo.setCreateTime(new Date());
        phoneSaleExtendInfo.setType(transfer.getType());
        phoneSaleExtendInfo.setPushDxTime(new Date());
        phoneSaleExtendInfo.setTransformType("0");
        phoneSaleExtendInfo.setSourceId(transfer.getId());
        phoneSaleExtendInfoMapper.insertSelective(phoneSaleExtendInfo);
    }

    private DassImportDataDTO packageDassImportData(MarketingTransferSyncUser transfer, MarketingSyncUser syncUser) {
        DassImportDataDTO batchImportData = new DassImportDataDTO();
        batchImportData.setId(transfer.getId());
        String cell = BrCipherMaker.getInstance().decode(syncUser.getCell());
        String phone = AESUtil.aesEncrypty(cell, aesKey);
        String decodeName;
        String name = StringUtils.hasText(syncUser.getName()) ?
                (syncUser.getName().equals(decodeName = BrCipherMaker.getInstance().decode(syncUser.getName())) ? "1"
                        : decodeName) : "1";
        // 根据custNum取上传接口最新的name转成明文传输
        batchImportData.setName(name);
        batchImportData.setOrgname("ppdai");
        // 根据custNum取上传接口最新的cell转aes加密
        batchImportData.setPhone(phone);
        batchImportData.setUid(transfer.getCustNum());
        batchImportData.setUserType("2");
        batchImportData.setSource("16");
        // 根据userType获取对应的情况类型1,2,3->a,b,c
        String situation = map.get(transfer.getUserType());
        if ("c".equals(situation)) {
            batchImportData.setType("3");
            String reserveField1 = transfer.getReserveField1();
            JSONObject object = JSON.parseObject(reserveField1);
            if (!CollectionUtils.isEmpty(object)) {
                String auditAmount = object.getString("auditAmount");
                if (StringUtils.hasText(auditAmount)) {
                    String value = auditAmountMap.get(auditAmount);
                    batchImportData.setAuditAmount(StringUtils.hasText(value)?value:auditAmount);
                }
            }
        } else {
            batchImportData.setType("2");
        }
        return batchImportData;
    }
}
