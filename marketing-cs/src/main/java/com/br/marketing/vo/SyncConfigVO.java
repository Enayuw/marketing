package com.br.marketing.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class SyncConfigVO {

    @Schema(description = "主键id")
    private Long id;

    @Schema(description = "apiCode")
    private String apiCode;

    @Schema(description = "源目录")
    private String srcPath;

    @Schema(description = "目的目录")
    private String targetPath;

    @Schema(description = "同步文件的类型。1：sftp>>本地磁盘，2：本地磁盘>>sftp")
    private Integer type;

    @Schema(description = "文件类型。1:跑分上传文件,2:错误文件,3:电销文件,4:七七撞库文件")
    private Integer dataType;

}
