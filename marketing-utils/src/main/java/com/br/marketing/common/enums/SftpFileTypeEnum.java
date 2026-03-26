package com.br.marketing.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SftpFileTypeEnum {
    SEVEN("qiqi")
    ,DX("dianxiao")
    ,HLBYTRANSFORM("hl_transform")
    ,SHBYTRANSFORM("sh_transform")
    ,DXTRANSFORM("dx_transform")
    ,DXIBU("dx_ibu")
    ,ZHONGBANGLABEL("zhongbanglabel")
    ,TONGCHENG_UNDO_PUSHTOCUSTOMER("tongcheng_undo_pushToCustomer")
    ,XIECHENGSMSQUIT("xiechengsms")
    ,ZHIJIACLUE("zhijiaclue")
    ,SUSHANG_TRANSFER("sushang_transfer")
    ,SUSHANG_CALLRECORD("sushang_callrecord")
    ,WUBA_COLLIDING("wuba_colliding")
    ,WUBA_OLD_COLLIDING("wuba_old_colliding")
    ,XIECHENG_ACTIVATE("xiecheng_activate")
    ,DD("didi")
    ,DD_BLACK("didi_black")
    , GUO_MEI_DATA_CALLBACK("guo_mei_data_callback")
    ,SHUNFENG_COMPANY("shunfeng_company")
    ,PP_RONGSHU_MARK("pp_rongshu_mark")
    ,SMY_PUSH_BLACK_LIST("smy_black_list")
    ,SANLIULING_PP("sanliuling_pp")
    ,XIECHENG_CPS_COLLIDING("xiecheng_cps_colliding")
    ,ZHONGBANG_AI("zhongbang_ai")
    ,ZHONGBANG_AI_VOICE("zhongbang_ai_voice")
    ,SYJ_ORIGINAL("syj_original")
    ,SYJ_BLACK("syj_black")
    ;
   private String value;
}
