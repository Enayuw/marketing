package com.br.marketing.service;

import com.br.marketing.commonentity.PageResultReturn;
import com.br.marketing.entity.LocalFile;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 文件接口
 * <p>
 * --------------------------------
 *
 * @BelongsProject: marketing
 * @BelongsPackage: com.br.marketing.service
 * @Description: 文件接口
 * @CreateTime: 2022-09-15 15 :32
 * @Version: 1.0
 * @Author: guangchao.zhang
 * ------------------------------
 */
public interface LocalFileService {

    /**
     * 获取客户信息列表数据
     * @param page
     * @param pageSize
     * @param apiCode
     * @return
     */
    PageResultReturn list(int page, int pageSize,String search, String apiCode, String uploadStartTime,String uploadEndTime, String fileType);
    Long allCount(String search, String apiCode, String uploadStartTime, String uploadEndTime, String fileType);
    void refreshPushNumber(List<Map<String, Object>> quantityList, Date pushStartTime, Date pushEndTime);

    /**
     * 获取apiCode 大于dayStartTime 下载完成(status=2)的最新记录
     * @param apiCode
     * @return
     */
    List<LocalFile> getLastDataByApiCode(String apiCode, LocalDateTime dayStartTime,LocalDateTime dateEndTime);


}
