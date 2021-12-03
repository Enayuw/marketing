package com.br.marketing.client.haier.output;

import com.br.marketing.utils.RsaUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.codec.binary.Base64;
import org.springframework.util.Assert;
import org.springframework.util.DigestUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Set;
import java.util.function.Function;

/**
 * 推送名单数据
 *
 * @author zeqiang.guo@brgroup.com
 * @dateTime 2021/12/2 16:52
 */
public class PushDTO {
    //对接方标识
    private String apiCode;
    // 推送数据
    private String formData;
    //校验值
    private String checkData;

    public PushDTO() {
    }

    public PushDTO(String apiCode, Set<DataItems> t, Function<Set<DataItems>, FormData> function, String apiKey) throws Exception {
        Assert.notNull(t, "list is not null");
        FormData formDataObj = function.apply(t);
        ObjectMapper objectMapper = new ObjectMapper();
        this.formData = Base64.encodeBase64String(RsaUtil.encryptByPublicKey(objectMapper.writeValueAsString(formDataObj)
                .getBytes(StandardCharsets.UTF_8), apiKey));
        this.apiCode = apiCode;
        this.checkData = DigestUtils.md5DigestAsHex(this.formData.concat(this.apiCode).concat(apiKey)
                .getBytes(StandardCharsets.UTF_8));
    }


    private byte[] toByteArray(Object obj) throws IOException {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(obj);
            oos.flush();
            return bos.toByteArray();
        } catch (IOException e) {
            throw e;
        }
    }

    public String getApiCode() {
        return apiCode;
    }

    public void setApiCode(String apiCode) {
        this.apiCode = apiCode;
    }

    public String getFormData() {
        return formData;
    }

    public void setFormData(String formData) {
        this.formData = formData;
    }

    public String getCheckData() {
        return checkData;
    }

    public void setCheckData(String checkData) {
        this.checkData = checkData;
    }

    @Override
    public String toString() {
        return "PushDTO{" +
                "apiCode='" + apiCode + '\'' +
                ", formData='" + formData + '\'' +
                ", checkData='" + checkData + '\'' +
                '}';
    }


    public static class FormData implements Serializable {
        private static final long serialVersionUID = 2227641858566088690L;
        //全局唯一ID
        private String requestId;

        /* 待转化状态
           码值：促注册 1;促申额 2;促首贷 32
        */
        private String type;
        //当天相同type对应同一batchNo
        private String batchNo;
        private Set<DataItems> dataItems;

        public FormData() {
        }

        public FormData(String requestId, String type) {
            this.requestId = requestId;
            this.type = type;
            this.batchNo = LocalDateTime.now().format(DateTimeFormatter.BASIC_ISO_DATE).concat(type);
        }

        public FormData(String requestId, String type, Set<DataItems> dataItemsSet) {
            this.requestId = requestId;
            this.type = type;
            this.batchNo = LocalDateTime.now().format(DateTimeFormatter.BASIC_ISO_DATE).concat(type);
            this.dataItems = dataItemsSet;
        }

        public void setRequestId(String requestId) {
            this.requestId = requestId;
        }

        public void setType(String type) {
            this.type = type;
            this.batchNo = LocalDateTime.now().format(DateTimeFormatter.BASIC_ISO_DATE).concat(type);
        }

        public void setDataItemsSet(Set<DataItems> dataItemsSet) {
            this.dataItems = dataItemsSet;
        }

        public String getRequestId() {
            return requestId;
        }

        public String getBatchNo() {
            return batchNo;
        }

        public String getType() {
            return type;
        }

        public Set<DataItems> getDataItemsSet() {
            return dataItems;
        }
    }

    public static class DataItems implements Serializable {
        private static final long serialVersionUID = -4349718363357947519L;
        //任务ID，与上传接口(接口1)taskId一致
        private String taskId;
        //客户编号，同上传接口(接口1)custNum
        private String custNum;

        public DataItems() {
        }

        public DataItems(String taskId, String custNum) {
            this.taskId = taskId;
            this.custNum = custNum;
        }

        public String getTaskId() {
            return taskId;
        }

        public void setTaskId(String taskId) {
            this.taskId = taskId;
        }

        public String getCustNum() {
            return custNum;
        }

        public void setCustNum(String custNum) {
            this.custNum = custNum;
        }
    }
}
