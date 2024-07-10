package com.br.marketing.service.Impl.zhijia;

import com.br.common.encryption.Md5Utils;
import com.br.marketing.client.zhijia.ZhiJiaClient;
import com.br.marketing.client.zhijia.input.ReqAddZhiJiaClueDTO;
import com.br.marketing.client.zhongan.utils.Md5OfZanUtils;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.entity.*;
import com.br.marketing.mapper.ZhiJiaClueBackDataMapper;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.google.api.client.util.Base64;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @ClassName ZhiJiaClueFeedBackServiceImpl
 * @Description TODO
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
    private ZhiJiaClient zhiJiaClient;

    @Value("${api.zhijia.zhiJiaClientAppid:00}")
    private String zhiJiaClientAppid;

    @Override
    public void process() {

        // 创建撞库线程池
        ThreadPoolExecutor zhiJiaCollidingThread =
                BrExecutors.getThreadPool(marketingCommonConfig.getZhiJiaCollidingThread(), marketingCommonConfig.getZhiJiaCollidingThread());

        while (true) {
            // 查询未推送数据
            String now = LocalDate.now().toString();
            ZhiJiaClueBackDataExample example = new ZhiJiaClueBackDataExample();
            example.createCriteria().andStatusEqualTo(1).andPushStatusEqualTo(0)
                    .andCreateDateEqualTo(now);
            example.setOrderByClause("id asc limit 2000");
            List<ZhiJiaClueBackData> zhiJiaClueBackDataList = zhiJiaClueBackDataMapper.selectByExample(example);
            if (zhiJiaClueBackDataList.isEmpty()) {
                break;
            }
            // 执行前将状态改为推送中
            List<Long> ids = zhiJiaClueBackDataList.stream().map(ZhiJiaClueBackData::getId).collect(Collectors.toList());
            zhiJiaClueBackDataMapper.updateBatchById(ids,1);
            // 开始推送
            zhiJiaCollidingThread.execute(() -> pushZhiJiaCollidingSync(zhiJiaClueBackDataList));
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
    }

    public void pushZhiJiaCollidingSync(List<ZhiJiaClueBackData> zhiJiaClueBackDataList){

        for (ZhiJiaClueBackData zhiJiaClueBackData : zhiJiaClueBackDataList) {
            Long id = zhiJiaClueBackData.getId();
            // 调用省市区接口
            // 调用车辆信息接口
            try {
                // 调用高质线索创建接口
                Result result = zhiJiaClient.addZhiJiaClue(buildAddZhiJiaClue(zhiJiaClueBackData));
                // 更新结果
                if (ResultCode.SUCCESS.getValue().equals(result.getCode())) {
                    //匹配成功更新
                    updatePushStatus(id, 2, result.getMessage());
                } else {
                    //匹配失败更新
                    updatePushStatus(id, 4, result.getMessage());
                }
            }catch (Exception e){
                //匹配失败更新
                updatePushStatus(id, 3, e.getMessage());
                log.error("调用高质线索创建接口异常！", e.getMessage());
            }
        }
    }

    private void updatePushStatus(Long id, int status, String message) {
        ZhiJiaClueBackDataExample example = new ZhiJiaClueBackDataExample();
        example.createCriteria().andIdEqualTo(id);
        ZhiJiaClueBackData record = new ZhiJiaClueBackData();
        record.setPushStatus(status);
        record.setErrorMsg(message);
        zhiJiaClueBackDataMapper.updateByExampleSelective(record,example);
    }

    private ReqAddZhiJiaClueDTO buildAddZhiJiaClue(ZhiJiaClueBackData zhiJiaClueBackData) {
        // 组装入参
        ReqAddZhiJiaClueDTO dto = new ReqAddZhiJiaClueDTO();
        dto.setAccess_token("");
        dto.setMobile(zhiJiaClueBackData.getCell());
        dto.setMobilecode(encryptCell(zhiJiaClueBackData.getCell()));
        dto.setCid(1);
        dto.setCountyid(1);
        dto.setBrandid("");
        dto.setSeriesid("");
        dto.setSpecid("");
        dto.setFirstregtime(zhiJiaClueBackData.getFirstRegTime());
        //dto.setPlatenum("");
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

}
