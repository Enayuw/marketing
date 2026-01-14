package com.br.marketing.vo.tccpa;

import com.br.marketing.enums.TcCpaFailMsgEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TcyrCpaFailMsgVO {

    /**
     *
     */
    private Integer value;

    /**
     *
     */
    private String desc;

    public static TcyrCpaFailMsgVO fromFailMsgEnum(TcCpaFailMsgEnum tcCpaFailMsgEnum) {
        TcyrCpaFailMsgVO tcyrCpaFailMsgVO = new TcyrCpaFailMsgVO();
        tcyrCpaFailMsgVO.value = tcCpaFailMsgEnum.getValue();
        tcyrCpaFailMsgVO.desc = tcCpaFailMsgEnum.getDesc();
        return tcyrCpaFailMsgVO;
    }
}
