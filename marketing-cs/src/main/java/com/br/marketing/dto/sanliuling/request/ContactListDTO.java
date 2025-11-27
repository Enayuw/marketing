package com.br.marketing.dto.sanliuling.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @ClassName ContactListDTO
 * @Author kongbx
 * @Date 2025/9/13 15:13
 */
@Data
public class ContactListDTO {
    @Schema(description = "联系人编号")
    @JsonProperty("contactCustNum")
    private String contactCustNum;

    @Schema(description = "联系人姓名")
    @JsonProperty("contactName")
    private String contactName;

    @Schema(description = "联系人电话")
    @JsonProperty("contactCell")
    private String contactCell;

    @Schema(description = "联系人电话原始值")
    @JsonProperty("originalCell")
    private String originalCell;

    @Schema(description = "联系人关系")
    @JsonProperty("contactRelationship")
    private String contactRelationship;

}
