package com.br.marketing.client.dassservice.input.black;

import com.br.marketing.common.utils.AESUtil;
import com.br.marketing.common.utils.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 黑名单
 *
 * @author Guo Zeqiang
 * @dateTime 2022/3/1 13:36
 */
public class BlackListDTO extends BlackListAbstract {
    private static final long serialVersionUID = 2263700614183680472L;
    /**
     * 2022/3/1 13:43 用户唯一ID
     * 是否必填 是
     */
    private String uid;
    /**
     * 2022/3/1 13:43 手机号AES加密
     * 是否必填 否
     */
    private String phone;
    /**
     * 2022/3/1 13:43 转化节点
     * 是否必填 否
     */
    private String type;
    /**
     * 2022/3/1 13:43 客户场景类型
     * 是否必填 否
     */
    private String userType;
    /**
     * 2022/3/1 13:43 数据源
     * 是否必填 否
     */
    private String source;
    /**
     * 2022/3/1 13:43 营销中台编号
     * 是否必填 否
     */
    private String apiCode;
    /**
     * 2022/3/1 13:43 机构：yixin
     * 是否必填 是
     */
    private String orgName;

    public BlackListDTO() {
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getApiCode() {
        return apiCode;
    }

    public void setApiCode(String apiCode) {
        this.apiCode = apiCode;
    }

    public String getOrgName() {
        return orgName;
    }

    public void setOrgName(String orgName) {
        this.orgName = orgName;
    }

    public BlackListDTO(String uid, String phone, String type, String userType, String source
            , String apiCode, String orgName) {
        this.uid = uid;
        this.phone = phone;
        this.type = type;
        this.userType = userType;
        this.source = source;
        this.apiCode = apiCode;
        this.orgName = orgName;
    }

    @Override
    public String toString() {
        return "BlackListDTO{" +
                "uid='" + uid + '\'' +
                ", phone='" + phone + '\'' +
                ", type='" + type + '\'' +
                ", userType='" + userType + '\'' +
                ", source='" + source + '\'' +
                ", apiCode='" + apiCode + '\'' +
                ", orgName='" + orgName + '\'' +
                '}';
    }

    @Override
    public List<Object> valueList(String ascKey) {
        List<Object> list = new ArrayList<>();
        if (StringUtils.isNotBlank(this.uid)) {
            list.add(this.uid);
        }
        if (StringUtils.isNotBlank(this.phone)) {
            if (StringUtils.isEmpty(ascKey)) {
                list.add(this.phone);
            } else {
                list.add(AESUtil.decrypt(this.phone, ascKey));
            }
        }
        if (StringUtils.isNotBlank(this.type)) {
            list.add(this.type);
        }
        if (StringUtils.isNotBlank(this.userType)) {
            list.add(this.userType);
        }
        if (StringUtils.isNotBlank(this.source)) {
            list.add(this.source);
        }
        if (StringUtils.isNotBlank(this.apiCode)) {
            list.add(this.apiCode);
        }
        if (StringUtils.isNotBlank(this.orgName)) {
            list.add(this.orgName);
        }
        return list;
    }
}
