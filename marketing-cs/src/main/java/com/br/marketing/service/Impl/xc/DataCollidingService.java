package com.br.marketing.service.Impl.xc;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public interface DataCollidingService<T> {
    void pushDataAndHandleResult(List<T> list);
}
