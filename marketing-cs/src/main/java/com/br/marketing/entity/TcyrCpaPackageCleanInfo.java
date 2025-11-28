package com.br.marketing.entity;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.List;

@Getter
@Setter
public class TcyrCpaPackageCleanInfo {

    private Date executeTime;

    private List<TcyrCpaBatchCleanInfo> batchCleanInfos;

    public TcyrCpaPackageCleanInfo(Date executeTime, List<TcyrCpaBatchCleanInfo> batchCleanInfos) {
        this.executeTime = executeTime;
        this.batchCleanInfos = batchCleanInfos;
    }
}