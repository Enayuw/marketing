package com.br.marketing.client.dassservice.input.black;

import java.io.Serializable;
import java.util.List;

/**
 * 黑名单抽象类
 *
 * @author Guo Zeqiang
 * @dateTime 2022/3/1 13:35
 */
public abstract class BlackListAbstract implements Serializable {
    private static final long serialVersionUID = -238632921160746444L;

    public abstract List<Object> valueList(String ascKey);
}
