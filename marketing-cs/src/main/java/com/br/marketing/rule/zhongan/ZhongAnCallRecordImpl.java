package com.br.marketing.rule.zhongan;

import com.br.common.util.BrCipherMaker;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.client.zhongan.input.ZaRosterLockingDataDTO;
import com.br.marketing.common.constants.rediskey.RedisKeyConstant;
import com.br.marketing.common.utils.DateHelper;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.dto.customer.CallRecordBO;
import com.br.marketing.entity.MarketingSyncUser;
import com.br.marketing.entity.ZhonganRosterLockingDataExample;
import com.br.marketing.mapper.MarketingSyncInfoMapper;
import com.br.marketing.mapper.ZhonganRosterLockingDataMapper;
import com.br.marketing.rule.AssembleData;
import com.br.marketing.strategy.InterfaceHandlerEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

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

    final static DateTimeFormatter YYYYMMDDSHORTDF = DateTimeFormatter.ofPattern(DateHelper.SHORT_DATE_FORMAT);
    final static DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
    
    @Override
    public ZaRosterLockingDataDTO assemble(Object transmitFact, ProcessHandlerContext context) {
        CallRecordBO bo = (CallRecordBO) transmitFact;
        log.warn("众安拨打明细符合落库规则，id={}", bo.getId());
        //上传表获取手机号，转为md5加密
        String cell = "";
        MarketingSyncUser syncUser = marketingSyncInfoMapper.getNewestByCusnumAndStatus(bo.getApiCode(), bo.getCaseNum());
        if(syncUser != null && StringUtils.isNotBlank(syncUser.getCell())){
            cell = DigestUtils.md5DigestAsHex(BrCipherMaker.getInstance().decode(syncUser.getCell()).getBytes());
        }
        //callStartTime 取 yyyy-MM-dd
        String bizDate = "";
        if(bo.getDetail() != null && bo.getDetail().getCallEndTime() != null){
            try {
                bizDate = df.format(bo.getDetail().getCallStartTime());
            }catch (Exception e){
                e.printStackTrace();
                log.error("众安拨打明细时间格式转换出错！" + e.getMessage());
            }
        }
        ZaRosterLockingDataDTO data = new ZaRosterLockingDataDTO();
        data.setApiCode(bo.getApiCode());
        data.setLocalId(bo.getId());
        data.setMobileMd5(cell);
        data.setBizDate(bizDate);
        data.setTag("MG");
        data.setDataSource(2);
        return data;
    }

    @Override
    public boolean isNeedAssemble(Object transmitFact, ProcessHandlerContext context) {
        //1.剔除黑名单（callStatus=12）数据
        //2.到上传表根据caseNum匹配最新手机号
        //3.手机号在 众安明细锁定表 当日去重
        boolean flag = Boolean.FALSE;
        if (transmitFact instanceof CallRecordBO){
            CallRecordBO bo = (CallRecordBO) transmitFact;
            if(bo.getDetail() != null && bo.getDetail().getCallStatus() != null && 12 == bo.getDetail().getCallStatus()){
                //黑名单
                custNumCache(bo.getCaseNum());
                return flag;
            }
            //上传表获取手机号，转为md5加密
            MarketingSyncUser syncUser = marketingSyncInfoMapper.getNewestByCusnumAndStatus(bo.getApiCode(), bo.getCaseNum());
            if(syncUser != null && StringUtils.isNotBlank(syncUser.getCell())){
                String cell = DigestUtils.md5DigestAsHex(BrCipherMaker.getInstance().decode(syncUser.getCell()).getBytes());
                //获取当前日期
                String today = new Date().toInstant().atZone(ZoneId.systemDefault()).toLocalDate().format(YYYYMMDDSHORTDF);
                Integer createDate = Integer.valueOf(today);
                ZhonganRosterLockingDataExample example = new ZhonganRosterLockingDataExample();
                example.createCriteria().andApiCodeEqualTo(bo.getApiCode()).andCreateDateEqualTo(createDate).andMobileMd5EqualTo(cell);
                int count = zhonganRosterLockingDataMapper.countByExample(example);
                if(count > 0){
                    return flag;
                }
                flag = Boolean.TRUE;
            }
        }
        return flag;
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
