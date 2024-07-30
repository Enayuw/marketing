package com.br.marketing.service.Impl.wuba;

import com.br.common.log.AlertLog;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.webhook.dingding.msgtype.DingDingMarkdownMessage;
import com.br.marketing.webhook.dingding.service.DingDingRobotHookService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @Description WuBaDingDingService
 * @Author lixiang
 * @Date 2024-07-10
 */
@Service
@Slf4j
public class WuBaDingDingService {

    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Resource
    private DingDingRobotHookService dingDingRobotHookService;

    public void sendAlert(String title, String text){
        try {
            String accessToken = marketingCommonConfig.getQiFuDingDingAccessToken();
            String secret = marketingCommonConfig.getQiFuDingDingSecret();
            sendAlert(title, text, accessToken, secret);
        }catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.EXCEPTION_WUBA.getCode(), e.getMessage()));
        }
    }

    public void sendAlert(String title, String text, String accessToken, String secret){
        // DingDingAlert
        DingDingMarkdownMessage.Markdown markdown = new DingDingMarkdownMessage.Markdown();
        markdown.setTitle(title);
        markdown.setText(text);
        DingDingMarkdownMessage dingDingMarkdownMessage = new DingDingMarkdownMessage();
        dingDingMarkdownMessage.setMarkdown(markdown);
        dingDingRobotHookService.sendMessageGroup(accessToken, secret, dingDingMarkdownMessage, true);
    }
}
