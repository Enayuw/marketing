package com.br.marketing.client.haier.output;

import com.br.marketing.client.haier.utils.Md5Utils;
import com.br.marketing.client.haier.utils.RsaUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.codec.binary.Base64;
import org.springframework.util.Assert;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;

/**
 * 结果查询
 *
 * @author zeqiang.guo@brgroup.com
 * @dateTime 2021/12/6 14:17
 */
public class ResultQueryDTO extends BaseDTO {

    public ResultQueryDTO() {
        super();
    }

    public ResultQueryDTO(String apiCode, String requestId, String apiKey) throws Exception {
        super(apiCode);
        Assert.notNull(requestId, "requestId is not null");
        final String formDataStr = new ObjectMapper().writeValueAsString(new FormData(requestId));
        Assert.notNull(formDataStr, "formDataStr is not null");
        super.formData = Base64.encodeBase64String(RsaUtil.encryptByPublicKey(
                formDataStr.getBytes(StandardCharsets.UTF_8), apiKey));
        super.checkData = Md5Utils.genMd5(formDataStr.concat(apiCode).concat(apiKey));
    }

    @Override
    public String toString() {
        return "ResultQueryDTO{" +
                "apiCode='" + apiCode + '\'' +
                ", formData='" + formData + '\'' +
                ", checkData='" + checkData + '\'' +
                '}';
    }

    public static class FormData implements Serializable {
        private static final long serialVersionUID = 2227641858566088690L;
        //全局唯一ID
        private String requestId;

        public FormData() {
        }

        public FormData(String requestId) {
            this.requestId = requestId;
        }

        public void setRequestId(String requestId) {
            this.requestId = requestId;
        }

        public String getRequestId() {
            return requestId;
        }

        @Override
        public String toString() {
            return "FormData{" +
                    "requestId='" + requestId + '\'' +
                    '}';
        }
    }
}
