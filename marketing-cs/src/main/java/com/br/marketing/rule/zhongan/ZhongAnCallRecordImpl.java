package com.br.marketing.rule.zhongan;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSONObject;
import com.br.common.log.AlertLog;
import com.br.common.util.BrCipherMaker;
import com.br.marketing.bo.SyncUserValidityPeriodsBO;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.client.zhongan.input.ZaRosterLockingDataDTO;
import com.br.marketing.common.constants.rediskey.RedisKeyConstant;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.DateHelper;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.dto.customer.CallRecordBO;
import com.br.marketing.entity.MarketingSyncUser;
import com.br.marketing.mapper.MarketingSyncInfoMapper;
import com.br.marketing.mapper.ZhonganRosterLockingDataMapper;
import com.br.marketing.rule.AssembleData;
import com.br.marketing.service.TransferDataValidityPeriodService;
import com.br.marketing.strategy.InterfaceHandlerEnum;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

/**
 * 众安拨打明细入库规则
 */
@Service
@Slf4j
public class ZhongAnCallRecordImpl implements AssembleData<ZaRosterLockingDataDTO> {

    @Autowired
    private ZhonganRosterLockingDataMapper zhonganRosterLockingDataMapper;

    @Resource
    private RedisChgService redisChgService;

    @Resource
    private MarketingSyncInfoMapper marketingSyncInfoMapper;

    @Resource
    private TransferDataValidityPeriodService transferDataValidityPeriodService;

    final static DateTimeFormatter YYYYMMDDSHORTDF = DateTimeFormatter.ofPattern(DateHelper.SHORT_DATE_FORMAT);
    final static DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
    
    @Override
    public ZaRosterLockingDataDTO assemble(Object transmitFact, ProcessHandlerContext context) {
        CallRecordBO bo = (CallRecordBO) transmitFact;
//        log.warn("众安拨打明细符合落库规则，id={}", bo.getId());
        //上传表获取手机号，转为md5加密
        String cell = "";
        MarketingSyncUser syncUser = marketingSyncInfoMapper.getNewestByCusnumAndStatus(bo.getApiCode(), bo.getCaseNum());
        if(syncUser != null && StringUtils.isNotBlank(syncUser.getCell())){
            cell = DigestUtils.md5DigestAsHex(BrCipherMaker.getInstance().decode(syncUser.getCell()).getBytes());
        }
        //callStartTime 取 yyyy-MM-dd
        String bizDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        if(bo.getDetail() != null && bo.getDetail().getCallEndTime() != null ){
            try {
                bizDate = df.format(bo.getDetail().getCallStartTime());
            }catch (Exception e){
                e.printStackTrace();
                log.error("众安拨打明细时间格式转换出错！" + e.getMessage());
            }
        }

        String userProperties = bo.getDetail().getUserProperties();
        JSONObject jo = JSONObject.parseObject(userProperties);
        String userType = jo.getString("userType");

        ZaRosterLockingDataDTO data = new ZaRosterLockingDataDTO();
        data.setApiCode(bo.getApiCode());
        data.setLocalId(bo.getId());
        data.setCaseNum(bo.getCaseNum());
        data.setMobileMd5(cell);
        data.setBizDate(bizDate);
        data.setTag("MG");
        data.setDataSource(2);
        data.setUserType(userType);
        data.setIsConnect(ObjectUtil.isEmpty(bo.getDetail()) ? null : bo.getDetail().getIsConnect());
        return data;
    }

    @Override
    public boolean isNeedAssemble(Object transmitFact, ProcessHandlerContext context) {
        //1.剔除黑名单（callStatus=12）数据
        //2.到上传表根据caseNum匹配最新手机号
        //3.手机号在 众安明细锁定表 当日去重
        if (!(transmitFact instanceof CallRecordBO)){
            return false;
        }

        CallRecordBO bo = (CallRecordBO) transmitFact;
        if(bo.getDetail() != null && bo.getDetail().getCallStatus() != null && 12 == bo.getDetail().getCallStatus()){
            //黑名单
            custNumCache(bo.getCaseNum());
            //Set<String> smembers = redisChgService.smembers(RedisKeyConstant.zhongAnblackCusNumToday);
            //log.warn("众安拨打明细黑名单redis数据："+ smembers);
            return false;
        }

        String userProperties = bo.getDetail().getUserProperties();
        if(StringUtils.isEmpty(userProperties)){
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.EXCEPTION_ZHONGAN_CALL_RECORD.getCode(),
                    AlarmSendCodeEnum.EXCEPTION_ZHONGAN_CALL_RECORD.getMessage()+"userProperties字段为空" +
                            ", caseNum: " + bo.getCaseNum() +
                            ", id: " + bo.getId(),
                    "众安通话明细回调告警"));
            return false;
        }
        JSONObject jo = JSONObject.parseObject(userProperties);
        if(jo == null ){
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.EXCEPTION_ZHONGAN_CALL_RECORD.getCode(),
                    AlarmSendCodeEnum.EXCEPTION_ZHONGAN_CALL_RECORD.getMessage()+"userProperties格式不正确" +
                            ", caseNum: " + bo.getCaseNum() +
                            ", id: " + bo.getId(),
                    "众安通话明细回调告警"));
            return false;
        }
        String userType = jo.getString("userType");
        if(StringUtils.isEmpty(userType)){
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.EXCEPTION_ZHONGAN_CALL_RECORD.getCode(),
                    AlarmSendCodeEnum.EXCEPTION_ZHONGAN_CALL_RECORD.getMessage()+"userType字段未传" +
                            ", caseNum: " + bo.getCaseNum() +
                            ", id: " + bo.getId(),
                    "众安通话明细回调告警"));
            return false;
        }

        Set<String> custNums = new HashSet<>();
        custNums.add(bo.getCaseNum());

        Map<String, SyncUserValidityPeriodsBO> keyToSyncUserBO = transferDataValidityPeriodService
                .getValidityPeriodsByCustNumAndUserType(custNums, userType, bo.getApiCode(), new Date());

        SyncUserValidityPeriodsBO syncUserValidityPeriodsBO = keyToSyncUserBO.get(bo.getCaseNum());
        if(syncUserValidityPeriodsBO == null){
            log.warn("众安通话明细回调, 未匹配到上传数据, caseNum: {}, userType: {}, id: {}", bo.getCaseNum(), userType, bo.getId());
            return false;
        }
        List<MarketingSyncUser> syncUsers = syncUserValidityPeriodsBO.getSyncUsers();
        if(syncUsers == null || syncUsers.size()<1){
            log.warn("众安通话明细回调, 未匹配到上传数据, caseNum: {}, userType: {}, id: {}", bo.getCaseNum(), userType, bo.getId());
            return false;
        }
        return true;
    }

    public void custNumCache(String custNum){
        redisChgService.saddMember(RedisKeyConstant.zhongAnblackCusNumToday, custNum);
        //第二天凌晨失效
        if (redisChgService.exists(RedisKeyConstant.zhongAnblackCusNumToday)) {
            redisChgService.expire(RedisKeyConstant.zhongAnblackCusNumToday, getKeyExpiration());
        }
    }

    /**
     * 获取当前时间到第二天凌晨的秒
     *
     * @dateTime 2022/11/15 10:21
     */
    private int getKeyExpiration() {
        final LocalDateTime now = LocalDateTime.now();
        // 当前毫秒数
        long l = now.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        LocalDateTime localDateTime = now.plusDays(1);
        // 第二天凌晨毫秒数
        long l1 = localDateTime.toLocalDate().atStartOfDay().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        return (int) (l1 - l) / 1000;
    }

    @Override
    public String label() {
        return "ZhongAn_CallRecordData_Insert";
    }

    @Override
    public Integer dataDirection() {
        return InterfaceHandlerEnum.ZHONGAN_LOCK_DATA_INSERT.getCode();
    }

    @Override
    public Integer ruleDataCollection() {
        return null;
    }

}
