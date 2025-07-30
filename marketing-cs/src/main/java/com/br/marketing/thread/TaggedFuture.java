package com.br.marketing.thread;

import java.util.concurrent.Future;

public class TaggedFuture<T> {
    private final String tag;
    private final Future<T> future;

    public TaggedFuture(String tag, Future<T> future) {
        this.tag = tag;
        this.future = future;
    }

    // Getter 方法
    public String getTag() { return tag; }
    public Future<T> getFuture() { return future; }
}
