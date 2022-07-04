## marketing 营销平台
### 模块说明
### marketing-api 对接客户的接口服务
- 原始数据上传接口（上传数据消费端）
- 标准转化接口（标准转化数据消费端）
- 萨摩耶转化接口
- 数禾转化接口
- - -
### marketing-inner-api 对接内部的接口服务
- 接口  
  1.跑分配置Customer、FastTaskRule、ResourceAllocation、RuleOfProduct、RuleOfScore
  、RuleOfSole、ScoreOptLog、SyncConfig、SyncReport  
  2.规则中心 PushRuleFilter  
  3.对接客服通话明细 ZnkPush
- 消费端  
  1.推送客服  
  2.查询客服推送状态  
  3.哈啰消费  
  4.推送黑名单
- - -
### marketing-check  读取文件入库、报警、校验、方法重试等
- job  
  ~~1.ApiToDb--生成跑分任务~~  
  ~~2.ApiToDbByTimeJob--根据时间参数 生成指定范围的跑分任务~~  
  3.RetryCommonServiceJob--重试服务job  
  4.SftpToDbByResultDataJob--读取上传电销文件入库（通用文件，小薇文件，桔子文件）
  5.SftpToDbByTwoSevenJob--读取七七撞库文件  
  6.SftpToDbByCommonJob--读取文件落表 通用job  
  7.TransferFileTaskJob--转化数据落sftp  
  8.ResultJob--校验生成的跑分文件  
  ~~9.SftpToDbJob--读取文件生成跑分任务~~  
  10.QueryHaierJob--查询海尔数据
  11.PushHaierJob--推送海尔电销数据
- 消费端  
  1.推送电销  
  2.推送黑名单  
  3.七七数据消费  
  4.海尔数据消费
- - -   
### marketing-task 跑分服务。
- TaskScoreStartJob--跑分job
- TaskActionJob--跑分操作job
  0-暂停跑分程序以及暂停任务；1-恢复跑分程序；2-恢复被暂停的任务；
- - -
### marketing-push-task 结果推送调度任务，流失预警结果文件推送到ftp。部署在调度平台。
- mergeJob--合并文件，推送文件至内部sftp
- - -
### marketing-cs、marketing-utils 公共依赖。
- - -
### marketing-sync 文件同步
- GetFromSftpJob--拉取文件从客户ftp至内部ftp
- PutToSftpJob--推送文件从内部ftp至客户ftp
- - -


注意：
每次上线都需要将最新代码合并到master分支，为当前版本创建tag