package com.br.marketing.check.job;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.client.HttpProxyClient;
import com.br.marketing.entity.CustomerCallingDataStatus;
import com.br.marketing.entity.CustomerCallingDialog;
import com.br.marketing.mapper.CustomerCallingDataStatusMapper;
import com.br.marketing.mapper.CustomerCallingDialogMapper;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static com.br.marketing.check.utils.CallingUtil.getJsonObject;

/**
 * @author guangchao.zhang
 * @Classname CallingBackInfoJob
 * @Description 首次拨打发送信息回调
 * @Date 2022/2/21 10:41 AM
 */
@Component
@Slf4j
public class CallingBackInfoJob extends AbstractSimpleElasticJob {
    @Resource
    CustomerCallingDialogMapper customerCallingDialogMapper;

    @Resource
    CustomerCallingDataStatusMapper customerCallingDataStatusMapper;

    @Resource
    HttpProxyClient httpProxyClient;


    @Override
    public void process(JobExecutionMultipleShardingContext jobExecutionMultipleShardingContext) {
        // 查询需要更新的用户的request id

      List<Map<String,Object>> requestList =   customerCallingDialogMapper.getRequestId();

        for (Map<String, Object> stringObjectMap : requestList) {
            Object pushUrl = stringObjectMap.get("pushUrl");
            Object requestId = stringObjectMap.get("requestId");
            Object extendConfigInfo = stringObjectMap.get("extendConfigInfo");
            JSONObject extendConfigInfoJson = getJsonObject(extendConfigInfo.toString());
            JSONObject pushUrlJson = getJsonObject(pushUrl.toString());
            String getUrl = pushUrlJson.getString("getUrl");
            Boolean isProxy = extendConfigInfoJson.getBoolean("isProxy") == null ? Boolean.TRUE : extendConfigInfoJson.getBoolean("isProxy");
            if(!getUrl.isEmpty()){
                JSONObject param = new JSONObject();
                param.put("requestId", requestId);
                Map<String, Object> result = httpProxyClient.request(getUrl, param.toJSONString(), isProxy);
                String data = result.get("data").toString();
                JSONObject jsonObject = JSONObject.parseObject(data);
                JSONArray resultData = jsonObject.getJSONArray("resultData");
                for(int i=0;i<resultData.size();i++){
                    JSONObject dataItem = resultData.getJSONObject(i);
                    Long id = Long.valueOf(dataItem.getString("id"));
                    String taskId = dataItem.getString("taskId");
                    String caseNum = dataItem.getString("caseNum");
                    String status = dataItem.getString("status");
                    CustomerCallingDialog customerCallingDialog = new CustomerCallingDialog();
                    customerCallingDialog.setId(id);
                    customerCallingDialog.setSendStatus(2);
                    customerCallingDialogMapper.updateByPrimaryKeySelective(customerCallingDialog);
                    if("failure".equals(status)){
                        String errorDescription = dataItem.getString("errorDescription");
                        CustomerCallingDataStatus customerCallingDataStatus = new CustomerCallingDataStatus();
                        customerCallingDataStatus.setRequestId(requestId.toString());
                        customerCallingDataStatus.setSendStatus(2);
                        customerCallingDataStatus.setDescription(errorDescription);
                        customerCallingDataStatus.setCreateTime(new Date());
                        customerCallingDataStatusMapper.insertSelective(customerCallingDataStatus);
                    }
                }
            }

        }
    }
}
