package com.br.marketing.dto.tc;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;

@Data
public class TcTransformNotifyDto extends TcDataDto{

    @Schema(description = "userKey")
    @NotNull(message = "userKey必传")
    @NotEmpty(message = "userKey必传")
    private String userKey;

    @Schema(description = "transformNode")
    @NotNull(message = "transformNode必传")
    @NotEmpty(message = "transformNode必传")
    private String transformNode;

    @Schema(description = "nodeStatus")
    @NotNull(message = "nodeStatus必传")
    @NotEmpty(message = "nodeStatus必传")
    private String nodeStatus;

    @Schema(description = "transformAmount")
    private Long transformAmount;

    @Schema(description = "transformTime")
    @NotNull(message = "transformTime必传")
    @NotEmpty(message = "transformTime必传")
    private String transformTime;

    public String validate() {
        if (StringUtils.isEmpty(batchNo)) {
            return "缺少必输字段data.batchNo";
        }
        if (StringUtils.isEmpty(userKey)) {
            return "缺少必输字段data.userKey";
        }
        if (StringUtils.isEmpty(transformNode)) {
            return "缺少必输字段data.transformNode";
        }
        if (StringUtils.isEmpty(nodeStatus)) {
            return "缺少必输字段data.nodeStatus";
        }
        if (StringUtils.isEmpty(transformTime)) {
            return "缺少必输字段data.transformTime";
        }
        if (null == transformAmount && nodeStatus.equals(NodeStatusEnum.SUCCESS.getStatus())
                && (transformNode.equals(TransformNodeEnum.CREDIT.getCode()) || transformNode.equals(TransformNodeEnum.LOAN.getCode()))) {
            return "缺少必输字段data.transformAmount";
        }
        return null;
    }

    @Getter
    public enum TransformNodeEnum {
        VISIT("VISIT","首访"),
        CREDIT("CREDIT","首次授信"),
        LOAN("LOAN","首次借款");

        private String code;

        private String desc;

        TransformNodeEnum(String code, String desc) {
            this.code = code;
            this.desc = desc;
        }
    }

    @Getter
    public enum NodeStatusEnum {
        SUCCESS("SUCCESS","成功"),
        FAIL("FAIL","失败");
        private String status;

        private String desc;

        NodeStatusEnum(String status, String desc) {
            this.status = status;
            this.desc = desc;
        }
    }


}
