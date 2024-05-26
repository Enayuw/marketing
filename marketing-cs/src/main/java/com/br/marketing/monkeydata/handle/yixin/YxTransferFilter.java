package com.br.marketing.monkeydata.handle.yixin;

import java.util.List;

public interface YxTransferFilter<T> {

    List<T> filter(List<T> list);
}
