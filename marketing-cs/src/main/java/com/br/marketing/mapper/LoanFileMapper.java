package com.br.marketing.mapper;

import com.br.marketing.entity.LoanFile;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Created by Bairong on 2019/10/18.
 */
@Repository
public interface LoanFileMapper {
    Integer insertFile(LoanFile blf);

    List<LoanFile> queryFile(String apiCode);

    List<LoanFile> queryFileById(Long id);
    List<LoanFile> queryUploadFile(String apiCode);

    void updateFile(LoanFile blf);

    void update(LoanFile blf);

    LoanFile queryBlf(String batchNumber);

    /**
     *查询今天生成的结果文件信息
     * @param apiCode 客户编号
     * @return
     */
    List<LoanFile> queryTodayFile(String apiCode);

    List<LoanFile> queryResultByApiCode(String apiCode);

    List<LoanFile> queryEmptyFileListByApiCode(String apiCode);

    List<LoanFile> querydataNumDiffListByApiCode(String apiCode);

    List<String> getApiCodes();

    /**
     *查询今天生成的结果哟文件的数据量
     * @param apiCode 客户编号
     * @return
     */
    Map<String,BigDecimal> queryTotalDataNum(String apiCode);

    /**
     *查询今天任务的状态
     * @param apiCode 客户编号
     * @return
     */
    List<Integer> queryTadayTaskStatus(String apiCode);

    /**
     * 修改标识文件状态
     * @param apiCode 客户编号
     */
    void updateSignFileStatus(String apiCode);

    /**
     * 查询结果文件在本地服务器磁盘上的路径
     * @param param 参数：apiCode、batchNumber、fileName
     * @return 文件路径
     */
    LoanFile queryFilePath(Map<String, String> param);

    /**
     * 更新ftp上文件的信息到DB
     * @param loanFile 文件信息
     */
    void updateFtpFileInfo(LoanFile loanFile);

    /**
     * 查询内部标识为2的批次个数
     * @param apiCode apiCode
     * @return 个数
     */
    Integer queryBlfBySignStatus(String apiCode);

    /**
     * 结果文件同步成功后，修改历史状态
     * @param param 参数
     */
    void updateStatus(Map<String, String> param);

    /**
     * 修改压缩文件状态
     * @param param 参数
     */
    void updateZipFileStatus(Map<String, String> param);



    void updateFileComplete(Map<String, String> param);

    LoanFile selectFileComplete(Map<String, String> param);
}
