package com.br.marketing.monkey.service.zhongbang;

import com.br.marketing.entity.LocalFile;

import java.time.LocalDate;

public interface ZhongBangAIVoiceService {
    boolean voiceAIFileUpload(String apiCode, String cId, LocalDate value);

    void voiceAIFileUploadDetail(LocalFile localFile, LocalDate localDate);
}
