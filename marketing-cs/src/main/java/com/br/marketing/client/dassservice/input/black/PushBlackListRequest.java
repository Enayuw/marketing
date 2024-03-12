package com.br.marketing.client.dassservice.input.black;

import com.google.common.base.Joiner;
import org.springframework.util.CollectionUtils;
import org.springframework.util.DigestUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 入参
 *
 * @author Guo Zeqiang
 * @dateTime 2022/3/1 13:33
 */
public class PushBlackListRequest {

    /**
     * 2022/3/1 13:45 签名过期时间
     */
    private long ts;
    /**
     * 2022/3/1 13:45 签名
     */
    private String sign;
    /**
     * 2022/3/1 13:45 数据
     */
    private List<BlackListDTO> data;


    public PushBlackListRequest(List<BlackListDTO> data, String secretKey, String ascKey) {
        this.ts = System.currentTimeMillis() / 1000;
        this.sign = spliceSign(data, secretKey, ascKey);
        this.data = data;
    }

    public String spliceSign(List<BlackListDTO> data, String secretKey, String ascKey) {
        List<Object> sortList = new ArrayList<>();
        sortList.add(String.valueOf(this.ts));
        if (CollectionUtils.isEmpty(data)) {
            data = Collections.emptyList();
        }
        data.forEach(t -> {
            List<Object> list = t.valueList(ascKey);
            sortList.addAll(list);
            list.clear();
        });
        List<Object> sort2List = sortList.stream().sorted().collect(Collectors.toList());
        sortList.clear();
        String paramValue = Joiner.on("").join(sort2List);
        return DigestUtils.md5DigestAsHex(String.format(secretKey + "%s", paramValue).getBytes());
    }

    public PushBlackListRequest() {
    }

    public long getTs() {
        return ts;
    }

    public void setTs(long ts) {
        this.ts = ts;
    }

    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }

    public List<BlackListDTO> getData() {
        return data;
    }

    public void setData(List<BlackListDTO> data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "PushBlackListRequest{" +
                "ts=" + ts +
                ", sign='" + sign + '\'' +
                ", data=" + data +
                '}';
    }
}
