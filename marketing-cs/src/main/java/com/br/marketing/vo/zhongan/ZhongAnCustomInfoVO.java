package com.br.marketing.vo.zhongan;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * @ClassName ZhongAnCustomInfoVO
 * @Description TODO
 * @Author kongbx
 * @Date 2024/9/18 16:18
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ZhongAnCustomInfoVO {

    @ApiModelProperty("数据日期")
    private String reportDate;
    @ApiModelProperty("场景")
    private Integer userType;
    @ApiModelProperty("组别")
    private Integer constituencies;
    @ApiModelProperty("总数据量")
    private Integer totalNum;
    @ApiModelProperty("进件人数")
    private Integer incomingNum;
    @ApiModelProperty("批核人数")
    private Integer approversNum;

}
