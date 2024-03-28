package com.br.marketing.service.Impl.tongcheng;

import com.br.marketing.entity.LocalFile;

import java.util.List;

public interface TongChengUndoListPushToCustomerService {
    void process(LocalFile localFile);
    void refreshLocalFile(List<Long> localIdList);
}
