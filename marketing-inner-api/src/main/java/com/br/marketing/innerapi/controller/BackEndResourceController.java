package com.br.marketing.innerapi.controller;

import com.alibaba.fastjson.JSONObject;
import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.constants.rocketmq.MarketingAssistConstants;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.config.RocketMqSwitch;
import com.br.marketing.entity.XieChengData;
import com.br.marketing.innerapi.service.ResourceAllocationService;
import com.br.marketing.mapper.XieChengDataMapper;
import com.br.marketing.origin.DataLoadingHandlerService;
import com.br.marketing.service.ICustomerConfigService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadPoolExecutor;

import static com.br.marketing.common.utils.MQConstants.ROUTING_KEY_UNIVERSAL_SFTPTODB_XIECHENGRECEIVE;

@RestController
@RequestMapping(value = "/backend/resource")
public class BackEndResourceController {

    private static final Logger log = LoggerFactory.getLogger(ResourceAllocationController.class);

    @Autowired
    ResourceAllocationService resourceAllocationService;

    @Autowired
    ICustomerConfigService iCustomerConfigService;

    @Resource
    private DataLoadingHandlerService dataLoadingHandlerService;


    @Resource
    private RocketMqSwitch rocketMqSwitch;

    @Operation(summary = "新增zk节点信息", description = "新增zk节点")
    @GetMapping("/createZkData")
    public ApiResult<Boolean> createZkData(String path, String data){
        try {
            Boolean flag = resourceAllocationService.createZkData(path,data);
            return new ApiResult<Boolean>().success(flag);
        } catch (Exception ex) {
            ex.printStackTrace();
            log.error(ex.getMessage(),ex);
        }
        return new ApiResult<Boolean>().success(Boolean.FALSE);
    }

    @Operation(summary = "修改zk节点信息", description = "修改zk节点信息")
    @GetMapping("/setNodeData")
    public ApiResult<Boolean> setNodeData(String path,String data){
        try {
            Boolean flag = resourceAllocationService.setNodeData(path,data);
            return new ApiResult<Boolean>().success(flag);
        } catch (Exception ex) {
            ex.printStackTrace();
            log.error(ex.getMessage(),ex);
        }
        return new ApiResult<Boolean>().success(Boolean.FALSE);
    }

    @Operation(summary = "删除zk节点信息", description = "删除zk节点信息")
    @GetMapping("/deleteZkData")
    public ApiResult<Boolean> deleteZkData(String path){
        try {
            Boolean flag = resourceAllocationService.deleteZkData(path);
            return new ApiResult<Boolean>().success(flag);
        } catch (Exception ex) {
            ex.printStackTrace();
            log.error(ex.getMessage(),ex);
        }
        return new ApiResult<Boolean>().success(Boolean.FALSE);
    }

    @Operation(summary = "查看zk节点信息", description = "查看zk节点信息")
    @GetMapping("/seeZkData")
    public ApiResult<String> seeZkData(String path){
        try {
            String content = resourceAllocationService.seeZkData(path);
            return new ApiResult<String>().success().setData(content);
        } catch (Exception ex) {
            ex.printStackTrace();
            log.error(ex.getMessage(),ex);
            return new ApiResult<String>().fail().setMessage(ex.getMessage());
        }
    }

    @GetMapping("/updateEncryptyType")
    public ApiResult updateEncryptyType(@RequestParam("apiCode") String apiCode, @RequestParam("type") Integer type){
        Result result = iCustomerConfigService.updateEncryptyType(apiCode, type);
        return new ApiResult().fromResult(result,1);
    }

    @Resource
    XieChengDataMapper xieChengDataMapper;


    /**
     * 携程通话明细回调广告上报压测接口
     * @return
     */
    @GetMapping("/pushMq")
    public String pushMq(@RequestParam("createDate") Integer createDate){

        ThreadPoolExecutor threadPool = BrExecutors.getThreadPool(5, 5);
        Boolean mark = Boolean.TRUE;
        Long localId = null;
        while (mark){
            List<XieChengData> datas = xieChengDataMapper.getByCellTodayAndLocalId(createDate, localId);
            if(datas.size()<=0){
                mark = Boolean.FALSE;
                continue;
            }
            localId = datas.get(datas.size()-1).getLocalId();
            final List<XieChengData> objects = new ArrayList<>();
            objects.addAll(datas);
            threadPool.submit(()->{
                for (XieChengData data : objects) {
                    JSONObject msg = new JSONObject();
                    msg.put("localId", data.getLocalId());
                    msg.put("type", 2);
//                    producter.send(ROUTING_KEY_UNIVERSAL_SFTPTODB_XIECHENGRECEIVE
//                            , msg.toJSONString());
                    rocketMqSwitch.sendMessage(data.getApiCode(), MarketingAssistConstants.TOPIC,
                            MarketingAssistConstants.TAG_MARKETING_UNIVERSAL_SFTPTODB_XIECHENGRECEIVE, msg.toJSONString(),
                            ROUTING_KEY_UNIVERSAL_SFTPTODB_XIECHENGRECEIVE);
                }
            });

        }
        threadPool.shutdown();
        while (!threadPool.isTerminated()) {
            try {
                Thread.sleep(1000);
            }catch (Exception ex){
                ex.printStackTrace();
            }
        }
        return "ok";
    }

    @GetMapping("/getRulesFromCache")
    public String hset(@RequestParam("apiCode") String apiCode) {
        Set<String> rules = dataLoadingHandlerService.customerRules(apiCode);
        return rules.toString();
    }
}
