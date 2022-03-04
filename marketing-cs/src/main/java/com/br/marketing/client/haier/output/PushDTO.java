package com.br.marketing.client.haier.output;

import com.br.marketing.client.haier.utils.Md5Utils;
import com.br.marketing.client.haier.utils.RsaUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.codec.binary.Base64;
import org.springframework.util.Assert;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

/**
 * 推送名单数据
 *
 * @author zeqiang.guo@brgroup.com
 * @dateTime 2021/12/2 16:52
 */
public class PushDTO extends BaseDTO {

    public PushDTO() {
        super();
    }

    public PushDTO(String apiCode, FormData formData, String apiKey) throws Exception {
        super(apiCode);
        Assert.notNull(formData, "formData is not null");
        final String formDataStr = new ObjectMapper().writeValueAsString(formData);
        Assert.notNull(formDataStr, "formDataStr is not null");
        super.formData = Base64.encodeBase64String(RsaUtil.encryptByPublicKey(
                formDataStr.getBytes(StandardCharsets.UTF_8), apiKey));
        super.checkData = Md5Utils.genMd5(formDataStr.concat(apiCode).concat(apiKey));
    }

    public PushDTO(String apiCode, Set<DataItems> t, Function<Set<DataItems>, FormData> function, String apiKey) throws Exception {
        super(apiCode);
        Assert.notNull(t, "list is not null");
        FormData formDataObj = function.apply(t);
        Assert.notNull(formDataObj, "formDataObj is not null");
        final String formDataStr = new ObjectMapper().writeValueAsString(formDataObj);
        Assert.notNull(formDataStr, "formDataStr is not null");
        super.formData = Base64.encodeBase64String(RsaUtil.encryptByPublicKey(
                formDataStr.getBytes(StandardCharsets.UTF_8), apiKey));
        super.checkData = Md5Utils.genMd5(formDataStr.concat(apiCode).concat(apiKey));
    }

    @Override
    public String toString() {
        return "PushDTO{" +
                "apiCode='" + apiCode + '\'' +
                ", formData='" + formData + '\'' +
                ", checkData='" + checkData + '\'' +
                '}';
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

        public FormData(String requestId, String type, String batchNo, Set<DataItems> dataItems) {
            this.requestId = requestId;
            this.type = type;
            this.batchNo = batchNo;
            this.dataItems = dataItems;
        }

        public FormData(String requestId, String type, Set<DataItems> dataItems) {
            this.requestId = requestId;
            this.type = type;
            this.batchNo = LocalDateTime.now().format(DateTimeFormatter.BASIC_ISO_DATE).concat(type);
            this.dataItems = dataItems;
        }


        public FormData(String requestId, String type) {
            this.requestId = requestId;
            this.type = type;
            this.batchNo = LocalDateTime.now().format(DateTimeFormatter.BASIC_ISO_DATE).concat(type);
        }

        public <T> FormData(String requestId, String type, String batchNo, List<T> list, Function<List<T>, Set<DataItems>> function) {
            this.requestId = requestId;
            this.type = type;
            this.batchNo = batchNo;
            Assert.notNull(list, "list is not null");
            this.dataItems = function.apply(list);
        }

        public <T> FormData(String requestId, String type, List<T> list, Function<List<T>, Set<DataItems>> function) {
            this.requestId = requestId;
            this.type = type;
            this.batchNo = LocalDateTime.now().format(DateTimeFormatter.BASIC_ISO_DATE).concat(type);
            Assert.notNull(list, "list is not null");
            this.dataItems = function.apply(list);
        }

        public void setRequestId(String requestId) {
            this.requestId = requestId;
        }

        public void setType(String type) {
            this.type = type;
            this.batchNo = LocalDateTime.now().format(DateTimeFormatter.BASIC_ISO_DATE).concat(type);
        }

        public void setBatchNo(String batchNo) {
            this.batchNo = batchNo;
        }

        public void setDataItems(Set<DataItems> dataItems) {
            this.dataItems = dataItems;
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

        public Set<DataItems> getDataItems() {
            return dataItems;
        }

        @Override
        public String toString() {
            return "FormData{" +
                    "requestId='" + requestId + '\'' +
                    ", type='" + type + '\'' +
                    ", batchNo='" + batchNo + '\'' +
                    ", dataItems=" + dataItems +
                    '}';
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

        @Override
        public String toString() {
            return "DataItems{" +
                    "taskId='" + taskId + '\'' +
                    ", custNum='" + custNum + '\'' +
                    '}';
        }
    }
}
