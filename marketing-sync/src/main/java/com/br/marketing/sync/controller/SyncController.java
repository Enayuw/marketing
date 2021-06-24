package com.br.marketing.sync.controller;

import com.br.marketing.entity.SyncConfig;
import com.br.marketing.sync.service.SyncService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/sync/")
@Slf4j
public class SyncController {
    @Resource
    SyncService syncServiceImpl;

    @GetMapping("getFromSftp")
    public String getFromSftp(){
        syncServiceImpl.getFromSftp();
        return "success";
    }

    @GetMapping("putToSftp")
    public String putToSftp(){
        syncServiceImpl.putToSftp();
        return "success";
    }

    /**
     * 			`api_code`,
     * 			`type`,
     * 			`src_path`,
     * 			`target_path`,
     * 			`suffix`,
     * 			`check_finish`,
     * 			`check_success`,
     * 			`status`,
     * 			`remark`,
     * 			`src_sftp_host`,
     * 			`src_sftp_port`,
     * 			`src_sftp_user`,
     * 			`src_sftp_pwd`,
     * 			`target_sftp_host`,
     * 			`target_sftp_port`,
     * 			`target_sftp_user`,
     * 			`target_sftp_pwd`,
     * 			`create_time`
     * @return
     */
    @GetMapping("insertConfig")
    public String insertConfig(SyncConfig loanSyncConfig){
        syncServiceImpl.insertConfig(loanSyncConfig);
        return "success";
    }

}
