package com.br.marketing.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Objects;

/**
 * 客户 cid、apiCode 信息vo
 *
 * @author zeqiang.guo@brgroup.com
 * @dateTime 2021/9/1 15:46
 */
@Schema(description = "cid、apiCode信息")
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CustomerSelectVO {
    /**
     * 2021/8/31 16:11 合作客户ID
     */
    @Schema(description = "合作客户ID")
    private String cid;
    /**
     * 2021/8/31 16:11 接口编码
     */
    @Schema(description = "接口编码")
    private String apiCode;

    /**
     * 合作客户名称
     */
    @Schema(description = "合作客户名称")
    private String name;

    /**
     * 合作客户简称
     */
    @Schema(description = "合作客户简称")
    private String shortName;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CustomerSelectVO that = (CustomerSelectVO) o;
        return Objects.equals(cid, that.cid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cid);
    }
}
