package com.br.marketing.config;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.entity.QifuStrategyReportData;
import com.br.marketing.entity.rocketmq.RocketMQSwitchEntity;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.monitor.grpc.EnvUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.core.env.Environment;

import java.util.UUID;


/**
 * RocketMQ和RabbitMQ切换开关
 * @Author: yu.xia@brgroup.com
 * @Date: 2024-08-22
 */
@Slf4j
@Component
public class RocketMQSwitch {
    /**
     * speed中配置启用RocketMQ的部署环境，多个以逗号分隔
     * 参数从SRE的yaml配置中获取 SPEED_ENV
     */
    public static final String ENV_SPEED = "env";
    /**
     * speed中配置启用RocketMQ的服务名称
     * 多个以逗号分隔
     */
    public static final String NAME_SPEED = "name";
    /**
     * speed中配置启用RocketMQ的apiCode
     * 多个以逗号分隔
     */
    public static final String APICODES_SPEED = "apiCodes";
    /**
     * speed中配置启用RocketMQ的tags
     * 多个以逗号分隔
     */
    public static final String TAGS_SPEED = "tags";
    /**
     * TAG对应的小开关
     */
    public static final String FLAG = "flag";
    /**
     *
     */
    public static final String TAG = "tag";

    @Autowired
    private Environment env;

    @Autowired
    private MarketingCommonConfig marketingCommonConfig;

    public Boolean rocketMQSwitchFlag(String apiCode, String tag){
        UUID uuid = UUID.randomUUID();
        log.warn("[{}]rocketMQSwitchFlag--apiCode[{}]tag[{}]", uuid, apiCode, tag);
        String rocketMqSwitchString = marketingCommonConfig.getRocketMqSwitch2();
        if(StringUtils.isBlank(rocketMqSwitchString)){
            return Boolean.FALSE;
        }
        RocketMQSwitchEntity entity = JSON.parseObject(rocketMqSwitchString, new TypeReference<RocketMQSwitchEntity>() {
        }.getType());
        String global = entity.getGlobal();
        if("true".equalsIgnoreCase(global)){
            return Boolean.TRUE;
        }else{
            JSONObject group = entity.getGroup();
            JSONObject tagObjet = group.getJSONObject(tag);
            if(null == tagObjet || tagObjet.isEmpty()){
                return Boolean.FALSE;
            }else{
                Boolean flagBoolean = tagObjet.getBoolean(FLAG);
                log.warn("[{}]rocketMQSwitchFlag--tagObjet[{}]flagBoolean[{}]", uuid, tagObjet, flagBoolean);
                if(flagBoolean){
                    return Boolean.TRUE;
                }else{
                    if(StringUtils.isBlank(apiCode)){
                        return Boolean.FALSE;
                    }
                    String apiCodes = tagObjet.getString(APICODES_SPEED);
                    if(StringUtils.isNotBlank(apiCodes) && apiCodes.contains(apiCode)){
                        return Boolean.TRUE;
                    }
                }
            }
            return Boolean.FALSE;
        }
    }

    public static void main(String[] args) {
        String rocketMqSwitchString = "{\"global\":\"false\",\"group\":{\"Marketing.PreUser.Receive\":" +
                "{\"flag\":false,\"apiCodes\":\"7410950,7410951\"},\"Marketing.PreUser.Receive.Small\":" +
                "{\"flag\":true,\"apiCodes\":\"7410950,7410951\"}}}";
        String apiCode = "74109501";
        String tag = "Marketing.PreUser.Receive";
        RocketMQSwitchEntity entity = JSON.parseObject(rocketMqSwitchString, new TypeReference<RocketMQSwitchEntity>() {
        }.getType());
        String global = entity.getGlobal();
        if("true".equalsIgnoreCase(global)){
            System.out.println(true);
            return;
        }else{
            JSONObject group = entity.getGroup();
            JSONObject tagObjet = group.getJSONObject(tag);
            if(null == tagObjet || tagObjet.isEmpty()){
                System.out.println(false);
                return;
            }else{
                Boolean flagBoolean = tagObjet.getBoolean(FLAG);
                if(flagBoolean){
                    System.out.println(true);
                    return;
                }else{
                    if(StringUtils.isBlank(apiCode)){
                        System.out.println(false);
                        return;
                    }
                    String apiCodes = tagObjet.getString(APICODES_SPEED);
                    if(StringUtils.isNotBlank(apiCodes) && apiCodes.contains(apiCode)){
                        System.out.println(true);
                        return;
                    }
                }
            }
            System.out.println(false);
        }
    }

//    public static void main(String[] args) {
//        String rocketMqSwitchString = "{\"global\":\"true\",\"group\":[{\"flag\":true,\"tag\":{\"Marketing.PreUser.Receive\":\"7410950,7410951\"}},{\"flag\":true,\"tag\":{\"Marketing.PreUser.Receive.Small\":\"7411950,7412950\"}}]}";
//        String apiCode = "74109501";
//        String tag = "Marketing.PreUser.Receive";
//        RocketMQSwitchEntity entity = JSON.parseObject(rocketMqSwitchString, new TypeReference<RocketMQSwitchEntity>() { }.getType());
//        String global = entity.getGlobal();
//        if("false".equalsIgnoreCase(global)){
//            System.out.println(false);
//        }else{
//            JSONArray group = entity.getGroup();
//            for (int i = 0; i < group.size(); i++) {
//                JSONObject object = group.getJSONObject(i);
//                Boolean flagBoolean = object.getBoolean(FLAG);
//                if(flagBoolean){
//                    JSONObject tagJSONObject = object.getJSONObject(TAG);
//                    String tagUseApiCodes = tagJSONObject.getString(tag);
//                    if(StringUtils.isBlank(apiCode)){
//                        System.out.println(false);
//                    }
//                    if(StringUtils.isNotBlank(tagUseApiCodes) && tagUseApiCodes.contains(apiCode)){
//                        System.out.println(true);
//                    }
//                }
//            }
//            System.out.println(false);
//        }
//    }

//    /**
//     * 判断是否使用RocketMQ发送消息的开关
//     * @Author yu.xia@brgroup.com
//     * @Date 2024/8/22 11:16
//     * @param apiCode apiCode
//     * @param tag tag
//     * @return Boolean true使用RocketMQ；false使用RabbitMQ
//     */
//    public Boolean rocketMQSwitchFlag(String apiCode, String tag){
//        JSONObject rocketMqSwitch = marketingCommonConfig.getRocketMqSwitch1();
//        if(null == rocketMqSwitch || rocketMqSwitch.isEmpty()){
//            return Boolean.FALSE;
//        }else{
//            String envSpeed = rocketMqSwitch.getString(ENV_SPEED);
//            String nameSpeed = rocketMqSwitch.getString(NAME_SPEED);
//            String apiCodesSpeed = rocketMqSwitch.getString(APICODES_SPEED);
//            String tagsSpeed = rocketMqSwitch.getString(TAGS_SPEED);
//            Boolean flag = Boolean.TRUE;
//            if(null != envSpeed){
//                String speedEnv = EnvUtil.getProperties("SPEED_ENV");
//                if(StringUtils.isNotBlank(speedEnv)){
//                    flag = flag && envSpeed.contains(speedEnv);
//                }
//            }
//            String name = getServiceName();
//            if(null != nameSpeed && StringUtils.isNotBlank(name)){
//                flag = flag && nameSpeed.contains(name);
//            }
//            if(null != apiCodesSpeed && StringUtils.isNotBlank(apiCode)){
//                flag = flag && apiCodesSpeed.contains(apiCode);
//            }
//            if(null != tagsSpeed && StringUtils.isNotBlank(tag)){
//                flag = flag && tagsSpeed.contains(tag);
//            }
//            return flag;
//        }
//    }
//
//    public String getServiceName() {
//        return env.getProperty("spring.application.name");
//    }
//
//    public static void main(String[] args) {
////        String conf = "{\"env\":\"pre,prod\",\"name\":\"marketing-api,marketing-mq-consumer\",\"apiCodes\":\"7410950,7410957\",\"tags\":\"Marketing.PreUser.Receive,Marketing.PreUser.Receive.Small\"}";
//        String conf = null;
//        String apiCode = "";
//        String tag = "";
//        String name = "";
//        String speedEnv = "";
//        JSONObject rocketMqSwitch = JSON.parseObject(conf);
//        if(null == rocketMqSwitch || rocketMqSwitch.isEmpty()){
//            System.out.println("false");
//        }else{
//            String envSpeed = rocketMqSwitch.getString(ENV_SPEED);
//            String nameSpeed = rocketMqSwitch.getString(NAME_SPEED);
//            String apiCodesSpeed = rocketMqSwitch.getString(APICODES_SPEED);
//            String tagsSpeed = rocketMqSwitch.getString(TAGS_SPEED);
//            Boolean flag = Boolean.FALSE;
//            if(null != envSpeed){
//                if(StringUtils.isNotBlank(speedEnv)){
//                    flag = flag || envSpeed.contains(speedEnv);
//                }
//            }
//            if(null != nameSpeed && StringUtils.isNotBlank(name)){
//                flag = flag || nameSpeed.contains(name);
//            }
//            if(null != apiCodesSpeed && StringUtils.isNotBlank(apiCode)){
//                flag = flag || apiCodesSpeed.contains(apiCode);
//            }
//            if(null != tagsSpeed && StringUtils.isNotBlank(tag)){
//                flag = flag || tagsSpeed.contains(tag);
//            }
//            System.out.println(flag);
//        }
//    }

}
