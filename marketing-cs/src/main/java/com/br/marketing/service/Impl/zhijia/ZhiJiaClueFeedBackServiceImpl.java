package com.br.marketing.service.Impl.zhijia;

import com.alibaba.fastjson.JSONObject;
import com.br.common.encryption.Md5Utils;
import com.br.common.log.AlertLog;
import com.br.marketing.client.AlarmApiClient;
import com.br.marketing.client.zhijia.ZhiJiaClient;
import com.br.marketing.client.zhijia.input.ReqAddZhiJiaClueDTO;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.dto.zhijia.CityCountyDataDTO;
import com.br.marketing.dto.zhijia.ZhiJiaCarInfoDTO;
import com.br.marketing.entity.*;
import com.br.marketing.enums.DingDingAlarmFunctionEnum;
import com.br.marketing.mapper.LocalFileMapper;
import com.br.marketing.mapper.ZhiJiaClueBackDataMapper;
import com.br.marketing.rpcclient.RpcClientProxy;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.webhook.dingding.msgtype.DingDingMarkdownMessage;
import com.br.marketing.webhook.dingding.service.DingDingRobotHookService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.Map;
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
    private DingDingRobotHookService dingDingRobotHookService;

    @Value("${api.qifu.isProxy:true}")
    private boolean isProxy;

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
            return new Result().setCode(ResultCode.FAIL.getValue()).setMessage("文件不存在");
        }

        ThreadPoolExecutor zhiJiaCollidingThread =
                BrExecutors.getThreadPool(marketingCommonConfig.getZhiJiaCollidingThread(), marketingCommonConfig.getZhiJiaCollidingThread());

        Long st1 = System.currentTimeMillis();
        localFile.setPushStartTime(new Date());
        // 获取省市区、车辆信息初始化配置
        List<ZhijiaCityConfig> cityConfigList = zhiJiaDataProcessService.getCityConfigList();
        List<ZhijiaCountyConfig> countyConfigList = zhiJiaDataProcessService.getCountyConfigList();
        List<ZhiJiaCarBrandInfo> carBrandInfos = zhiJiaDataProcessService.getCarBrandInfos();

        if(cityConfigList.isEmpty() || countyConfigList.isEmpty()){
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.ZHIJIA_SERVICEERROR.getCode(), "之家初始化市区信息为空!"));
            return new Result().setCode(ResultCode.FAIL.getValue()).setMessage("之家初始化市区信息为空");
        }
        if(carBrandInfos.isEmpty()){
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.ZHIJIA_SERVICEERROR.getCode(), "之家初始化车辆信息为空!"));
            return new Result().setCode(ResultCode.FAIL.getValue()).setMessage("之家初始化车辆信息为空");
        }

        Long minId = null;
        Boolean isContiue = Boolean.TRUE;
        while (isContiue) {
            // 查询未推送数据
            Integer pageSize = marketingCommonConfig.getZhiJiaQryUserMessageSize();
            List<ZhiJiaClueBackData> zhiJiaClueBackDataList = zhiJiaClueBackDataMapper.getBatchById(id, pageSize, minId);
            if (zhiJiaClueBackDataList.isEmpty()) {
                isContiue = Boolean.FALSE;
                continue;
            }
            minId = zhiJiaClueBackDataList.get(zhiJiaClueBackDataList.size() - 1).getId() + 1;

            // 开始推送
            List<List<ZhiJiaClueBackData>> partition = ListUtils.partition(zhiJiaClueBackDataList, 50);

            partition.forEach((List<ZhiJiaClueBackData> p) -> {
                zhiJiaCollidingThread.submit(() -> pushZhiJiaCollidingSync(p,
                        cityConfigList,countyConfigList,
                        carBrandInfos));
            });

        }
        zhiJiaCollidingThread.shutdown();
        try {
            while (!zhiJiaCollidingThread.awaitTermination(10L, TimeUnit.SECONDS)) {
                log.warn("之家创建线索接口线程池关闭");
            }
        } catch (InterruptedException ex) {
            zhiJiaCollidingThread.shutdownNow();
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.ZHIJIA_SERVICEERROR.getCode(), "之家创建线索接口线程池关闭！异常"), ex);
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
            sendAlarm(localFile.getActualNumber() - localFile.getPushNumber(), "之家创建回传线索失败数量统计,localId："+localFile.getId());
        }
        log.warn("之家创建回传线索结束，耗时：{} ms", System.currentTimeMillis() - st1);

        return new Result().setCode(ResultCode.SUCCESS.getValue()).setDate(false).setMessage("成功");
    }

    public void pushZhiJiaCollidingSync(List<ZhiJiaClueBackData> zhiJiaClueBackDataList,
                                        List<ZhijiaCityConfig> cityConfigList,List<ZhijiaCountyConfig> countyConfigList,
                                        List<ZhiJiaCarBrandInfo> carBrandInfos){

        for (ZhiJiaClueBackData zhiJiaClueBackData : zhiJiaClueBackDataList) {
            Long id = zhiJiaClueBackData.getId();
            try {
                ReqAddZhiJiaClueDTO reqAddZhiJiaClueDTO = new ReqAddZhiJiaClueDTO();
                // 匹配省市区信息
                CityCountyDataDTO cityCountyDataDTO =
                        zhiJiaDataProcessService.matchCityAndCounty(cityConfigList,countyConfigList,zhiJiaClueBackData);
                if(!cityCountyDataDTO.getIsMatch()){
                    log.warn("匹配省市区信息异常, 请求：{}， 返回：{} "
                            , JSONObject.toJSONString(zhiJiaClueBackData), JSONObject.toJSONString(cityCountyDataDTO));
                    updatePushStatus(id, 4, null, cityCountyDataDTO.getErrorMsg());
                    // 钉钉报警
                    StringBuilder sb = new StringBuilder("# 之家省市区匹配异常\n");
                    errorStatistics(id,cityCountyDataDTO.getErrorMsg(),sb);
                    continue;
                }
                // 匹配车牌信息
                ZhiJiaCarInfoDTO zhiJiaCarBrandInfo = zhiJiaDataProcessService.getZhiJiaCarBrandInfo(zhiJiaClueBackData, carBrandInfos);
                if(!zhiJiaCarBrandInfo.getIsMatch()){
                    log.warn("匹配车牌信息异常, 请求：{}， 返回：{} "
                            , JSONObject.toJSONString(zhiJiaClueBackData), JSONObject.toJSONString(zhiJiaCarBrandInfo));
                    updatePushStatus(id, 4, null, zhiJiaCarBrandInfo.getErrorMsg());
                    // 钉钉报警
                    StringBuilder sb = new StringBuilder("# 之家车牌信息匹配异常\n");
                    errorStatistics(id,zhiJiaCarBrandInfo.getErrorMsg(),sb);
                    continue;
                }
                // 匹配车系信息
                List<ZhiJiaCarSeriesInfo> carSeriesInfos = zhiJiaDataProcessService.getCarSeriesInfos(zhiJiaCarBrandInfo.getBrandId());
                ZhiJiaCarInfoDTO zhiJiaCarSeriesInfo = zhiJiaDataProcessService.getZhiJiaCarSeriesInfo(zhiJiaClueBackData, carSeriesInfos);
                if(!zhiJiaCarSeriesInfo.getIsMatch()){
                    log.warn("匹配车系信息异常, 请求：{}， 返回：{} "
                            , JSONObject.toJSONString(zhiJiaClueBackData), JSONObject.toJSONString(zhiJiaCarSeriesInfo));
                    updatePushStatus(id, 4, null, zhiJiaCarSeriesInfo.getErrorMsg());
                    // 钉钉报警
                    StringBuilder sb = new StringBuilder("# 之家车系信息匹配异常\n");
                    errorStatistics(id,zhiJiaCarSeriesInfo.getErrorMsg(),sb);
                    continue;
                }

                // 获取token
                String token = zhiJiaDataProcessService.getToken();
                if(StringUtils.isEmpty(token)){
                    log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.ZHIJIA_SERVICEERROR.getCode(), "之家线索获取token为空！,id:" +id));
                    updatePushStatus(id, 4, null, "之家线索获取token为空！,id:" +id);
                    continue;
                }

                // 解密手机号
                String cell = RpcClientProxy.decode(zhiJiaClueBackData.getCell(), "cell", "md5", "");
                if(StringUtils.isEmpty(cell)){
                    log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.ZHIJIA_SERVICEERROR.getCode(), "之家线索解密手机号失败！,id:" +id));
                    updatePushStatus(id, 4, null, "之家线索解密手机号失败！,id:" +id);
                    continue;
                }

                // 组装参数
                reqAddZhiJiaClueDTO.setAccess_token(token);
                reqAddZhiJiaClueDTO.setCid(cityCountyDataDTO.getCId());
                reqAddZhiJiaClueDTO.setCountyid(cityCountyDataDTO.getCountyId());
                reqAddZhiJiaClueDTO.setBrandid(zhiJiaCarSeriesInfo.getBrandId() != null ?
                        String.valueOf(zhiJiaCarSeriesInfo.getBrandId()) : "");
                reqAddZhiJiaClueDTO.setSeriesid(zhiJiaCarSeriesInfo.getSeriesId() != null ?
                        String.valueOf(zhiJiaCarSeriesInfo.getSeriesId()) : "");
                reqAddZhiJiaClueDTO.setMobile(cell);
                reqAddZhiJiaClueDTO.setMobilecode(encryptCell(cell));
                reqAddZhiJiaClueDTO.setFirstregtime(zhiJiaClueBackData.getFirstregtime());
                if(!zhiJiaClueBackData.getPlatenum().isEmpty()){
                    reqAddZhiJiaClueDTO.setPlatenum(zhiJiaClueBackData.getPlatenum());
                }
                if(!zhiJiaClueBackData.getMileage().isEmpty()){
                    reqAddZhiJiaClueDTO.setMileage(zhiJiaClueBackData.getMileage());
                }
                reqAddZhiJiaClueDTO.setAppid(StringUtils.isNotBlank(zhiJiaClientAppid) ? Integer.parseInt(zhiJiaClientAppid) : 1742);

                // 调用高质线索创建接口
                Result<String> result = zhiJiaClient.addZhiJiaClue(reqAddZhiJiaClueDTO);
                // 更新结果
                if (ResultCode.SUCCESS.getValue().equals(result.getCode())) {
                    // 创建线索成功则更新状态和cclId
                    if(StringUtils.isNotEmpty(result.getData())){
                        int cclId = Integer.parseInt(result.getData());
                        updatePushStatus(id, 2, cclId, result.getMessage());
                    }
                } else {
                    // 创建线索失败
                    updatePushStatus(id, 3, null, result.getMessage());
                    // 钉钉报警
                    StringBuilder sb = new StringBuilder("# 之家创建线索异常\n");
                    errorStatistics(id,result.getMessage(),sb);
                }
            }catch (Exception e){
                log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.ZHIJIA_SERVICEERROR.getCode(), "之家线索创建异常！"), e);
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

    /**
     * 手机号加密
     * @param cell
     * @return
     */
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
            byte[] encryptedBytes = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
            return Base64.encodeBase64String(encryptedBytes);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void sendAlarm(Integer failNum, String title) {
        if (failNum > 0) {
            try {
                alarmClient.sendAlarm("推送失败条数=" + failNum, title, AlarmSendCodeEnum.ZHIJIA_SERVICEERROR.getCode());
            } catch (Exception ex) {
                log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.ZHIJIA_SERVICEERROR.getCode(), ex.getMessage()), ex);
            }
        }
    }

    /**
     * 2023-09-27 18:08
     * 错误信息告警
     */
    private void errorStatistics(Long id,
                                 String errorMsg,StringBuilder sb) {

        // 之家告警参数
        Map<String, JSONObject> webHookInfo = marketingCommonConfig.getDingDingWebHookInfo();
        Map<String, Object> map = webHookInfo.get(DingDingAlarmFunctionEnum.ZHIJIA_CLUEFEEDBACK_MSG.toString());

        DingDingMarkdownMessage.Markdown markdown = new DingDingMarkdownMessage.Markdown();
        String title = "之家线索异常信息";
        markdown.setTitle(title);
        sb.append("错误数据id：").append(id).append("|\n")
                .append("错误原因：").append(errorMsg)
                .append("|\n");
        String text = sb.toString();
        markdown.setText(text);
        DingDingMarkdownMessage dingDingMarkdownMessage = new DingDingMarkdownMessage();
        dingDingMarkdownMessage.setMarkdown(markdown);
        dingDingRobotHookService.sendMessageGroup(map.get("token").toString(), map.get("secret").toString(), dingDingMarkdownMessage, isProxy);
    }

}
