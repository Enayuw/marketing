package com.br.marketing.service.Impl.zhongbang;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.br.common.util.BrCipherMaker;
import com.br.common.util.MD5Utils;
import com.br.marketing.bo.SyncUserValidityPeriodsBO;
import com.br.marketing.client.zbank.ZbankResponse;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.dto.zbank.ZbankLabelRatingReResultDTO;
import com.br.marketing.entity.LocalFile;
import com.br.marketing.entity.MarketingSyncUser;
import com.br.marketing.entity.ZhongbangVoiceFileDetail;
import com.br.marketing.mapper.LocalFileMapper;
import com.br.marketing.mapper.ZhongbangVoiceFileDetailMapper;
import com.br.marketing.service.TransferDataValidityPeriodService;
import com.br.marketing.strategy.MethodRetryHandlerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 众邦录音文件自动推送处理逻辑
 * @Author: yu.xia@brgroup.com
 * @Date: 2024-05-16
 */
@Service
@Slf4j
public class ZhongBangPushVoiceServiceImpl implements IZhongBangPushVoiceService {

    @Resource
    private ZhongbangVoiceFileDetailMapper zhongBangVoiceFileDetailMapper;

    @Resource
    private MethodRetryHandlerService methodRetryHandlerService;
    @Resource
    private TransferDataValidityPeriodService transferDataValidityPeriodService;
    @Resource
    private LocalFileMapper localFileMapper;

    @Override
    public void pageAndPush() {
        Long detailId = null;
        // 是否是最后一批数据或已经完成
        Boolean finishFlag = Boolean.FALSE;
        // 查询 push_status=0 的数量级
        Integer countPushStatus0 = zhongBangVoiceFileDetailMapper.selectPushStatus0Count();
        String pushDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        while(true){
            Integer countPushStatus1 = zhongBangVoiceFileDetailMapper.selectPushStatusCount(1,null);
            finishFlag = countPushStatus0==0 && countPushStatus1<=500;
            // 1.查询500个满足条件的数据
            List<ZhongbangVoiceFileDetail> detailList = zhongBangVoiceFileDetailMapper.selectByPushStatus1(detailId);
            boolean breakFlag = (countPushStatus0>0 && detailList.size()<500) || detailList.size()<1;
            if(breakFlag){
                break;
            }
            detailId = detailList.get(detailList.size()-1).getId();
            // 2.参数拼接
            List<Long> idList = new ArrayList<>();
            List<Long> localIdList = new ArrayList<>();
            List<Long> errorIdList = new ArrayList<>();
            JSONArray flArray = new JSONArray();
            Iterator<ZhongbangVoiceFileDetail> iterator = detailList.iterator();
            while(iterator.hasNext()){
                ZhongbangVoiceFileDetail t = iterator.next();
                Set<String> custNumSet = new HashSet<>();
                Long id = t.getId();
                idList.add(id);
                Long localId = t.getLocalId();
                localIdList.add(localId);
                String apiCode = t.getApiCode();
                String custNum = t.getCaseNum();
                custNumSet.add(custNum);
                Map<String, SyncUserValidityPeriodsBO> validityPeriodsByCustNum =
                        transferDataValidityPeriodService.getValidityPeriodsByCustNum(custNumSet, apiCode, t.getCreateTime());
                if(null != validityPeriodsByCustNum){
                    SyncUserValidityPeriodsBO syncUserValidityPeriodsBO = validityPeriodsByCustNum.get(custNum);
                    if(null != syncUserValidityPeriodsBO){
                        List<MarketingSyncUser> syncUserList = syncUserValidityPeriodsBO.getSyncUsers();
                        MarketingSyncUser marketingSyncUser = syncUserList.get(0);
                        JSONObject flObject = new JSONObject();
                        flObject.put("OpnPltfrmId",t.getCustomerFileId());
                        flObject.put("CstNo",custNum);
                        String idCard = marketingSyncUser.getIdCard();
                        if(StringUtils.isNotBlank(idCard)){
                            String md5Id = MD5Utils.cell32(BrCipherMaker.getInstance().decode(idCard));
                            flObject.put("IdentNo", md5Id);
                        }
                        flObject.put("MblPhnId",marketingSyncUser.getCellMd5().toLowerCase());
                        flObject.put("RcrdTy",t.getCallType());
                        flObject.put("RcrdDt",t.getCallStartTime());
                        String name = marketingSyncUser.getName();
                        if(StringUtils.isNotBlank(name)){
                            String md5Name = MD5Utils.cell32(BrCipherMaker.getInstance().decode(name));
                            flObject.put("Rmk1", md5Name);
                        }
                        flArray.add(flObject);
                    }else{
                        errorIdList.add(id);
                    }
                }else{
                    errorIdList.add(id);
                }
            }
            if(errorIdList.size()>0){
                log.warn("不在有效期内的b_zhongbang_voice_file_detail数据id是:{}", JSON.toJSONString(errorIdList));
                zhongBangVoiceFileDetailMapper.updateBatchByIds(errorIdList, 4, pushDate);
                if(detailList.size() == errorIdList.size()){
                    continue;
                }else{
                    idList.removeAll(errorIdList);
                }
            }
            JSONObject paramJson = new JSONObject();
            paramJson.put("ids",idList);
            paramJson.put("FlArray",flArray);
            // 3.调用推送接口
            Result<ZbankResponse<ZbankLabelRatingReResultDTO>> result =
                    methodRetryHandlerService.pushZbankRecodFileRe(paramJson, null);
            if(ResultCode.SUCCESS.getValue().equals(result.getCode())){
                // 4.判断推送结果状态，成功就更新表状态
                zhongBangVoiceFileDetailMapper.updateBatchByIds(idList, 2, pushDate);
            }else if(ResultCode.INTERNAL_SERVER_ERROR.getValue().equals(result.getCode())){
                // 4.判断推送需要重试，不处理状态
                log.warn("众邦录音文件自动推送流控，后续要重试，入参:{}--反参code:{}--message:{}--data:{}"
                        , paramJson, result.getMessage(), result.getCode(), result.getData());
            }else{
                // 4.判断推送结果失败，就更新表状态
                log.warn("众邦录音文件自动推送异常，入参:{}--反参code:{}--message:{}--data:{}"
                        , paramJson, result.getMessage(), result.getCode(), result.getData());
                zhongBangVoiceFileDetailMapper.updateBatchByIds(idList, 3, pushDate);
            }
        }
        if(finishFlag){
            List<Long> fileIdList = zhongBangVoiceFileDetailMapper.selectDistinctLocalIdtikv_();
            fileIdList.stream().forEach((Long t)->{
                Integer pushStatus2Total = zhongBangVoiceFileDetailMapper.selectPushStatusCount(2,t);
                Integer pushStatus3Total = zhongBangVoiceFileDetailMapper.selectPushStatusCount(3,t);
                Integer pushStatus4Total = zhongBangVoiceFileDetailMapper.selectPushStatusCount(4,t);
                LocalFile record = new LocalFile();
                record.setId(t);
                record.setPushNumber(pushStatus2Total);
                record.setErrorActualNumber(pushStatus3Total+pushStatus4Total);
                record.setPushEndTime(new Date());
                localFileMapper.updatePushNumber(record);
            });
        }
    }
}
