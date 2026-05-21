package com.br.marketing.constant;

/**
 * 滴滴 AI 接入相关、在代码中集中维护的固定常量，避免魔法数散落在各业务类里。
 *
 * <p>当前数值以硬编码形式存在，与营销公共配置里滴滴接口号的默认取值保持一致；若后续要改为配置中心或环境变量驱动，
 * 建议只在本类或单一配置适配层替换取值来源，尽量不动调用方方法签名。
 *
 * @author yueping.bai
 */
public final class DidiaiFixedConfig {

    /** 滴滴定制化上传在营销侧登记的接口编号，对外联调与库表维度均使用该值。 */
    public static final String UPLOAD_API_CODE = "7413678";

    /**
     * 定制化上传汇总数据分表用的后缀片段，与数据访问层按后缀拼接物理表名时使用；当前约定对应一张具体实例表，
     * 表名由基础名加该后缀组成。
     */
    public static final String UPLOAD_TCID = "_9356";

    /**
     * 离线补偿任务总开关：为真时调度触发后会执行扫表与入库逻辑；为假时入口立即返回，便于在预发或未就绪环境关闭写入。
     */
    public static final boolean OFFLINE_CLEAN_PUSH_JOB_ENABLED = true;

    /** 离线任务单次从数据库拉取的待处理记录条数上限，用于控制内存与单次执行时长。 */
    public static final int OFFLINE_JOB_PAGE_SIZE = 100;

    /** 是否开放给联调或测试用的模拟加密上传入口，与生产真实解密链路区分。 */
    public static final boolean SIM_UPLOAD_ENABLED = true;

    /** 模拟上传场景下优先使用的应用标识；若留空则运行时从已配置应用列表中取第一条。 */
    public static final String SIM_DEFAULT_APP_KEY = "";

    private DidiaiFixedConfig() {}
}

