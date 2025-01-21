package com.br.marketing.service.sftp;

import com.br.marketing.entity.LoanFile;

import java.util.List;

/**
 * Created by Bairong on 2019/8/28.
 */
public interface PushService {
     void push(List<LoanFile> files) throws Exception;
}
