package com.br.marketing.service.Impl.zhijia;

import com.alibaba.fastjson.JSONObject;
import com.br.common.encryption.Md5Utils;
import com.br.marketing.client.AlarmApiClient;
import com.br.marketing.client.zhijia.ZhiJiaClient;
import com.br.marketing.client.zhijia.input.ReqAddZhiJiaClueDTO;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.dto.zhijia.CityCountyDataDTO;
import com.br.marketing.entity.*;
import com.br.marketing.mapper.LocalFileMapper;
import com.br.marketing.mapper.ZhiJiaClueBackDataMapper;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.google.api.client.util.Base64;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @ClassName ZhiJiaClueFeedBackServiceImpl
 * @Description 之家线索匹配
 * @Author kongbx
 * @Date 2024/7/10 15:44
 */
@Service
@Slf4j
public class ZhiJiaClueFeedBackServiceImpl implements ZhiJiaClueFeedBackService{

    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Resource
    private ZhiJiaClueBackDataMapper zhiJiaClueBackDataMapper;

    @Resource
    ZhiJiaDataProcessService zhiJiaDataProcessService;

    @Resource
    LocalFileMapper localFileMapper;

    @Resource
    private ZhiJiaClient zhiJiaClient;

    @Resource
    private AlarmApiClient alarmClient;

    @Value("${api.zhijia.zhiJiaClientAppid:00}")
    private String zhiJiaClientAppid;

    @Override
    public Result process(Long id) {
        LocalFile localFile = localFileMapper.selectByPrimaryKey(id);
        if (localFile == null) {
            return new Result().setCode(ResultCode.SUCCESS.getValue()).setMessage("文件不存在");
        }

        ThreadPoolExecutor zhiJiaCollidingThread =
                BrExecutors.getThreadPool(marketingCommonConfig.getZhiJiaCollidingThread(), marketingCommonConfig.getZhiJiaCollidingThread());

        Long st1 = System.currentTimeMillis();
        localFile.setPushStartTime(new Date());

        Long minId = null;
        Boolean isContiue = Boolean.TRUE;
        while (isContiue) {
            // 查询未推送数据
            List<ZhiJiaClueBackData> zhiJiaClueBackDataList = zhiJiaClueBackDataMapper.getBatchById(id, minId);
            if (zhiJiaClueBackDataList.isEmpty()) {
                isContiue = Boolean.FALSE;
                continue;
            }
            minId = zhiJiaClueBackDataList.get(zhiJiaClueBackDataList.size() - 1).getId() + 1;

            // 开始推送
            List<List<ZhiJiaClueBackData>> partition = ListUtils.partition(zhiJiaClueBackDataList, 1000);

            partition.forEach((List<ZhiJiaClueBackData> p) -> {
                zhiJiaCollidingThread.submit(() -> pushZhiJiaCollidingSync(p));
            });

        }
        zhiJiaCollidingThread.shutdown();
        try {
            while (!zhiJiaCollidingThread.awaitTermination(10L, TimeUnit.SECONDS)) {
                log.info("之家创建线索接口线程池关闭");
            }
        } catch (InterruptedException ex) {
            zhiJiaCollidingThread.shutdownNow();
            log.error("之家创建线索接口线程池关闭！异常", ex);
            Thread.currentThread().interrupt();
        }

        ZhiJiaClueBackDataExample example = new ZhiJiaClueBackDataExample();
        example.createCriteria().andLocalIdEqualTo(localFile.getId())
                .andPushStatusEqualTo(2)
                .andStatusEqualTo(1);
        int num = zhiJiaClueBackDataMapper.countByExample(example);
        localFile.setPushEndTime(new Date());
        localFile.setPushNumber(num);
        localFile.setPushStatus("2");
        localFileMapper.updateByPrimaryKeySelective(localFile);
        //统计告警
        if (!localFile.getPushNumber().equals(localFile.getActualNumber())) {
            sendAlarm(localFile.getActualNumber() - localFile.getPushNumber(), "之家创建回传线索失败数量统计");
        }
        log.warn("之家创建回传线索结束，耗时：{} ms", System.currentTimeMillis() - st1);

        return new Result().setCode(ResultCode.SUCCESS.getValue()).setDate(false).setMessage("成功");
    }

    public void pushZhiJiaCollidingSync(List<ZhiJiaClueBackData> zhiJiaClueBackDataList){

        for (ZhiJiaClueBackData zhiJiaClueBackData : zhiJiaClueBackDataList) {
            Long id = zhiJiaClueBackData.getId();
            try {
                ReqAddZhiJiaClueDTO reqAddZhiJiaClueDTO = new ReqAddZhiJiaClueDTO();
                // 调用省市区接口
                CityCountyDataDTO cityCountyDataDTO = zhiJiaDataProcessService.matchCityAndCounty(null,null,zhiJiaClueBackData);
                if(cityCountyDataDTO.getIsMatch()){
                    reqAddZhiJiaClueDTO.setCid(cityCountyDataDTO.getCId());
                    reqAddZhiJiaClueDTO.setCountyid(cityCountyDataDTO.getCountyId());
                }else {
                    updatePushStatus(id, 4, null, cityCountyDataDTO.getErrorMsg());
                    continue;
                }

                // 调用车辆信息接口
                Result<ZhiJiaCarSeriesInfo> zhiJiaCarInfo = zhiJiaDataProcessService.getZhiJiaCarInfo(zhiJiaClueBackData);
                if(ResultCode.SUCCESS.getValue().equals(zhiJiaCarInfo.getCode())){
                    ZhiJiaCarSeriesInfo data = zhiJiaCarInfo.getData();
                    reqAddZhiJiaClueDTO.setBrandid(data.getBrandId() != null ? String.valueOf(data.getBrandId()) : "");
                    reqAddZhiJiaClueDTO.setSeriesid(data.getSeriesId() != null ? String.valueOf(data.getSeriesId()) : "");
                }else {
                    updatePushStatus(id, 4, null, zhiJiaCarInfo.getMessage());
                    continue;
                }

                // 调用高质线索创建接口
                Result<Integer> result = zhiJiaClient.addZhiJiaClue(buildAddZhiJiaClue(zhiJiaClueBackData,reqAddZhiJiaClueDTO));
                // 更新结果
                if (ResultCode.SUCCESS.getValue().equals(result.getCode())) {
                    // 匹配成功则更新状态和cclId
                    Integer cclId = result.getData();
                    updatePushStatus(id, 2, cclId, result.getMessage());
                } else {
                    // 匹配失败更新
                    updatePushStatus(id, 4, null, result.getMessage());
                }
            }catch (Exception e){
                updatePushStatus(id, 3, null, e.getMessage());
                log.error("调用高质线索创建接口异常！", e.getMessage());
            }
        }
    }

    private void updatePushStatus(Long id, int status, Integer cclId, String message) {
        ZhiJiaClueBackDataExample example = new ZhiJiaClueBackDataExample();
        example.createCriteria().andIdEqualTo(id);
        ZhiJiaClueBackData record = new ZhiJiaClueBackData();
        record.setPushStatus(status);
        record.setDataMessage(message);
        if(cclId != null){
            record.setCclId(cclId);
        }
        zhiJiaClueBackDataMapper.updateByExampleSelective(record,example);
    }

    private ReqAddZhiJiaClueDTO buildAddZhiJiaClue(ZhiJiaClueBackData zhiJiaClueBackData,ReqAddZhiJiaClueDTO dto) {
        dto.setAccess_token(zhiJiaDataProcessService.getToken());
        dto.setMobile(zhiJiaClueBackData.getCell());
        dto.setMobilecode(encryptCell(zhiJiaClueBackData.getCell()));
        dto.setFirstregtime(zhiJiaClueBackData.getFirstRegTime());
        dto.setMileage(zhiJiaClueBackData.getMileAge());
        dto.setAppid(Integer.valueOf(zhiJiaClientAppid));
        return dto;
    }

    public String encryptCell(String cell) {
        String keyStr = StringUtils.substring(Md5Utils.cell32(zhiJiaClientAppid), 0, 16);
        String ivStr = reverseString(keyStr);
        return encrypt(cell, keyStr, ivStr);
    }

    public static String reverseString(String s) {
        return new StringBuilder(s).reverse().toString();
    }

    public static String encrypt(String plaintext, String keyStr, String ivStr) {
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            SecretKeySpec keySpec = new SecretKeySpec(keyStr.getBytes(), "AES");
            IvParameterSpec ivSpec = new IvParameterSpec(ivStr.getBytes());
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);
            byte[] encryptedBytes = cipher.doFinal(plaintext.getBytes());
            return new String(Base64.encodeBase64(encryptedBytes));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void sendAlarm(Integer failNum, String title) {
        if (failNum > 0) {
            try {
                alarmClient.sendAlarm("推送失败条数=" + failNum, title, AlarmSendCodeEnum.EXCEPTION_ZHIJIA_ERROR.getCode());
            } catch (Exception ex) {
                log.error(ex.getMessage(), ex);
            }
        }
    }

}
