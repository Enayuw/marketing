package com.br.marketing.datarelayservice.service;


import com.br.marketing.dto.xyf.XyfEncryptionDTO;

/**
 * 信用飞接口
 */
public interface XyfCustomizeService {

    XyfEncryptionDTO batchSubmit(XyfEncryptionDTO encryptionBody, String apiCode);
}
