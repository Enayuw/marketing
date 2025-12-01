package com.br.marketing.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TcyrCpaPackageCleanInfo {

    private Date executeTime;

    private List<TcyrCpaBatchCleanInfo> batchCleanInfos;

}