package com.br.marketing.bridge.didiai;

import com.alibaba.fastjson.JSON;
import com.br.marketing.client.marketingapi.input.UploadDataDTO;
import com.br.marketing.dto.MarketingPreUserDTO;
import com.br.marketing.dto.MarketingPreUserDetailDTO;

import java.util.List;
import java.util.Random;

/**
 * 滴滴 AI 离线链路中，将清洗结果组装为内部网关推送所需 UploadDataDTO 的构建器。
 *
 * <p>背景：通用清洗服务返回的明细列表需要封装进 MarketingPreUserDTO，再序列化为 jsonData 字段，
 * 并配合 apiCode 组成上传 DTO，供 PushInfoService.pushUploadByRetry 方法使用。本类负责生成符合下游约定的
 * requestId 格式及随机后缀，降低与其它通道请求号碰撞的概率。
 *
 * @author yueping.bai
 */
public final class DidiaiUploadDataBuilder {

    private DidiaiUploadDataBuilder() {}

    /**
     * 根据接口编号、业务侧任务标识与清洗后的用户明细列表构造上传数据对象。
     *
     * <p>生成的 MarketingPreUserDTO 中 requestId 字段由 apiCode、taskId、当前毫秒时间与五位数随机数拼接，
     * 仅保证同进程内短时间内的实用性，不承诺全局唯一性由本方法单独承担。
     *
     * @param apiCode 接口编号，与配置及上游约定一致
     * @param taskId  业务任务或请求标识，通常与汇总表 requestId 或业务 taskId 对齐
     * @param details 清洗服务输出的用户明细列表，写入 dataItems
     * @return 已设置 apiCode 与 jsonData 的 UploadDataDTO，可直接用于重试推送
     */
    public static UploadDataDTO build(
            String apiCode, String taskId, List<MarketingPreUserDetailDTO> details) {
        Random random = new Random();
        int randomNumber = 10000 + random.nextInt(90000);
        String requestId = apiCode + "_" + taskId + "_" + System.currentTimeMillis() + "_" + randomNumber;
        MarketingPreUserDTO marketingPreUserDTO = new MarketingPreUserDTO();
        marketingPreUserDTO.setTaskId(taskId);
        marketingPreUserDTO.setRequestId(requestId);
        marketingPreUserDTO.setDataItems(details);
        UploadDataDTO uploadDataDTO = new UploadDataDTO();
        uploadDataDTO.setApiCode(apiCode);
        uploadDataDTO.setJsonData(JSON.toJSONString(marketingPreUserDTO));
        return uploadDataDTO;
    }
}
