package com.br.marketing.file.controller;

import com.br.marketing.file.service.sync.FileSyncService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/fileSync/")
@Slf4j
public class SyncController {

    @Resource
    FileSyncService fileSyncService;

    @GetMapping("pullFromSftp")
    public String getFromSftp() {
        fileSyncService.pullFromSftp();
        return "success";
    }

    @GetMapping("pushToSftp")
    public String putToSftp() {
        fileSyncService.pushToSftp();
        return "success";
    }
}
