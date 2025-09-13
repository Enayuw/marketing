package com.br.marketing.dto.sanliuling.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @ClassName ContactListDTO
 * @Author kongbx
 * @Date 2025/9/13 15:13
 */
@Data
public class ContactListDTO {
    @ApiModelProperty("联系人编号")
    @JsonProperty("contactCustNum")
    private String contactCustNum;

    @ApiModelProperty("联系人姓名")
    @JsonProperty("contactName")
    private String contactName;

    @ApiModelProperty("联系人电话")
    @JsonProperty("contactCell")
    private String contactCell;

    @ApiModelProperty("联系人关系")
    @JsonProperty("contactRelationship")
    private String contactRelationship;

}
