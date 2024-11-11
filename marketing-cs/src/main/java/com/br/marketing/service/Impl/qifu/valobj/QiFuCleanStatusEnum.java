package com.br.marketing.service.Impl.qifu.valobj;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum QiFuCleanStatusEnum {

    RUNNING(1, "处理中")
    , SUCCESS(2, "处理成功")
    , FAILPUSH(3, "推送失败")
    , FAILDATAACTION(4, "数据处理失败");

    private Integer value;
    private String desc;
}
