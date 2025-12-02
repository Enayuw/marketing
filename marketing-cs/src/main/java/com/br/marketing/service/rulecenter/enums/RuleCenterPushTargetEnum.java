package com.br.marketing.service.rulecenter.enums;


import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum RuleCenterPushTargetEnum {

    PUSH_POLICY(0, "pushPolicyPushStrategy", "推送决策"),
    ORIGINAL_INTERFACE(1, "dataLabelPushStrategy", "数据打标"),
    MERGE_PUSH_POLICY(2,"mergeDataPushStrategy","合并数据推送决策"),
    HALO_CALLBACK(3,"haloCallbackPushStrategy","哈啰硅基人回调"),
    UPLOAD_REPUSH_POLICY(4,"uploadRePushPolicyStrategy","上传数据重推决策(operateType=3,4,5,6)");

    private Integer code;
    private String pushAchieve;
    private String desc;


    public static RuleCenterPushTargetEnum findPushNameByCode(Integer code) {
        for (RuleCenterPushTargetEnum e : RuleCenterPushTargetEnum.values()) {
            if (e.getCode().equals(code)) {
                return e;
            }
        }
        return null;

    }


}
