package com.br.marketing.datarelayservice.dto.smy.request;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;
import io.swagger.annotations.ApiModelProperty;
import java.io.Serializable;
import java.util.List;
import lombok.Data;

@Data
public class SmyTransferRequestDTO implements Serializable {
    private static final long serialVersionUID = -3120269378878940556L;
    @ApiModelProperty("事件类型 例：登陆：login; 注册：regist; 人脸识别：F1; 身份认证：F2; 填联系人：F3; 完件：finish; 授信成功：approve; 交易成功：loan; 客诉名单：blacklist")
    @JSONField(name = "event_type")
    private String eventType;
    @ApiModelProperty("事件发生的毫秒级时间戳 例：1694597797924")
    @JSONField(name = "event_time ")
    private Long eventTime;
    @ApiModelProperty("回传标识 注：最长为256")
    @JSONField(name = "cid")
    private String cid;
    @ApiModelProperty("扩展json字段 注：JSON字符串 例：假完件标识：finish_fake; API完件标识：finish_api; 授信额度区间：acct_level")
    @JSONField(name = "extend_fields")
    private String extendFields;
}
