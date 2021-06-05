package com.br.marketing.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

@Data
public class MarketingPreUserDTO {

    @NotNull(message = "taskId必传")
    @NotEmpty(message = "taskId必传")
    private String taskId;

    @NotNull(message = "dataItems必传")
    @Size(min = 1,message = "dataItems必传")
    private List<MarketingPreUserDetailDTO> dataItems;
}
