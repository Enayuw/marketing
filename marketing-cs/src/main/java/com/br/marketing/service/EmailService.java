package com.br.marketing.service;

import com.br.marketing.entity.StraHisFile;

/**
 * Created by Bairong on 2020/7/11.
 * 报警&通知接口类
 */
public interface EmailService {
    /**
     * 系统级别异常报警接口
     *
     * @param context 邮件内容
     * @param type    服务名称
     */
    void sendAlarm(String context, String type);

    /**
     * ppd定制：每天定时发送当日ppd客户任务数据处理结果统计，包括全量字段文件个数、重点字段文件个数、回传状态等。
     *
     * @param apiCode 客户编号
     */
    void sendReport(String apiCode);

    /**
     * 数据量差异：客户上传后正常的数据量-应返回数据量，任务处理完成后返回给客户的数据量-实际返回数据量，应返回数据量与实际返回数据量有差异触发预警。
     * 当日应返回文件数与实际返回文件数有差异
     *
     * @param apiCode 客户编号
     */
    void resultVolumeCheck(String apiCode);

    void resultVolumeCheck(StraHisFile file);

    /**
     * 文件同步异常：数据文件、标识文件正常上传至ftp后，1小时内底层服务未同步至客户SFTP，则触发报警。
     *
     * @param apiCode 客户编号
     */
    void ftpToSftpCheck(String apiCode);

    /**
     * 360定制校验：结果文件大小为0或者超过2M，触发报警。
     *
     * @param apiCode 客户编号
     * @param message 文件名称,文件大小（单位b）
     */
    void fileSizeException(String apiCode, String message);

    /**
     * 结果文件上传异常：数据任务处理结束（数据任务包括，客户上传时的数据解析任务和正式查询，处理数据的任务），结果文件从本地服务器上传到远程ftp服务器后，
     * 校验本地文件数和远程ftp文件数是否一致，文件大小是否一致、标识文件是否正确、目录是否正确，异常情况发送报警。
     *
     * @param apiCode 客户编号
     * @param message
     */
    void fileUploadFtpException(String apiCode, String message);

    /**
     * 内部结果文件统计邮件发送接口
     * 每天下午18点定时发送当天所有任务的结果文件信息
     * 包括批次号，文件个数、数据量、文件大小，上传时间等
     */
    void report();

    /**
     * 文件上传失败：客户上传的文件命名不符合规范、客户api_code校验异常、客户配置文件参数异常
     * 文件上传成功，数据文件校验正常，配置文件校验正常，参数校验正常，数据入库正常
     *
     * @param apiCode 客户编号
     * @param message
     */
    void fileUpload(String apiCode, String message);

    /**
     * 数据量异常：实际数据量与配置文件中客户给的数据量不一致
     *
     * @param apiCode 客户编号
     * @param message
     */
    void dataFileVolumn(String apiCode, String message);

    /**
     * 每日任务处理进度统计报告
     * 每天早上9点半上班前发送
     */
    void progressReport();

    /**
     * 剔除监控文件上传失败：客户上传的文件命名不符合规范、客户api_code校验异常、
     * 剔除监控文件上传成功，数据文件校验正常，配置文件校验正常，参数校验正常，数据入库正常
     *
     * @param apiCode 客户编号
     * @param message
     */
    void deleteMonitorFileUpload(String apiCode, String message);

    /**
     * 画像结果异常报警
     *
     * @param title   标题
     * @param message 内容
     */
    void hxResultErrorAlarm(String title, String message);

    /**
     * 压缩文件校验失败报警
     *
     * @param fileName 文件名称
     * @param apiCode  客户账号
     */
    void zipFileErrorAlarm(String fileName, String apiCode);

    /**
     * 监控今日到期
     */
    void closeDateAlarm();

    /**
     * 监控即将到期
     */
    void monitoringExpirationAlarm();
}