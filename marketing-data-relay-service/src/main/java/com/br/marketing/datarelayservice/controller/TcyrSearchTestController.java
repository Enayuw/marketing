package com.br.marketing.datarelayservice.controller;


import com.br.marketing.common.commondto.Result;
import com.br.marketing.datarelayservice.service.TcSearchService;
import com.br.marketing.entity.MarketingTcyrSyncRecord;
import com.br.marketing.service.tc.TcSyncDataMatchService;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.core.io.UrlResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipInputStream;


@Api(value = "同程易融tet")
@RequestMapping("/marketing/v1/api/tc")
@RestController
@Slf4j
public class TcyrSearchTestController {

    @Resource
    private TcSearchService tcSearchService;

    @Resource
    private TcSyncDataMatchService tcPullGzFileService;

    @GetMapping("/search")
    public ResponseEntity<String> search() {
        try {
            Object object = tcSearchService.search();
            return ResponseEntity.ok(object.toString());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("文件解压失败，请检查文件是否损坏：" + e.getMessage());
        }
    }

    @RequestMapping("/syncTest")
    public ResponseEntity<String> syncTest()  {
        String apiCode = "7492773";
        List<MarketingTcyrSyncRecord> syncRecordList = tcPullGzFileService.searchTcyrSyncList(apiCode,1);
        for (MarketingTcyrSyncRecord syncRecord : syncRecordList) {
            try {
                tcPullGzFileService.updageTcyrRecordSyncStatus(syncRecord.getBatchNo(),3);
                Result syncResult =tcPullGzFileService.dealTcyrFileSync(syncRecord);
                if (syncResult != null  && syncResult.isSuccess()) {
                    tcPullGzFileService.updageTcyrRecordSyncStatus(syncRecord.getBatchNo(),4);
                    log.warn("fileSync任务执行成功, batchNo:{}",syncRecord.getBatchNo());
                }
            }catch (Exception e) {
                log.error("syncTest 异常,",e);
                //修改 b_marketing_tcyr_sync_record 批次号 对应记录为失败
            }
        }
        return ResponseEntity.ok("ok");
    }


}
