package com.br.marketing.check.filter;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPFileFilter;

@Slf4j
public class MyFtpFileFilter implements FTPFileFilter {
    @Override
    public boolean accept(FTPFile ftpFile) {
        boolean flag=false;
       if(ftpFile.isFile()){
           String name = ftpFile.getName();
           log.info("name:{}",name);
           if("4007771_yyy17_20200812.zip".equals(name)){
               flag=true;
           }
       }
        return flag;
    }
}
