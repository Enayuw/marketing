/**
 * @author Hua Qiang
 * @version
 * @date 2024-08-27 20:48
 */
package com.br.marketing.common.constants.rocketmq;

/**
 * RocketMQ 常见概念命名规范
 * 命名规范：
 * <p>
 * topic：
 * 不能为空，只能包含字母、数字、“-”及“_”，3-64 字符
 * 建议格式：String.format("tp_%s_%s", "系统名","业务名")
 * 例如：tp_order_check
 * <p>
 * tag：
 * 允许为空，只要是字符串就可以，用来做 topic 下的二级消息过滤
 * 建议格式：String.format（"tag_%s","业务动作或类别"）
 * 例如：tag+业务动作，例如：订单创建的 tag为：tag_create；订单关闭的tag为：tag_close。
 * <p>
 * keys：
 * 允许为空，建议设置，只要是字符串或字符串数组都可以，用来在控制台查询消息或查询消息轨迹
 * 建议格式：String.format("%s","业务唯一性 ID")
 * 例如：订单 ID，交易 ID 或流水号，TraceID 等唯一性 ID。
 * <p>
 * producer group：
 * 不能为空，3-64个字符，只能包含字母、数字、“-”及“_”
 * 建议格式：String.format("pg_%s_%s", "系统名","业务名")
 * 例如：pg_transfer_check
 * <p>
 * consumer group：
 * 不能为空，3-64个字符，只能包含字母、数字、“-”及“_”
 * 建议格式：String.format("cg_%s_%s", "系统名","业务名")
 * 例如：cg_transfer_check
 * <p>
 * role：
 * 不能为空，只支持数字、大小写字母、分隔符（"_"、"-"），不能超过 32 个字符
 * 不同业务读写权限的标记，建议格式：业务名 + 发送/消费
 * 例如：role_order_send, role_order_consume，role_order_all
 * <p>
 * clientId：
 * clientId 表示一个客户端实例 ID，不同客户端不可重复。clientId 不能在客户端直接设置，instanceName 是 clientId 的可选组成部分，可以通过调整instanceName 修改 clientId。
 * <p>
 * instanceName：
 * 实例名，默认场景不需要用户特殊设置，默认系统会通过以下代码随机生成一个唯一值。
 * 广播消费者在每次启动时，需要保持消费者实例名不变，读取客户端本地的进度文件，需要显式地设置 instanceName，并且广播消费需要保持当前客户端 IP 启动前后不变， 如果容器部署，需要设置固定 pod IP 调度，否则重启期间的广播消息会漏消费。
 * 建议格式：String.format("instance-%s-%s","系统名","业务名")。
 * <p>
 * <p>
 * <p>
 * <p>
 * 使用规范：
 * <p>
 * 生产者：
 * 【强制】一个领域服务只能有一个 topic。
 * 【强制】领域服务发送消息时必须根据业务动作设置 tag。
 * 【强制】在 producer 发送消息时必须设置 keys。
 * 【强制】消息发送成功或者失败要打印消息日志，务必要打印 SendResult 和 key 字段。
 * 【强制】新建生产者时必须指定生产者分组。
 * 【建议】消息发送失败后建议将消息存储到 db，然后由定时器类线程进行定时重试，确保消息达到 broker。
 * 【建议】对于可靠性要求不高的业务场景可以使用 oneway 消息。
 * <p>
 * 消费者：
 * 【强制】新建消费者时必须指定消费者分组。
 * 【强制】消息消费者无法避免消息重复，所以需要业务服务来保证消息消费幂等。
 * 【建议】为了提高消费并行度，可以在同一个 ConsumerGroup 下启动多个 Consumer 实例或者通过修改 ConsumeThreadMin 和consumeThreadMax 来提高单个 Consumer 的并行消费能力。
 * 【建议】为了增加业务吞吐量，可以通过设置 consumer 的 consumeMessageBatchMaxSize 来批量消费消息。
 * 【建议】发生消息堆积时，如果业务对数据要求不高时，可以选择丢弃不重要的消息。
 * 【建议】如果消息量较少，建议在消费入口打印消息、消费耗时等，方便后续排查问题。
 */
