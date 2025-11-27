package com.br.marketing.dto.tc;

import com.alibaba.fastjson.JSONObject;
import com.br.marketing.util.tc.RSAUtil;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.Getter;
import org.hibernate.validator.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
public class TcResponseDTO {

    @Schema(description = "code")
    @NotNull(message = "code必传")
    @NotEmpty(message = "code必传")
    private String code;

    @Schema(description = "msg")
    @NotNull(message = "msg必传")
    @NotEmpty(message = "msg必传")
    private String msg;

    @Schema(description = "sign")
    @NotNull(message = "sign必传")
    @NotEmpty(message = "sign必传")
    private String sign;

    @Schema(description = "timestamp")
    @NotNull(message = "timestamp必传")
    @NotEmpty(message = "timestamp必传")
    private String timestamp;

    @Schema(description = "data")
    @NotNull(message = "data必传")
    @NotEmpty(message = "data必传")
    private String data;

    public TcResponseDTO success(String brPrivateKey) {
        this.code = TcResponseDTO.ResultEnum.SUCCESS.getCode();
        this.msg = TcResponseDTO.ResultEnum.SUCCESS.getMsg();
        this.timestamp = String.valueOf(System.currentTimeMillis());
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("status", TcResponseDTO.ResultEnum.SUCCESS.getStatus());
        this.data = jsonObject.toJSONString();
        RSAUtil.sign(this, brPrivateKey);
        return this;
    }

    public TcResponseDTO outterParamsFail(String brPrivateKey, String msg) {
        this.code = TcResponseDTO.ResultEnum.OUTTERPARAMS_ERROR.getCode();
        this.msg = msg;
        this.timestamp = String.valueOf(System.currentTimeMillis());
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("status", ResultEnum.OUTTERPARAMS_ERROR.getStatus());
        this.data = jsonObject.toJSONString();
        RSAUtil.sign(this, brPrivateKey);
        return this;
    }

    public TcResponseDTO signFail(String brPrivateKey) {
        this.code = TcResponseDTO.ResultEnum.SIGN_ERROR.getCode();
        this.msg = TcResponseDTO.ResultEnum.SIGN_ERROR.getMsg();
        this.timestamp = String.valueOf(System.currentTimeMillis());
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("status", ResultEnum.SIGN_ERROR.getStatus());
        this.data = jsonObject.toJSONString();
        RSAUtil.sign(this, brPrivateKey);
        return this;
    }

    public TcResponseDTO idempotentFail(String brPrivateKey) {
        this.code = ResultEnum.IDEMPOTENT_ERRROR.getCode();
        this.msg = ResultEnum.IDEMPOTENT_ERRROR.getMsg();
        this.timestamp = String.valueOf(System.currentTimeMillis());
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("status", ResultEnum.IDEMPOTENT_ERRROR.getStatus());
        this.data = jsonObject.toJSONString();
        RSAUtil.sign(this, brPrivateKey);
        return this;
    }

    public TcResponseDTO innerParamsFail(String brPrivateKey, String msg) {
        this.code = TcResponseDTO.ResultEnum.INNERPARAMS_ERROR.getCode();
        this.timestamp = String.valueOf(System.currentTimeMillis());
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("status", ResultEnum.INNERPARAMS_ERROR.getStatus());
        jsonObject.put("msg", msg);
        this.data = jsonObject.toJSONString();
        RSAUtil.sign(this, brPrivateKey);
        return this;
    }

    public TcResponseDTO systemFail(String brPrivateKey) {
        this.code = TcResponseDTO.ResultEnum.SYSTEM_ERROR.getCode();
        this.msg = TcResponseDTO.ResultEnum.SYSTEM_ERROR.getMsg();
        this.timestamp = String.valueOf(System.currentTimeMillis());
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("status", ResultEnum.SYSTEM_ERROR.getStatus());
        this.data = jsonObject.toJSONString();
        RSAUtil.sign(this, brPrivateKey);
        return this;
    }

    public TcResponseDTO failed(String brPrivateKey, String msg) {
        this.code = TcResponseDTO.ResultEnum.BIZ_ERROR.getCode();
        this.msg = TcResponseDTO.ResultEnum.BIZ_ERROR.getMsg();
        this.timestamp = String.valueOf(System.currentTimeMillis());
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("status", ResultEnum.BIZ_ERROR.getStatus());
        jsonObject.put("msg", msg);
        this.data = jsonObject.toJSONString();
        RSAUtil.sign(this, brPrivateKey);
        return this;
    }

    @Getter
    public enum ResultEnum {
        SUCCESS("0000", "SUCCESS","成功"),
        OUTTERPARAMS_ERROR("4000", "FAIL",null),
        SIGN_ERROR("5000", "FAIL","验签失败"),
        INNERPARAMS_ERROR("6000", "FAIL",null),
        IDEMPOTENT_ERRROR("7000", "FAIL", "requestNo重复"),
        SYSTEM_ERROR("8000", "FAIL","系统异常"),
        BIZ_ERROR("9000", "FAIL","业务异常");

        private String code;

        private String status;

        private String msg;

        ResultEnum(String code, String status, String msg) {
            this.code = code;
            this.msg = msg;
            this.status = status;
        }
    }
}
