package com.br.marketing.datarelayservice.dto.smy.request;

import com.alibaba.fastjson.annotation.JSONField;
import io.swagger.annotations.ApiModelProperty;
import java.io.Serializable;
import java.util.List;
import lombok.Data;

@Data
public class SmyUploadRequestDTO implements Serializable {
    private static final long serialVersionUID = -3120269378878940556L;
    @ApiModelProperty("请求流水号 注：每个请求唯一")
    @JSONField(name = "request_no")
    private String requestNo;
    @ApiModelProperty("客群标识批次号 注：同个批次的所有请求相同 例：WP_FIN_DT_100_20231010")
    @JSONField(name = "case_type")
    private String caseType;
    @ApiModelProperty("批次总数")
    @JSONField(name = "total")
    private Integer total;
    @ApiModelProperty("代运营名单列表 注：单次上传的名单不大于 100 条")
    @JSONField(name = "name_list")
    private List<NameValueDTO> nameList;
}
