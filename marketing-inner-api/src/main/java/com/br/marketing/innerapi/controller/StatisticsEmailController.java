package com.br.marketing.innerapi.controller;

import com.br.marketing.client.AlarmApiClient;
import com.br.marketing.common.commondto.ApiResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

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
 * @Description : 数据统计发送email处理接口
 * ---------------------------------
 * @Author : jilong.xu
 * @Date : Create in 2022/3/8 17:04
 */
@RestController
@RequestMapping(value = "statistics")
public class StatisticsEmailController {

    @Resource
    private AlarmApiClient alarmApiClient;

    @PostMapping("/sendEmail")
    public ApiResult<Boolean> sendEmail(String title,String receivers,String data) {
        StringBuilder content = new StringBuilder();
        content.append("<h3><b>数据统计内容：</b></h3>")
                .append(data);
        alarmApiClient.sendMails(title,content.toString(),receivers);
        return new ApiResult<Boolean>().success(true);
    }
}
